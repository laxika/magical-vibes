package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FavorOfJukai;
import com.github.laxika.magicalvibes.cards.a.AncestralKatana;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KodamaOfTheWestTree.class, GrizzlyBears.class, Forest.class,
        FavorOfJukai.class, AncestralKatana.class})
class KodamaOfTheWestTreeTest extends BaseCardTest {

    @Test
    @DisplayName("Modified creatures you control have trample")
    void modifiedCreaturesHaveTrample() {
        harness.addToBattlefield(player1, new KodamaOfTheWestTree());
        Permanent modifiedBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent unmodifiedBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        modifiedBears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, modifiedBears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, unmodifiedBears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("A modified creature dealing combat damage searches for a basic land tapped")
    void modifiedCreatureCombatDamageSearchesForBasicLand() {
        harness.addToBattlefield(player1, new KodamaOfTheWestTree());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));

        declareAttackers(List.of(1));
        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).singleElement()
                .satisfies(card -> assertThat(card.getName()).isEqualTo("Forest"));

        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
    }

    @Test
    @DisplayName("An unmodified creature dealing combat damage does not search")
    void unmodifiedCreatureCombatDamageDoesNotSearch() {
        harness.addToBattlefield(player1, new KodamaOfTheWestTree());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(List.of(1));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Forest")).isEmpty();
    }

    @Test
    void onlyControllerOwnedAurasModifyCreatures() {
        Permanent kodama = harness.addToBattlefieldAndReturn(player1, new KodamaOfTheWestTree());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new FavorOfJukai());
        aura.setAttachedTo(kodama.getId());

        assertThat(gqs.hasKeyword(gd, kodama, Keyword.TRAMPLE)).isFalse();

        gd.playerBattlefields.get(player2.getId()).remove(aura);
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.hasKeyword(gd, kodama, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void opponentControlledEquipmentModifiesKodamaAndTriggersSearch() {
        Permanent kodama = harness.addToBattlefieldAndReturn(player1, new KodamaOfTheWestTree());
        kodama.setSummoningSick(false);
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new AncestralKatana());
        equipment.setAttachedTo(kodama.getId());
        harness.setLibrary(player1, List.of(new Forest()));

        assertThat(gqs.hasKeyword(gd, kodama, Keyword.TRAMPLE)).isTrue();

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
    }

    @Test
    void modifiedOpponentCreatureDoesNotGainTrampleOrTriggerSearch() {
        harness.addToBattlefield(player1, new KodamaOfTheWestTree());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponent.setSummoningSick(false);
        opponent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));

        assertThat(gqs.hasKeyword(gd, opponent, Keyword.TRAMPLE)).isFalse();
        declareAttackers(player2, List.of(0));
        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Forest")).isEmpty();
        assertThat(findPermanents(player2, "Forest")).isEmpty();
    }

    @Test
    void eachModifiedCreatureProducesASeparateSearch() {
        Permanent kodama = harness.addToBattlefieldAndReturn(player1, new KodamaOfTheWestTree());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        kodama.setSummoningSick(false);
        attacker.setSummoningSick(false);
        kodama.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        declareAttackers(List.of(0, 1));
        resolveCombat();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanents(player1, "Forest")).hasSize(2).allSatisfy(land ->
                assertThat(land.isTapped()).isTrue());
    }

    @Test
    void auraModifiedCreatureTriggersSearch() {
        Permanent kodama = harness.addToBattlefieldAndReturn(player1, new KodamaOfTheWestTree());
        kodama.setSummoningSick(false);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FavorOfJukai());
        aura.setAttachedTo(kodama.getId());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
    }

    @Test
    void removingLastCounterRemovesTrample() {
        Permanent kodama = harness.addToBattlefieldAndReturn(player1, new KodamaOfTheWestTree());
        kodama.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, kodama, Keyword.TRAMPLE)).isTrue();

        kodama.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(gqs.hasKeyword(gd, kodama, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void searchCanFailToFindEvenWhenBasicLandIsAvailable() {
        Permanent kodama = harness.addToBattlefieldAndReturn(player1, new KodamaOfTheWestTree());
        kodama.setSummoningSick(false);
        kodama.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanents(player1, "Forest")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
