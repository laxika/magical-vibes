package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SamuraiOfThePaleCurtainTest extends BaseCardTest {

    @Test
    @DisplayName("Bushido gives Samurai +1/+1 when it becomes blocked")
    void becomesBlockedGetsBushidoBonus() {
        harness.addToBattlefield(player1, new SamuraiOfThePaleCurtain());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent samurai = findPermanent(player1, "Samurai of the Pale Curtain");
        samurai.setSummoningSick(false);
        samurai.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, samurai)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, samurai)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bushido gives Samurai +1/+1 when it blocks")
    void blocksGetsBushidoBonus() {
        harness.addToBattlefield(player1, new SamuraiOfThePaleCurtain());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent samurai = findPermanent(player1, "Samurai of the Pale Curtain");
        samurai.setSummoningSick(false);
        Permanent attacker = findPermanent(player2, "Grizzly Bears");
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, samurai)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, samurai)).isEqualTo(3);
    }

    @Test
    @DisplayName("Permanents put into graveyards are exiled instead")
    void permanentsAreExiledInsteadOfEnteringGraveyards() {
        harness.addToBattlefield(player1, new SamuraiOfThePaleCurtain());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        destroyWithShock(player1, "Grizzly Bears");
        destroyWithShock(player2, "Grizzly Bears");
        destroyWithShock(player1, "Samurai of the Pale Curtain");

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears")
                        || card.getName().equals("Samurai of the Pale Curtain"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Samurai of the Pale Curtain"));
    }

    @Test
    @DisplayName("A permanent card in another zone still goes to the graveyard")
    void nonPermanentCardsAreNotExiled() {
        harness.addToBattlefield(player1, new SamuraiOfThePaleCurtain());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Shock"));
    }

    private void destroyWithShock(com.github.laxika.magicalvibes.model.Player targetOwner, String targetName) {
        UUID targetId = harness.getPermanentId(
                targetOwner,
                targetName);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
