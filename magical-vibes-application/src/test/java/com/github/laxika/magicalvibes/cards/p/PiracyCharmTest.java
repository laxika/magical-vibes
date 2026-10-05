package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.EssenceWarden;
import com.github.laxika.magicalvibes.cards.g.GiantDustwasp;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PiracyCharm.class, GiantDustwasp.class, Island.class, EssenceWarden.class})
class PiracyCharmTest extends BaseCardTest {

    @Test
    @DisplayName("Mode 0 gives a target creature islandwalk until end of turn")
    void grantsIslandwalkUntilEndOfTurn() {
        Permanent target = addCreatureReady(player2, new GiantDustwasp());

        cast(0, target.getId());

        assertThat(gqs.hasKeyword(gd, target, Keyword.ISLANDWALK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.ISLANDWALK)).isFalse();
    }

    @Test
    @DisplayName("Mode 1 gives a target creature +2/-1 until end of turn")
    void boostsTargetCreatureUntilEndOfTurn() {
        Permanent target = addCreatureReady(player2, new GiantDustwasp());

        cast(1, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Mode 2 makes a target player discard a card")
    void targetPlayerDiscards() {
        harness.setHand(player2, List.of(new GiantDustwasp()));

        cast(2, player2.getId());
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Giant Dustwasp");
    }

    @Test
    @DisplayName("Mode 2 can target the spell's controller")
    void targetControllerDiscards() {
        cast(2, player1.getId(), List.of(new PiracyCharm(), new GiantDustwasp()));
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Giant Dustwasp");
    }

    @Test
    @DisplayName("Mode 2 does nothing when the target player has no cards")
    void targetPlayerWithEmptyHandDoesNothing() {
        harness.setHand(player2, List.of());
        cast(2, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The targeted player chooses exactly one card to discard")
    void targetedPlayerChoosesOneCardFromMultipleCards() {
        harness.setHand(player2, List.of(new GiantDustwasp(), new EssenceWarden()));

        cast(2, player2.getId());
        harness.handleCardChosen(player2, 1);

        harness.assertInGraveyard(player2, "Essence Warden");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName).containsExactly("Giant Dustwasp");
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The toughness reduction puts a one-toughness creature into its owner's graveyard")
    void toughnessReductionKillsOneToughnessCreature() {
        Permanent target = addCreatureReady(player2, new EssenceWarden());

        cast(1, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        harness.assertInGraveyard(player2, "Essence Warden");
    }

    @Test
    @DisplayName("Mode 2 rejects a creature target")
    void discardModeRejectsCreatureTarget() {
        Permanent creature = addCreatureReady(player2, new GiantDustwasp());
        harness.setHand(player1, List.of(new PiracyCharm()));
        preparePiracyCharmCast();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 2, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Two power and toughness modifications accumulate on the same creature")
    void repeatedBoostsAccumulate() {
        Permanent target = addCreatureReady(player1, new GiantDustwasp());

        cast(1, target.getId());
        cast(1, target.getId());

        assertThat(target.getEffectivePower()).isEqualTo(7);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.ISLANDWALK)).isFalse();
    }

    @Test
    @DisplayName("Creature modes reject a player target")
    void creatureModesRejectPlayerTarget() {
        harness.setHand(player1, List.of(new PiracyCharm()));
        preparePiracyCharmCast();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creature modes reject a noncreature permanent target")
    void creatureModesRejectNoncreaturePermanentTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new PiracyCharm()));
        preparePiracyCharmCast();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Islandwalk prevents blocking while the defending player controls an Island")
    void islandwalkPreventsBlockingWithIsland() {
        Permanent attacker = addCreatureReady(player1, new GiantDustwasp());
        Permanent blocker = addCreatureReady(player2, new GiantDustwasp());
        harness.addToBattlefield(player2, new Island());

        cast(0, attacker.getId());

        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Islandwalk allows blocking while the defending player controls no Island")
    void islandwalkAllowsBlockingWithoutIsland() {
        Permanent attacker = addCreatureReady(player1, new GiantDustwasp());
        Permanent blocker = addCreatureReady(player2, new GiantDustwasp());

        cast(0, attacker.getId());

        attacker.setAttacking(true);
        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void cast(int mode, UUID targetId) {
        cast(mode, targetId, List.of(new PiracyCharm()));
    }

    private void cast(int mode, UUID targetId, List<Card> hand) {
        harness.setHand(player1, hand);
        preparePiracyCharmCast();
        harness.castModalInstant(player1, 0, mode, List.of(targetId));
        harness.passBothPriorities();
    }

    private void preparePiracyCharmCast() {
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}
