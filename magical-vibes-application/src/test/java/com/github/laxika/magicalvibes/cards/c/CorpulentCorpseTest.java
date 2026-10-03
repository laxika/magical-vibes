package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HavenwoodWurm;
import com.github.laxika.magicalvibes.cards.s.StuffyDoll;
import com.github.laxika.magicalvibes.cards.t.TeferiMageOfZhalfir;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CorpulentCorpse.class, HavenwoodWurm.class, StuffyDoll.class, TeferiMageOfZhalfir.class})
class CorpulentCorpseTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Corpulent Corpse with five time counters")
    void suspendExilesWithFiveTimeCounters() {
        CorpulentCorpse card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The last suspend counter offers a free cast and grants haste")
    void lastCounterOffersFreeCastWithHaste() {
        CorpulentCorpse card = suspendCard();

        for (int i = 0; i < 5; i++) {
            advanceToUpkeep(player1);
            resolveAllTriggers();
        }

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, "Corpulent Corpse");
        assertThat(gqs.hasKeyword(gd, permanent, com.github.laxika.magicalvibes.model.Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Declining the suspend cast leaves Corpulent Corpse in exile")
    void decliningLastCounterCastLeavesCardExiled() {
        CorpulentCorpse card = suspendCard();

        for (int i = 0; i < 5; i++) {
            advanceToUpkeep(player1);
            resolveAllTriggers();
        }

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard() == card);
    }

    @Test
    @DisplayName("Fear prevents a nonblack nonartifact creature from blocking Corpulent Corpse")
    void fearPreventsNonblackNonartifactBlocker() {
        Permanent attacker = addCreatureReady(player1, new CorpulentCorpse());
        attacker.setAttacking(true);

        addCreatureReady(player2, new HavenwoodWurm());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(fear)");
    }

    @Test
    @DisplayName("Fear allows black and artifact creatures to block Corpulent Corpse")
    void fearAllowsBlackAndArtifactBlockers() {
        Permanent attacker = addCreatureReady(player1, new CorpulentCorpse());
        attacker.setAttacking(true);

        Permanent blackBlocker = addCreatureReady(player2, new CorpulentCorpse());
        Permanent artifactBlocker = addCreatureReady(player2, new StuffyDoll());

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(blackBlocker.getBlockingTargetIds()).containsExactly(attacker.getId());
        assertThat(artifactBlocker.getBlockingTargetIds()).containsExactly(attacker.getId());
    }

    @Test
    @DisplayName("Suspend removes one counter only during its owner's upkeep")
    void suspendCountsDownOnlyOnOwnersUpkeep() {
        CorpulentCorpse card = suspendCard();

        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 5);

        advanceToUpkeep(player1);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 5);
        resolveAllTriggers();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Removing the last counter puts a separate cast trigger on the stack")
    void lastCounterCreatesRespondableCastTrigger() {
        CorpulentCorpse card = suspendCard();
        for (int i = 0; i < 4; i++) {
            advanceToUpkeep(player1);
            resolveAllTriggers();
        }

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("Teferi allows suspending Corpulent Corpse during the opponent's upkeep")
    void grantedFlashAllowsSuspendOnOpponentsTurn() {
        addCreatureReady(player1, new TeferiMageOfZhalfir());
        CorpulentCorpse card = new CorpulentCorpse();
        harness.setHand(player1, List.of(card));
        advanceToUpkeep(player2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Teferi prevents casting Corpulent Corpse from suspend")
    void opposingTeferiPreventsSuspendCast() {
        CorpulentCorpse card = suspendCard();
        addCreatureReady(player2, new TeferiMageOfZhalfir());

        for (int i = 0; i < 5; i++) {
            advanceToUpkeep(player1);
            resolveAllTriggers();
        }
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        harness.assertNotOnBattlefield(player1, "Corpulent Corpse");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Without flash Corpulent Corpse cannot be suspended during upkeep")
    void suspendRequiresCreatureCastingTiming() {
        CorpulentCorpse card = new CorpulentCorpse();
        harness.setHand(player1, List.of(card));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("Casting Corpulent Corpse normally does not grant haste")
    void normalCastDoesNotGrantSuspendHaste() {
        CorpulentCorpse card = new CorpulentCorpse();
        harness.setHand(player1, List.of(card));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, "Corpulent Corpse");
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.HASTE)).isFalse();
        assertThat(permanent.isSummoningSick()).isTrue();
    }

    private CorpulentCorpse suspendCard() {
        CorpulentCorpse card = new CorpulentCorpse();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }
}
