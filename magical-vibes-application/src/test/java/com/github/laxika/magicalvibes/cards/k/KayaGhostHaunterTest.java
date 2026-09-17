package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BlindHunter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KayaGhostHaunter.class, BlindHunter.class, GrizzlyBears.class, LightningBolt.class})
class KayaGhostHaunterTest extends BaseCardTest {

    @Test
    @DisplayName("0 exiles Kaya haunting the target and returns her when it leaves")
    void hauntsAndReturnsWhenTargetLeaves() {
        Permanent kaya = addReadyKaya(player1);
        Permanent bears = addReadyCreature(player2);

        harness.activateAbility(player1, battlefieldIndex(player1, kaya), 0, bears.getId());
        resolveStack();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(kaya);
        assertThat(gd.hauntingCardToPermanentId).containsEntry(kaya.getCard().getId(), bears.getId());
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(kaya.getCard().getId()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, bears));

        Permanent returnedKaya = findPermanent(player1, "Kaya, Ghost Haunter");
        assertThat(returnedKaya.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.hauntingCardToPermanentId).doesNotContainKey(kaya.getCard().getId());
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(kaya.getCard().getId()));
    }

    @Test
    @DisplayName("The damage emblem deals 3 damage to the haunted creature's owner")
    void damageEmblemDamagesHauntedCreatureOwner() {
        Permanent bears = addReadyCreature(player2);
        harness.setLife(player2, 20);

        hauntWithBlindHunter(bears);
        Permanent kaya = addReadyKaya(player1);
        harness.activateAbility(player1, battlefieldIndex(player1, kaya), 1, null, null);
        resolveStack();

        assertThat(gd.emblems).hasSize(1);
        advanceToNextUpkeep();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        resolveStack();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The control emblem gains control of a haunted creature")
    void controlEmblemGainsControlOfHauntedCreature() {
        Permanent bears = addReadyCreature(player2);

        hauntWithBlindHunter(bears);
        Permanent kaya = addReadyKaya(player1);
        harness.activateAbility(player1, battlefieldIndex(player1, kaya), 2, null, null);
        resolveStack();

        advanceToNextUpkeep();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        resolveStack();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, bears));
        assertThat(gd.controlEffectsFor(bears.getId())).isEmpty();
    }

    private void hauntWithBlindHunter(Permanent target) {
        Permanent hunter = addReadyPermanent(player1, new BlindHunter());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, java.util.List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, hunter.getId());
        resolveStack();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        resolveStack();
        assertThat(gd.hauntingCardToPermanentId).containsValue(target.getId());
    }

    private void resolveStack() {
        while (!gd.stack.isEmpty() && !gd.interaction.isAwaitingInput()) {
            harness.passBothPriorities();
        }
    }

    private void advanceToNextUpkeep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        gs.advanceStep(gd);
    }

    private Permanent addReadyKaya(Player player) {
        Permanent kaya = new Permanent(new KayaGhostHaunter());
        kaya.setCounterCount(CounterType.LOYALTY, 3);
        kaya.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(kaya);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return kaya;
    }

    private Permanent addReadyCreature(Player player) {
        return addReadyPermanent(player, new GrizzlyBears());
    }

    private Permanent addReadyPermanent(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent creature = new Permanent(card);
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
