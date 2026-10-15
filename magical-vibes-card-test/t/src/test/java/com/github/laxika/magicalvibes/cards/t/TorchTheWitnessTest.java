package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TorchTheWitness.class, GrizzlyBears.class, TinStreetGossip.class})
class TorchTheWitnessTest extends BaseCardTest {

    @Test
    @DisplayName("Deals twice X damage and investigates when the damage is excess")
    void investigatesForExcessDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new TorchTheWitness()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 2, target.getId());

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not investigate when no excess damage is dealt")
    void doesNotInvestigateWithoutExcessDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new TorchTheWitness()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, 1, target.getId());

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Rejects a noncreature target")
    void rejectsNonCreatureTarget() {
        harness.setHand(player1, List.of(new TorchTheWitness()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("X zero deals no damage and does not investigate")
    void zeroXDoesNotInvestigate() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TinStreetGossip());
        harness.setHand(player1, List.of(new TorchTheWitness()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Tin Street Gossip");
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("A surviving creature takes twice X damage without investigation")
    void nonlethalDamageDoesNotInvestigate() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TinStreetGossip());
        harness.setHand(player1, List.of(new TorchTheWitness()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 1, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Tin Street Gossip");
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Damage already marked reduces the amount needed for excess damage")
    void investigatesConsideringPriorDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TinStreetGossip());
        harness.setHand(player1, List.of(new TorchTheWitness(), new TorchTheWitness()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 1, target.getId());
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        harness.castAndResolveSorcery(player1, 0, 2, target.getId());

        harness.assertNotOnBattlefield(player2, "Tin Street Gossip");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    @DisplayName("Does not investigate when the only target leaves before resolution")
    void missingTargetDoesNotInvestigate() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new TinStreetGossip());
        harness.setHand(player1, List.of(new TorchTheWitness()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 3, target.getId());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }
}
