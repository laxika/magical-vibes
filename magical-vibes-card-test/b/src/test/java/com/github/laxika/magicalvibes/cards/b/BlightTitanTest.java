package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ParallelLives;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlightTitan.class, GrizzlyBears.class, Forest.class, ParallelLives.class})
class BlightTitanTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield mills two cards, then incubates for creature cards in the graveyard")
    void enteringMillsThenIncubates() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));
        harness.castFromHand(player1, new BlightTitan(), "{4}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking mills two cards, then incubates for the updated creature-card count")
    void attackingMillsThenIncubates() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));
        addCreatureReady(player1, new BlightTitan());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void createsAnIncubatorEvenWithNoCreaturesAndAnEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.enterBattlefieldAndReturn(player1, new BlightTitan());
        harness.passBothPriorities();

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(incubator.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(incubator.getCard().hasType(CardType.CREATURE)).isFalse();
    }

    @Test
    void countsOnlyItsControllersGraveyardAndMillsAShortLibrary() {
        harness.setLibrary(player1, List.of(new BlightTitan()));
        harness.setGraveyard(player1, List.of(new BlightTitan()));
        harness.setGraveyard(player2, List.of(new BlightTitan(), new BlightTitan()));
        harness.enterBattlefieldAndReturn(player1, new BlightTitan());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(findPermanent(player1, "Incubator").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    void createdTokenHasTheIncubatorArtifactSubtype() {
        harness.setLibrary(player1, List.of());
        harness.enterBattlefieldAndReturn(player1, new BlightTitan());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Incubator").getCard().getSubtypes())
                .extracting(Enum::name).contains("INCUBATOR");
    }

    @Test
    void payingTwoTransformsTheTokenAndKeepsItsCounters() {
        harness.setLibrary(player1, List.of(new BlightTitan(), new BlightTitan()));
        harness.enterBattlefieldAndReturn(player1, new BlightTitan());
        harness.passBothPriorities();
        Permanent incubator = findPermanent(player1, "Incubator");
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(incubator);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, index, null, null);
        harness.passBothPriorities();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(incubator.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(incubator.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(incubator.getCard().getSubtypes()).contains(CardSubtype.PHYREXIAN);
        assertThat(gqs.getEffectivePower(gd, incubator)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, incubator)).isEqualTo(2);
        assertThat(incubator.getCard().getName()).isEqualTo("Phyrexian Token");
    }

    @Test
    void twoPendingTransformActivationsDoNotTransformTheTokenBack() {
        harness.setLibrary(player1, List.of(new BlightTitan(), new BlightTitan()));
        harness.enterBattlefieldAndReturn(player1, new BlightTitan());
        harness.passBothPriorities();
        Permanent incubator = findPermanent(player1, "Incubator");
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(incubator);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, index, null, null);
        harness.activateAbility(player1, index, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(incubator.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void tokenDoublingGivesEveryIncubatorTheFullCounterCount() {
        harness.addToBattlefield(player1, new ParallelLives());
        harness.setLibrary(player1, List.of(new BlightTitan(), new BlightTitan()));
        harness.enterBattlefieldAndReturn(player1, new BlightTitan());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Incubator")).hasSize(2)
                .allSatisfy(token -> assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                        .isEqualTo(2));
    }
}
