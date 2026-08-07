package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GruulRagebeastTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature you control entering fights the chosen opponent creature")
    void allyCreatureEnteringFightsChosenCreature() {
        harness.addToBattlefieldAndReturn(player1, new GruulRagebeast());
        Permanent opponentGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castGrizzlyBears(player1);
        harness.passBothPriorities(); // resolve the creature spell → trigger awaits target

        harness.handlePermanentChosen(player1, opponentGiant.getId());
        harness.passBothPriorities(); // resolve the fight

        // The 2/2 Bears deals 2 to the 3/3 Giant and takes 3 back, so only the Bears dies.
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gqs.findPermanentById(gd, opponentGiant.getId())).isNotNull();
        assertThat(opponentGiant.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Gruul Ragebeast entering triggers on itself and fights as the entering creature")
    void ownEntryTriggersAndRagebeastFights() {
        Permanent opponentGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.setHand(player1, List.of(new GruulRagebeast()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the creature spell → trigger awaits target

        harness.handlePermanentChosen(player1, opponentGiant.getId());
        harness.passBothPriorities(); // resolve the fight

        // The 6/6 Ragebeast kills the 3/3 Giant and takes 3 damage itself.
        harness.assertInGraveyard(player2, "Hill Giant");
        Permanent ragebeast = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Gruul Ragebeast"))
                .findFirst().orElseThrow();
        assertThat(ragebeast.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("The trigger cannot target a creature you control")
    void cannotTargetOwnCreature() {
        harness.addToBattlefieldAndReturn(player1, new GruulRagebeast());
        Permanent ownGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castGrizzlyBears(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownGiant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("With no creature an opponent controls the trigger is removed, no choice is asked")
    void noOpponentCreatureMeansNoTrigger() {
        harness.addToBattlefieldAndReturn(player1, new GruulRagebeast());

        castGrizzlyBears(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    private void castGrizzlyBears(com.github.laxika.magicalvibes.model.Player player) {
        harness.setHand(player, List.of(new GrizzlyBears()));
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.castCreature(player, 0);
    }
}
