package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuirionElvesTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Quirion Elves asks its controller to choose a color")
    void entersAskingForColor() {
        harness.setHand(player1, List.of(new QuirionElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Quirion Elves");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");
        assertThat(findPermanent(player1, "Quirion Elves").getChosenColor()).isEqualTo(CardColor.BLUE);
    }

    @Test
    @DisplayName("The first ability taps for {G}")
    void firstAbilityAddsGreen() {
        addReadyElves(player1, CardColor.BLUE);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The second ability taps for one mana of the chosen color")
    void secondAbilityAddsChosenColor() {
        addReadyElves(player1, CardColor.BLUE);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Choosing green makes both abilities produce {G}")
    void chosenColorMayBeGreen() {
        addReadyElves(player1, CardColor.GREEN);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only one of the two abilities can be activated per untap — {T} is a cost")
    void tapCostAllowsOnlyOneActivation() {
        addReadyElves(player1, CardColor.RED);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(findPermanent(player1, "Quirion Elves").isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A summoning-sick Quirion Elves cannot use its tap abilities")
    void summoningSickCannotTap() {
        QuirionElves card = new QuirionElves();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(true);
        perm.setChosenColor(CardColor.BLUE);
        gd.playerBattlefields.get(player1.getId()).add(perm);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyElves(Player player, CardColor chosenColor) {
        QuirionElves card = new QuirionElves();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        perm.setChosenColor(chosenColor);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
