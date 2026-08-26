package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GuardianProjectTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card for an entering creature with a unique name")
    void drawsForUniqueName() {
        addGuardian(player1);
        castBears(player1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger when another controlled creature has the same name")
    void doesNotTriggerForSameNameOnBattlefield() {
        addGuardian(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        castBears(player1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when a creature card in the graveyard has the same name")
    void doesNotTriggerForSameNameInGraveyard() {
        addGuardian(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castBears(player1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Rechecks the same-name condition when the trigger resolves")
    void doesNotDrawIfEnteringCreatureDiesBeforeResolution() {
        addGuardian(player1);
        Card entering = new GrizzlyBears();
        harness.setHand(player1, List.of(entering));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        UUID enteringPermanentId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, enteringPermanentId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(entering.getId()));
    }

    private void castBears(Player player) {
        Card bears = new GrizzlyBears();
        harness.setHand(player, List.of(bears));
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.castCreature(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addGuardian(Player player) {
        Permanent guardian = new Permanent(new GuardianProject());
        guardian.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(guardian);
    }
}
