package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AcererakTheArchlich;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GnollHunter;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Dungeon;
import com.github.laxika.magicalvibes.model.DungeonProgress;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EllywickTumblestrum.class, AcererakTheArchlich.class, Forest.class,
        GnollHunter.class, Island.class})
class EllywickTumblestrumTest extends BaseCardTest {

    @Test
    @DisplayName("+1 ventures into a dungeon")
    void plusOneVenture() {
        addReadyEllywick(player1, 4);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lost Mine of Phandelver");
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));
    }

    @Test
    @DisplayName("-2 puts a creature into hand and gains 3 life for a legendary creature")
    void minusTwoLegendaryCreatureGainsLife() {
        addReadyEllywick(player1, 4);
        AcererakTheArchlich legendaryCreature = new AcererakTheArchlich();
        setTopCards(List.of(new Forest(), new Island(), new GnollHunter(),
                new Forest(), new Island(), legendaryCreature));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(legendaryCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(legendaryCreature);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("-2 does not gain life for a nonlegendary creature")
    void minusTwoNonlegendaryCreatureDoesNotGainLife() {
        addReadyEllywick(player1, 4);
        GnollHunter creature = new GnollHunter();
        setTopCards(List.of(new Forest(), new Island(), new Forest(),
                new Island(), new Forest(), creature));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("-7 grants trample, haste, and a dynamic boost for completed dungeons")
    void minusSevenCreatesDynamicEmblem() {
        addReadyEllywick(player1, 7);
        Permanent bear = addCreatureReady(player1, new GnollHunter());
        gd.recordCompletedDungeon(player1.getId(), Dungeon.LOST_MINE_OF_PHANDELVER);
        gd.recordCompletedDungeon(player1.getId(), Dungeon.TOMB_OF_ANNIHILATION);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.HASTE)).isTrue();

        gd.recordCompletedDungeon(player1.getId(), Dungeon.DUNGEON_OF_THE_MAD_MAGE);

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(8);
    }

    @Test
    void minusTwoMayDeclineLegendaryCreatureAndKeepsUntouchedCardsOnTop() {
        addReadyEllywick(player1, 4);
        AcererakTheArchlich creature = new AcererakTheArchlich();
        List<Card> lookedAt = List.of(creature, new Forest(), new Island(),
                new Forest(), new Island(), new Forest());
        Island untouched = new Island();
        ArrayList<Card> library = new ArrayList<>(lookedAt);
        library.add(untouched);
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 7))
                .containsExactlyInAnyOrderElementsOf(lookedAt);
    }

    @Test
    void minusTwoWithShortLibraryAndNoCreaturesReturnsAllCards() {
        addReadyEllywick(player1, 4);
        List<Card> library = List.of(new Forest(), new Island());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void emblemGrantsKeywordsWithoutCompletedDungeonsAndCountsDistinctNamesOnly() {
        addReadyEllywick(player1, 7);
        Permanent ownCreature = addCreatureReady(player1, new GnollHunter());
        Permanent opposingCreature = addCreatureReady(player2, new GnollHunter());
        gd.recordCompletedDungeon(player2.getId(), Dungeon.TOMB_OF_ANNIHILATION);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.HASTE)).isFalse();

        gd.recordCompletedDungeon(player1.getId(), Dungeon.LOST_MINE_OF_PHANDELVER);
        gd.recordCompletedDungeon(player1.getId(), Dungeon.LOST_MINE_OF_PHANDELVER);
        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new GnollHunter());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.HASTE)).isTrue();
    }

    @Test
    void plusOneAdvancesExistingDungeonAlongChosenPath() {
        addReadyEllywick(player1, 4);
        gd.playerDungeonProgress.put(player1.getId(),
                new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 0));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Goblin Lair");
        harness.passBothPriorities();

        assertThat(gd.playerDungeonProgress.get(player1.getId()))
                .isEqualTo(new DungeonProgress(Dungeon.LOST_MINE_OF_PHANDELVER, 1));
        assertThat(countPermanents(player1, "Goblin")).isEqualTo(1);
    }

    @Test
    void minusTwoWithEmptyLibraryDoesNothing() {
        addReadyEllywick(player1, 4);
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    private void setTopCards(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }

    private Permanent addReadyEllywick(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new EllywickTumblestrum());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
