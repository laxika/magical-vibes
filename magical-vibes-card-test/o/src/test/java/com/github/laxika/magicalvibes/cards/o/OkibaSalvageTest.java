package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CharcoalDiamond;
import com.github.laxika.magicalvibes.cards.e.EcologistsTerrarium;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GoldenTailDisciple;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MobilizerMech;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({OkibaSalvage.class, GrizzlyBears.class, MobilizerMech.class,
        CharcoalDiamond.class, GloriousAnthem.class, GoldenTailDisciple.class, EcologistsTerrarium.class})
class OkibaSalvageTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature with two +1/+1 counters when controlling an artifact and enchantment")
    void returnsCreatureWithCountersWithArtifactAndEnchantment() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addToBattlefield(player1, new CharcoalDiamond());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.setHand(player1, List.of(new OkibaSalvage()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(findReturned(creature).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Returns a Vehicle and applies the conditional counters")
    void returnsVehicleWithCounters() {
        Card vehicle = new MobilizerMech();
        harness.setGraveyard(player1, List.of(vehicle));
        harness.addToBattlefield(player1, new CharcoalDiamond());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.setHand(player1, List.of(new OkibaSalvage()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, vehicle.getId());

        assertThat(findReturned(vehicle).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Returns a creature without counters when the artifact and enchantment condition is not met")
    void returnsCreatureWithoutCountersWithoutArtifactAndEnchantment() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new OkibaSalvage()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(findReturned(creature).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cannot target a non-creature non-Vehicle card")
    void cannotTargetNonCreatureNonVehicleCard() {
        Card artifact = new CharcoalDiamond();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new OkibaSalvage()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnedVehicleCanSupplyTheRequiredArtifact() {
        Card vehicle = new MobilizerMech();
        harness.setGraveyard(player1, List.of(vehicle));
        harness.addToBattlefield(player1, new GoldenTailDisciple());
        harness.setHand(player1, List.of(new OkibaSalvage()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, vehicle.getId());

        assertThat(findReturned(vehicle).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Mobilizer Mech");
    }

    @Test
    void returnedCreatureCanSupplyTheRequiredEnchantment() {
        Card creature = new GoldenTailDisciple();
        harness.setGraveyard(player1, List.of(creature));
        harness.addToBattlefield(player1, new EcologistsTerrarium());
        harness.setHand(player1, List.of(new OkibaSalvage()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(findReturned(creature).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Golden-Tail Disciple");
    }

    @Test
    void artifactAloneDoesNotGrantCounters() {
        Card vehicle = new MobilizerMech();
        harness.setGraveyard(player1, List.of(vehicle));
        harness.addToBattlefield(player1, new EcologistsTerrarium());
        harness.setHand(player1, List.of(new OkibaSalvage()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, vehicle.getId());

        assertThat(findReturned(vehicle).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentsArtifactDoesNotSatisfyCondition() {
        Card creature = new GoldenTailDisciple();
        harness.setGraveyard(player1, List.of(creature));
        harness.addToBattlefield(player2, new EcologistsTerrarium());
        harness.setHand(player1, List.of(new OkibaSalvage()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(findReturned(creature).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotTargetOpponentsGraveyard() {
        Card vehicle = new MobilizerMech();
        harness.setGraveyard(player2, List.of(vehicle));
        harness.setHand(player1, List.of(new OkibaSalvage()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, vehicle.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void countersConditionIsCheckedAtResolution() {
        Card vehicle = new MobilizerMech();
        harness.setGraveyard(player1, List.of(vehicle));
        harness.setHand(player1, List.of(new OkibaSalvage()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castSorcery(player1, 0, vehicle.getId());

        harness.addToBattlefield(player1, new GoldenTailDisciple());
        harness.passBothPriorities();

        assertThat(findReturned(vehicle).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void targetRemovedFromGraveyardIsNotReturned() {
        Card vehicle = new MobilizerMech();
        harness.setGraveyard(player1, List.of(vehicle));
        harness.setHand(player1, List.of(new OkibaSalvage()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castSorcery(player1, 0, vehicle.getId());

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(vehicle));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mobilizer Mech");
    }

    private Permanent findReturned(Card card) {
        return findPermanent(player1, card.getName());
    }
}
