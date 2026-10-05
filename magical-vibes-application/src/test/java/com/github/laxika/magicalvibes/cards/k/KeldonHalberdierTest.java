package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.i.IcatianCrier;
import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KeldonHalberdier.class, IcatianCrier.class, PithingNeedle.class})
class KeldonHalberdierTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Keldon Halberdier with four time counters")
    void suspendExilesWithFourTimeCounters() {
        KeldonHalberdier card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The last suspend counter offers a free cast and grants haste")
    void lastCounterOffersFreeCastWithHaste() {
        KeldonHalberdier card = suspendCard();

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        var permanent = findPermanent(player1, "Keldon Halberdier");
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Declining the suspend cast leaves Keldon Halberdier exiled without time counters")
    void decliningSuspendCastLeavesCardExiled() {
        KeldonHalberdier card = suspendCard();

        for (int i = 0; i < 4; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        harness.assertNotOnBattlefield(player1, "Keldon Halberdier");
    }

    @Test
    @DisplayName("First strike lets Keldon Halberdier survive combat with a 1/1 blocker")
    void firstStrikeDealsCombatDamageBeforeBlocker() {
        Permanent attacker = addCreatureReady(player1, new KeldonHalberdier());
        Permanent blocker = addCreatureReady(player2, new IcatianCrier());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
    }

    @Test
    @DisplayName("Suspend is unavailable during upkeep without flash permission")
    void suspendRequiresCreatureCastingTiming() {
        KeldonHalberdier card = new KeldonHalberdier();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be suspended at this time");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
    }

    @Test
    @DisplayName("Suspend removes one counter when the upkeep trigger resolves")
    void upkeepCounterRemovalUsesStack() {
        KeldonHalberdier card = suspendCard();

        advanceToUpkeep(player1);

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
    }

    @Test
    @DisplayName("An opponent's upkeep does not remove suspend counters")
    void opponentsUpkeepDoesNotRemoveCounters() {
        KeldonHalberdier card = suspendCard();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    @Test
    @DisplayName("Casting normally does not grant suspend haste")
    void normalCastDoesNotGrantHaste() {
        harness.setHand(player1, List.of(new KeldonHalberdier()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, "Keldon Halberdier");
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.HASTE)).isFalse();
        assertThat(permanent.isSummoningSick()).isTrue();
    }

    @Test
    @CardUsed({KeldonHalberdier.class, PithingNeedle.class})
    @DisplayName("Pithing Needle does not prevent the suspend special action")
    void pithingNeedleDoesNotPreventSuspend() {
        harness.setHand(player1, List.of(new PithingNeedle()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Keldon Halberdier");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        KeldonHalberdier card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
        assertThat(gd.stack).isEmpty();
    }

    private KeldonHalberdier suspendCard() {
        KeldonHalberdier card = new KeldonHalberdier();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }
}
