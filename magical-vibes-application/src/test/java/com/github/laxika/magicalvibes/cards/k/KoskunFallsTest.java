package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KoskunFallsTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep with no creature at all sacrifices Koskun Falls")
    void upkeepWithoutCreatureSacrificesFalls() {
        harness.addToBattlefield(player1, new KoskunFalls());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        // Nothing can be tapped, so accepting cannot pay the cost — the else half resolves.
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Koskun Falls");
        harness.assertInGraveyard(player1, "Koskun Falls");
    }

    @Test
    @DisplayName("Upkeep with only a tapped creature sacrifices Koskun Falls")
    void upkeepWithOnlyTappedCreatureSacrificesFalls() {
        harness.addToBattlefield(player1, new KoskunFalls());
        Permanent bear = addCreature(player1, new GrizzlyBears());
        bear.tap();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UPKEEP);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Koskun Falls");
        harness.assertInGraveyard(player1, "Koskun Falls");
    }

    @Test
    @DisplayName("Tapping an untapped creature at upkeep keeps Koskun Falls")
    void tappingCreatureKeepsFalls() {
        harness.addToBattlefield(player1, new KoskunFalls());
        addCreature(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Koskun Falls");
        assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Declining to tap sacrifices Koskun Falls and leaves the creature untapped")
    void decliningSacrificesFalls() {
        harness.addToBattlefield(player1, new KoskunFalls());
        addCreature(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Koskun Falls");
        harness.assertInGraveyard(player1, "Koskun Falls");
        assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent must pay {2} per attacking creature")
    void opponentPaysTwoPerAttacker() {
        harness.addToBattlefield(player1, new KoskunFalls());
        addCreature(player2, new GrizzlyBears());

        harness.addMana(player2, ManaColor.COLORLESS, 2);

        declareAttackers(player2, List.of(0));

        // The tax was paid; player1 has no creatures, so combat auto-resolves and the attack lands.
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Opponent cannot attack with only {1} available")
    void opponentCannotAttackWithoutFullPayment() {
        harness.addToBattlefield(player1, new KoskunFalls());
        addCreature(player2, new GrizzlyBears());

        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay attack tax");
    }

    private Permanent addCreature(Player player, Card card) {
        Permanent p = new Permanent(card);
        p.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(p);
        return p;
    }
}
