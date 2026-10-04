package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KithkinBrinefarer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrowOldTogether.class, Forest.class, GrizzlyBears.class, KithkinBrinefarer.class})
class GrowOldTogetherTest extends BaseCardTest {

    @Test
    void seeksUpToTwoCreaturesFromTopTenAndPerpetuallyBoostsCreatureCardsInHand() {
        GrizzlyBears handBear = new GrizzlyBears();
        GrizzlyBears firstSoughtBear = new GrizzlyBears();
        GrizzlyBears secondSoughtBear = new GrizzlyBears();
        GrizzlyBears belowTopTenBear = new GrizzlyBears();
        List<Card> library = new ArrayList<>(List.of(firstSoughtBear));
        for (int i = 0; i < 8; i++) {
            library.add(new Forest());
        }
        library.add(secondSoughtBear);
        library.add(belowTopTenBear);

        harness.setHand(player1, List.of(new GrowOldTogether(), handBear));
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .contains(handBear, firstSoughtBear, secondSoughtBear)
                .doesNotContain(belowTopTenBear);
        assertThat(gd.playerDecks.get(player1.getId()))
                .contains(belowTopTenBear)
                .hasSize(9);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        List<Permanent> creatures = gd.playerBattlefields.get(player1.getId());
        assertThat(creatures).hasSize(2);
        assertThat(creatures).allSatisfy(permanent -> {
            assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(3);
        });
    }

    @Test
    void seeksTheOnlyCreatureFromAShortLibraryAndBoostsIt() {
        GrizzlyBears bear = new GrizzlyBears();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest, bear));

        harness.castFromHand(player1, new GrowOldTogether(), "{1}{G}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bear);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent creature = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void seeksExactlyTwoWhenMoreThanTwoCreaturesAreAvailable() {
        List<Card> bears = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player1, bears);

        harness.castFromHand(player1, new GrowOldTogether(), "{1}{G}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2).allMatch(bears::contains);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1).allMatch(bears::contains);
        List<Card> remainingCards = new ArrayList<>(gd.playerHands.get(player1.getId()));
        remainingCards.addAll(gd.playerDecks.get(player1.getId()));
        assertThat(remainingCards).containsExactlyInAnyOrderElementsOf(bears);
    }

    @Test
    void doesNotSeekCreaturesBelowTheTopTenWhenThereAreNoMatches() {
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            library.add(new Forest());
        }
        library.add(new GrizzlyBears());
        harness.setLibrary(player1, library);

        harness.castFromHand(player1, new GrowOldTogether(), "{1}{G}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(library);
    }

    @Test
    void repeatedCastsStillBoostExistingHandCreaturesWithAnEmptyLibrary() {
        GrizzlyBears bear = new GrizzlyBears();
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new GrowOldTogether(), new GrowOldTogether(), bear, new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent creature = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void seekingAKithkinTriggersLibraryToHandAbilities() {
        harness.addToBattlefield(player1, new KithkinBrinefarer());
        Card soughtKithkin = new KithkinBrinefarer();
        harness.setLibrary(player1, List.of(soughtKithkin));

        harness.castFromHand(player1, new GrowOldTogether(), "{1}{G}{U}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2).contains(soughtKithkin)
                .anyMatch(card -> !card.getId().equals(soughtKithkin.getId()));
    }
}
