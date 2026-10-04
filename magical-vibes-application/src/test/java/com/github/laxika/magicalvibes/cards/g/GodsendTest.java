package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BassaraTowerArcher;
import com.github.laxika.magicalvibes.cards.c.ConsignToDust;
import com.github.laxika.magicalvibes.cards.p.PensiveMinotaur;
import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Godsend.class, PensiveMinotaur.class, GuardianOfTheGateless.class,
        BassaraTowerArcher.class, ConsignToDust.class, SongOfTheDryads.class})
class GodsendTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +3/+3")
    void equippedCreatureGetsPlusThreePlusThree() {
        Permanent creature = addCreatureReady(player1, new PensiveMinotaur());
        Permanent godsend = harness.addToBattlefieldAndReturn(player1, new Godsend());
        godsend.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
    }

    @Test
    @DisplayName("Becoming blocked by multiple creatures creates one may trigger")
    void becomesBlockedExilesChosenBlocker() {
        Permanent creature = addCreatureReady(player1, new PensiveMinotaur());
        Permanent godsend = harness.addToBattlefieldAndReturn(player1, new Godsend());
        godsend.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent firstBlocker = addCreatureReady(player2, new PensiveMinotaur());
        Permanent secondBlocker = addCreatureReady(player2, new PensiveMinotaur());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(gd.stack).filteredOn(se -> se.getCard().getName().equals("Godsend")).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, firstBlocker.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(secondBlocker)
                .doesNotContain(firstBlocker);
        assertThat(gd.getCardsExiledByPermanent(godsend.getId()))
                .containsExactly(firstBlocker.getOriginalCard());
    }

    @Test
    @DisplayName("A Godsend block trigger fires once when one creature blocks multiple attackers")
    void blocksMultipleCreaturesExileOneChosenAttacker() {
        Permanent blocker = addCreatureReady(player2, new GuardianOfTheGateless());
        Permanent godsend = harness.addToBattlefieldAndReturn(player2, new Godsend());
        godsend.setAttachedTo(blocker.getId());

        Permanent firstAttacker = addCreatureReady(player1, new PensiveMinotaur());
        Permanent secondAttacker = addCreatureReady(player1, new PensiveMinotaur());
        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)));

        assertThat(gd.stack).filteredOn(se -> se.getCard().getName().equals("Godsend")).hasSize(1);
    }

    @Test
    @DisplayName("Declining Godsend's may ability leaves combat creatures in place")
    void decliningMayAbilityDoesNotExile() {
        Permanent creature = addCreatureReady(player1, new PensiveMinotaur());
        Permanent godsend = harness.addToBattlefieldAndReturn(player1, new Godsend());
        godsend.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new PensiveMinotaur());

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(gd.getCardsExiledByPermanent(godsend.getId())).isEmpty();
    }

    @Test
    void equipPaysThreeManaAndAttachesToOwnCreature() {
        Permanent godsend = harness.addToBattlefieldAndReturn(player1, new Godsend());
        Permanent creature = addCreatureReady(player1, new PensiveMinotaur());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(godsend.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        Permanent godsend = harness.addToBattlefieldAndReturn(player1, new Godsend());
        Permanent creature = addCreatureReady(player2, new PensiveMinotaur());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(godsend.getAttachedTo()).isNull();
    }

    @Test
    void opponentCannotCastSameNameAsExiledCard() {
        exileMinotaurWithGodsend();
        prepareMinotaurCast(player2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void godsendControllerCanCastSameNameAsExiledCard() {
        exileMinotaurWithGodsend();
        prepareMinotaurCast(player1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void cardLeavingExileEndsNameRestriction() {
        Permanent godsend = exileMinotaurWithGodsend();
        gd.removeFromExile(gd.getCardsExiledByPermanent(godsend.getId()).getFirst().getId());
        prepareMinotaurCast(player2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void newGodsendDoesNotInheritOldGodsendsNameRestriction() {
        Permanent godsend = exileMinotaurWithGodsend();
        harness.setHand(player1, List.of(new ConsignToDust()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0, List.of(godsend.getId()));
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Godsend");
        harness.addToBattlefield(player1, godsend.getOriginalCard());
        prepareMinotaurCast(player2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void exileDoesNotTargetHexproofBlocker() {
        Permanent attacker = addCreatureReady(player1, new PensiveMinotaur());
        Permanent godsend = harness.addToBattlefieldAndReturn(player1, new Godsend());
        godsend.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BassaraTowerArcher());
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.getCardsExiledByPermanent(godsend.getId())).containsExactly(blocker.getOriginalCard());
    }

    @Test
    void blockingCreatureExilesAttacker() {
        Permanent attacker = addCreatureReady(player1, new PensiveMinotaur());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new PensiveMinotaur());
        Permanent godsend = harness.addToBattlefieldAndReturn(player2, new Godsend());
        godsend.setAttachedTo(blocker.getId());
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.getCardsExiledByPermanent(godsend.getId())).containsExactly(attacker.getOriginalCard());
    }

    @Test
    void godsendControllerChoosesEvenWhenOpponentControlsEquippedCreature() {
        Permanent attacker = addCreatureReady(player1, new PensiveMinotaur());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new PensiveMinotaur());
        Permanent godsend = harness.addToBattlefieldAndReturn(player1, new Godsend());
        godsend.setAttachedTo(blocker.getId());
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.getCardsExiledByPermanent(godsend.getId())).containsExactly(attacker.getOriginalCard());
    }

    @Test
    void triggerStillExilesAfterGodsendIsDestroyed() {
        Permanent attacker = addCreatureReady(player1, new PensiveMinotaur());
        Permanent godsend = harness.addToBattlefieldAndReturn(player1, new Godsend());
        godsend.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new PensiveMinotaur());
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.setHand(player2, List.of(new ConsignToDust()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.castInstant(player2, 0, List.of(godsend.getId()));
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Godsend");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(blocker.getOriginalCard());
        prepareMinotaurCast(player2);
        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void restrictionAllowsOpponentToCastDifferentName() {
        exileMinotaurWithGodsend();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new BassaraTowerArcher()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void canExileBlockerThatStoppedBeingCreatureBeforeResolution() {
        Permanent attacker = addCreatureReady(player1, new PensiveMinotaur());
        Permanent godsend = harness.addToBattlefieldAndReturn(player1, new Godsend());
        godsend.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new PensiveMinotaur());
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        Permanent song = harness.addToBattlefieldAndReturn(player1, new SongOfTheDryads());
        song.setAttachedTo(blocker.getId());
        assertThat(gqs.isCreature(gd, blocker)).isFalse();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.getCardsExiledByPermanent(godsend.getId())).containsExactly(blocker.getOriginalCard());
    }

    private Permanent exileMinotaurWithGodsend() {
        Permanent attacker = addCreatureReady(player1, new PensiveMinotaur());
        Permanent godsend = harness.addToBattlefieldAndReturn(player1, new Godsend());
        godsend.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new PensiveMinotaur());
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.getCardsExiledByPermanent(godsend.getId())).containsExactly(blocker.getOriginalCard());
        return godsend;
    }

    private void prepareMinotaurCast(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player, List.of(new PensiveMinotaur()));
        harness.addMana(player, ManaColor.RED, 3);
    }
}
