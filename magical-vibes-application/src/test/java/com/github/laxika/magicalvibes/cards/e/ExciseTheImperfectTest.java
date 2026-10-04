package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExciseTheImperfect.class, GrizzlyBears.class, Forest.class})
class ExciseTheImperfectTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a nonland permanent and its controller incubates for its mana value")
    void exilesNonlandPermanentAndItsControllerIncubatesForManaValue() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ExciseTheImperfect()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));

        Permanent incubator = findPermanent(player2, "Incubator");
        assertThat(incubator).isNotNull();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanents(player1, "Incubator")).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ExciseTheImperfect()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    @DisplayName("The created artifact has the Incubator subtype")
    void createsIncubatorArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ExciseTheImperfect()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        Permanent incubator = findPermanent(player2, "Incubator");
        assertThat(gqs.isArtifact(gd, incubator)).isTrue();
        assertThat(gqs.isCreature(gd, incubator)).isFalse();
        assertThat(incubator.getCard().getSubtypes()).contains(CardSubtype.INCUBATOR);
    }

    @Test
    @DisplayName("The Incubator transforms for two mana and keeps its counters")
    void incubatorTransformsAndKeepsCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ExciseTheImperfect()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player1, 0, target.getId());
        Permanent incubator = findPermanent(player2, "Incubator");

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(incubator.getCard().getName()).isEqualTo("Phyrexian Token");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(incubator);
        assertThat(gqs.isCreature(gd, incubator)).isTrue();
        assertThat(gqs.isArtifact(gd, incubator)).isTrue();
        assertThat(incubator.getCard().getSubtypes()).contains(CardSubtype.PHYREXIAN);
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, incubator)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, incubator)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can exile your own permanent and incubate under your control")
    void canExileOwnPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ExciseTheImperfect()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Incubator")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanents(player2, "Incubator")).isEmpty();
    }

    @Test
    @DisplayName("Exiling an Incubator incubates zero and the new token dies when transformed")
    void zeroManaValueStillCreatesToken() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ExciseTheImperfect(), new ExciseTheImperfect()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castAndResolveInstant(player1, 0, target.getId());
        Permanent originalIncubator = findPermanent(player2, "Incubator");

        harness.castAndResolveInstant(player1, 0, originalIncubator.getId());

        assertThat(findPermanents(player2, "Incubator")).hasSize(1);
        Permanent newIncubator = findPermanent(player2, "Incubator");
        assertThat(newIncubator.getId()).isNotEqualTo(originalIncubator.getId());
        assertThat(newIncubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.isCreature(gd, newIncubator)).isFalse();

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(newIncubator);
    }
}
