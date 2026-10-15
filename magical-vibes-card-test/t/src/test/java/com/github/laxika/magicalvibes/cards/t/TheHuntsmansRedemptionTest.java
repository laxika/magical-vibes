package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MomentOfValor;
import com.github.laxika.magicalvibes.cards.r.RestlessCottage;
import com.github.laxika.magicalvibes.cards.r.RoyalTreatment;
import com.github.laxika.magicalvibes.cards.u.UnassumingSage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheHuntsmansRedemption.class, Forest.class, UnassumingSage.class, MomentOfValor.class,
        RestlessCottage.class, RoyalTreatment.class})
class TheHuntsmansRedemptionTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I creates a 3/3 green Beast token")
    void chapterICreatesBeastToken() {
        addSaga(0);

        triggerChapter();
        harness.passBothPriorities();

        List<Permanent> beasts = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Beast"))
                .toList();
        assertThat(beasts).hasSize(1);
        assertThat(beasts.getFirst().getCard().getColor()).isEqualTo(com.github.laxika.magicalvibes.model.CardColor.GREEN);
        assertThat(beasts.getFirst().getEffectivePower()).isEqualTo(3);
        assertThat(beasts.getFirst().getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Chapter II may sacrifice a creature and search for a creature or basic land")
    void chapterIISacrificesAndSearchesCreatureOrBasicLand() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new UnassumingSage());
        addSaga(1);
        Card invalid = new MomentOfValor();
        Card land = new Forest();
        Card creature = new UnassumingSage();
        harness.setLibrary(player1, List.of(invalid, land, creature));

        triggerChapter();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).containsExactly(sacrificed.getId());
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactlyInAnyOrder(land, creature);
        assertThat(search.params().reveals()).isTrue();
        harness.handleCardChosen(player1, search.params().cards().indexOf(land));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificed.getCard());
        assertThat(gd.playerHands.get(player1.getId())).contains(land);
    }

    @Test
    @DisplayName("Chapter II can be declined without sacrificing or searching")
    void chapterIICanBeDeclined() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new UnassumingSage());
        addSaga(1);
        Card libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));

        triggerChapter();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(libraryCard);
    }

    @Test
    @DisplayName("Chapter III boosts up to two target creatures and gives them trample")
    void chapterIIIBoostsTwoCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new UnassumingSage());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new UnassumingSage());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new UnassumingSage());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        addSaga(2);

        triggerChapter();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(first.getId(), second.getId(), third.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(land.getId());

        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, first, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, second, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, third)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, third, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Chapter I triggers when the Saga enters")
    void chapterITriggersOnEntry() {
        harness.castFromHand(player1, new TheHuntsmansRedemption(), "{2}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Beast")))
                .hasSize(1);
    }

    @Test
    @DisplayName("Chapter II searches during the same resolution as the sacrifice")
    void chapterIISearchIsNotASeparateTrigger() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new UnassumingSage());
        addSaga(1);
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.setHand(player2, List.of(new MomentOfValor()));

        triggerChapter();
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.stack).isEmpty();
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).contains(land);
    }

    @Test
    @DisplayName("Chapter II cannot sacrifice an opponent's creature or a noncreature")
    void chapterIISacrificeIsRestrictedToControlledCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new UnassumingSage());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new UnassumingSage());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        addSaga(1);

        triggerChapter();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).containsExactly(ownCreature.getId())
                .doesNotContain(opposingCreature.getId(), land.getId());
    }

    @Test
    @DisplayName("Chapter II excludes nonbasic lands and may fail to find")
    void chapterIISearchMayFailToFind() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new UnassumingSage());
        addSaga(1);
        Card valid = new UnassumingSage();
        Card nonbasic = new RestlessCottage();
        harness.setLibrary(player1, List.of(valid, nonbasic));

        triggerChapter();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(valid);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature.getCard());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(valid, nonbasic);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(valid, nonbasic);
    }

    @Test
    @DisplayName("Chapter II puts the searched creature into hand")
    void chapterIICanFindCreature() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new UnassumingSage());
        addSaga(1);
        Card creature = new UnassumingSage();
        harness.setLibrary(player1, List.of(creature));

        triggerChapter();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificed.getCard());
    }

    @Test
    @DisplayName("Chapter II cannot search when no creature can be sacrificed")
    void chapterIIWithNoCreatureDoesNotSearch() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new UnassumingSage());
        harness.addToBattlefield(player1, new Forest());
        addSaga(1);
        Card libraryCard = new Forest();
        harness.setLibrary(player1, List.of(libraryCard));

        triggerChapter();
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(libraryCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingCreature);
    }

    @Test
    @DisplayName("Chapter III may choose zero targets and then the Saga is sacrificed")
    void chapterIIICanChooseNoTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new UnassumingSage());
        Permanent saga = addSaga(2);

        triggerChapter();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(saga.getCard());
    }

    @Test
    @DisplayName("Chapter III may target one opposing creature and its effects expire at end of turn")
    void chapterIIICanChooseOneOpposingCreatureAndEffectsExpire() {
        Permanent unchosen = harness.addToBattlefieldAndReturn(player1, new UnassumingSage());
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new UnassumingSage());
        addSaga(2);

        triggerChapter();
        harness.handlePermanentChosen(player1, chosen.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, chosen)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, chosen)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, chosen, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, unchosen)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, unchosen, Keyword.TRAMPLE)).isFalse();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, chosen)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, chosen)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, chosen, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Chapter III still affects its remaining target when the other leaves")
    void chapterIIIPartiallyResolves() {
        Permanent removed = harness.addToBattlefieldAndReturn(player1, new UnassumingSage());
        Permanent remaining = harness.addToBattlefieldAndReturn(player1, new UnassumingSage());
        addSaga(2);

        triggerChapter();
        harness.handlePermanentChosen(player1, removed.getId());
        harness.handlePermanentChosen(player1, remaining.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, removed));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, remaining)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, remaining)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, remaining, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Chapter III cannot target an opposing creature with hexproof")
    void chapterIIIExcludesOpposingHexproofCreature() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player2, new UnassumingSage());
        Permanent legalCreature = harness.addToBattlefieldAndReturn(player1, new UnassumingSage());
        harness.setHand(player2, List.of(new RoyalTreatment()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castInstant(player2, 0, protectedCreature.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, protectedCreature, Keyword.HEXPROOF)).isTrue();
        addSaga(2);

        triggerChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(legalCreature.getId())
                .doesNotContain(protectedCreature.getId());
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheHuntsmansRedemption());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
    }
}
