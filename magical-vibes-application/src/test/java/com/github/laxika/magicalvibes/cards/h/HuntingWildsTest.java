package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EssenceWarden;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HuntingWilds.class, Forest.class, Island.class})
class HuntingWildsTest extends BaseCardTest {

    @Test
    void searchesForUpToTwoForests() {
        harness.setHand(player1, List.of(new HuntingWilds()));
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof Forest)
                .singleElement()
                .matches(permanent -> permanent.isTapped());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof Island);
    }

    @Test
    void mayFindNoForests() {
        harness.setHand(player1, List.of(new HuntingWilds()));
        harness.setLibrary(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof Forest);
        assertThat(gd.playerDecks.get(player1.getId()))
                .singleElement()
                .isInstanceOf(Island.class);
    }

    @Test
    void kickedSpellMakesFetchedForestsPermanentHastyThreeThreeGreenCreaturesAndUntapsThem() {
        Permanent existingForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        existingForest.tap();
        harness.setHand(player1, List.of(new HuntingWilds()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Island()));
        harness.addMana(player1, ManaColor.GREEN, 8);
        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        List<Permanent> forests = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof Forest)
                .toList();
        assertThat(forests).hasSize(3);
        assertThat(forests).allMatch(permanent -> gqs.isLand(gd, permanent));
        assertThat(forests).filteredOn(permanent -> gqs.isCreature(gd, permanent)).hasSize(2);
        assertThat(forests).filteredOn(permanent -> !gqs.isCreature(gd, permanent)).hasSize(1);
        assertThat(existingForest)
                .matches(Permanent::isTapped)
                .matches(permanent -> !gqs.isCreature(gd, permanent));
        assertThat(forests).filteredOn(permanent -> gqs.isCreature(gd, permanent))
                .allMatch(permanent -> !permanent.isTapped())
                .allMatch(permanent -> gqs.hasColor(gd, permanent, CardColor.GREEN))
                .allMatch(permanent -> gqs.getEffectivePower(gd, permanent) == 3)
                .allMatch(permanent -> gqs.getEffectiveToughness(gd, permanent) == 3)
                .allMatch(permanent -> gqs.hasKeyword(gd, permanent, Keyword.HASTE));
    }

    @Test
    void unkickedSpellPutsBothForestsOntoBattlefieldTappedWithoutAnimatingThem() {
        harness.setHand(player1, List.of(new HuntingWilds()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Island()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .hasSize(2)
                .allMatch(Permanent::isTapped)
                .allMatch(permanent -> !gqs.isCreature(gd, permanent));
    }

    @Test
    void kickedSpellMayDeclineToFindEvenWhenForestsAreAvailable() {
        Permanent existingForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        existingForest.tap();
        harness.setHand(player1, List.of(new HuntingWilds()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 8);
        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(existingForest);
        assertThat(existingForest.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, existingForest)).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    void kickedAnimationAndHasteSurviveTurnCleanup() {
        harness.setHand(player1, List.of(new HuntingWilds()));
        harness.setLibrary(player1, List.of(new Forest(), new Island()));
        harness.addMana(player1, ManaColor.GREEN, 8);
        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(3);
        assertThat(gqs.hasColor(gd, forest, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();
    }

    @Test
    @CardUsed(EssenceWarden.class)
    void fetchedForestsEnterAsLandsBeforeBecomingCreatures() {
        harness.addToBattlefield(player1, new EssenceWarden());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new HuntingWilds()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Island()));
        harness.addMana(player1, ManaColor.GREEN, 8);
        harness.castKickedSorcery(player1, 0);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }
}
