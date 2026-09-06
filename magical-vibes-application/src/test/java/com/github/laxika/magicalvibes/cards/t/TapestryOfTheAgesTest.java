package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TapestryOfTheAges.class, Cancel.class, GrizzlyBears.class, Forest.class})
class TapestryOfTheAgesTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate when no noncreature spell has been cast this turn")
    void cannotActivateWithoutNoncreatureSpell() {
        addReadyTapestry(player1);
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("noncreature spell");
    }

    @Test
    @DisplayName("Casting a creature spell does not enable the ability")
    void creatureSpellDoesNotEnableActivation() {
        addReadyTapestry(player1);
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("noncreature spell");
    }

    @Test
    @DisplayName("Casting a noncreature spell enables the ability and pays its cost")
    void noncreatureSpellEnablesActivation() {
        Permanent tapestry = addReadyTapestry(player1);
        gd.recordSpellCast(player1.getId(), new Cancel());
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);

        assertThat(tapestry.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving the ability draws a card")
    void resolvingDrawsACard() {
        addReadyTapestry(player1);
        gd.recordSpellCast(player1.getId(), new Cancel());
        Card drawn = new Forest();
        harness.setHand(player1, List.of());
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(drawn);
        addActivationMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    private Permanent addReadyTapestry(Player player) {
        Permanent tapestry = new Permanent(new TapestryOfTheAges());
        tapestry.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(tapestry);
        return tapestry;
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
