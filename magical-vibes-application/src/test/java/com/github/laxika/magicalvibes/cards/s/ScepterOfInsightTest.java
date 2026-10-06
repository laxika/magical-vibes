package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScepterOfInsight.class, Forest.class, GrizzlyBears.class})
class ScepterOfInsightTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability taps Scepter of Insight and consumes mana")
    void activatingTapsAndConsumesMana() {
        Permanent scepter = addReadyScepter(player1);
        addAbilityMana(player1);
        harness.setLibrary(player1, List.of(new Forest()));

        assertThat(scepter.isTapped()).isFalse();

        harness.activateAbility(player1, 0, null, null);

        assertThat(scepter.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Resolving ability draws a card for the controller only")
    void resolvingDrawsACard() {
        addReadyScepter(player1);
        addAbilityMana(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId()).get(1).getName()).isEqualTo("Forest");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate without the required blue mana")
    void cannotActivateWithoutBlueMana() {
        addReadyScepter(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate twice in a turn because it requires tap")
    void cannotActivateTwice() {
        addReadyScepter(player1);
        addAbilityMana(player1);
        addAbilityMana(player1);
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("A newly entered Scepter can activate immediately, drawing only on resolution")
    void canActivateOnTurnItEnters() {
        Permanent scepter = harness.addToBattlefieldAndReturn(player1, new ScepterOfInsight());
        addAbilityMana(player1);
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));

        harness.activateAbility(player1, 0, null, null);

        assertThat(scepter.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The ability resolves after Scepter leaves the battlefield")
    void drawsAfterSourceLeavesBattlefield() {
        Permanent scepter = addReadyScepter(player1);
        addAbilityMana(player1);
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(scepter);
        harness.setGraveyard(player1, List.of(scepter.getCard()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Blue mana alone does not pay the generic activation cost")
    void cannotActivateWithoutEnoughTotalMana() {
        Permanent scepter = addReadyScepter(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(scepter.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyScepter(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ScepterOfInsight());
        perm.setSummoningSick(false);
        return perm;
    }

    private void addAbilityMana(Player player) {
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);
    }


}
