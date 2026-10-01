package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HavenwoodWurm;
import com.github.laxika.magicalvibes.cards.s.StuffyDoll;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CorpulentCorpse.class, HavenwoodWurm.class, StuffyDoll.class})
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
            harness.passBothPriorities();
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
            harness.passBothPriorities();
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

    private CorpulentCorpse suspendCard() {
        CorpulentCorpse card = new CorpulentCorpse();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }
}
