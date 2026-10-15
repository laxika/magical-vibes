package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeylineOfAnticipation;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DinosaursOnASpaceship.class, DrowsingTyrannodon.class, GrizzlyBears.class, LeylineOfAnticipation.class})
class DinosaursOnASpaceshipTest extends BaseCardTest {

    @Test
    void boostsOtherDinosaursAndGrantsThemVigilanceAndTrample() {
        harness.addToBattlefield(player1, new DinosaursOnASpaceship());
        Permanent dinosaur = harness.addToBattlefieldAndReturn(player1, new DrowsingTyrannodon());
        Permanent nonDinosaur = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentDinosaur = harness.addToBattlefieldAndReturn(player2, new DrowsingTyrannodon());

        assertThat(gqs.getEffectivePower(gd, dinosaur)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, dinosaur)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, dinosaur, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, dinosaur, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, nonDinosaur)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, nonDinosaur, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponentDinosaur)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, opponentDinosaur, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void suspendsFromHandWithFourTimeCounters() {
        DinosaursOnASpaceship card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
    }

    @Test
    void createsFlyingHastyMulticolorDinosaurWhenTimeCounterIsRemoved() {
        DinosaursOnASpaceship card = suspendCard();

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            resolveAllTriggers();
        }

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(countPermanents(player1, "Dinosaur")).isEqualTo(4);

        Permanent token = findPermanent(player1, "Dinosaur");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveColors(gd, token)).containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, token)).contains(CardSubtype.DINOSAUR);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
    }

    @Test
    void twoCopiesBoostEachOtherButNotThemselves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new DinosaursOnASpaceship());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new DinosaursOnASpaceship());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(8);
    }

    @Test
    void createsOneTokenPerCounterThenCastsWithHasteAndBoostsAllFourTokens() {
        DinosaursOnASpaceship card = suspendCard();

        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
        assertThat(countPermanents(player1, "Dinosaur")).isZero();

        for (int i = 1; i <= 3; i++) {
            advanceToUpkeep(player1);
            resolveAllTriggers();
            assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4 - i);
            assertThat(countPermanents(player1, "Dinosaur")).isEqualTo(i);
        }

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        Permanent spaceship = findPermanent(player1, "Dinosaurs on a Spaceship");
        assertThat(gqs.getEffectivePower(gd, spaceship)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, spaceship, Keyword.HASTE)).isTrue();
        assertThat(findPermanents(player1, "Dinosaur")).hasSize(4).allSatisfy(token -> {
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
            assertThat(gqs.hasKeyword(gd, token, Keyword.TRAMPLE)).isTrue();
        });
    }

    @Test
    void canSuspendDuringOpponentsTurnWhenSpellsCanBeCastAsThoughTheyHadFlash() {
        harness.addToBattlefield(player1, new LeylineOfAnticipation());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.ensurePriority(player1);

        DinosaursOnASpaceship card = suspendCard();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
        assertThat(gd.stack).isEmpty();
    }

    private DinosaursOnASpaceship suspendCard() {
        DinosaursOnASpaceship card = new DinosaursOnASpaceship();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }
}
