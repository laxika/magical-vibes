package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.CharcoalDiamond;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MobilizerMech;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
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
        CharcoalDiamond.class, GloriousAnthem.class})
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

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

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

        harness.castSorcery(player1, 0, vehicle.getId());
        harness.passBothPriorities();

        assertThat(findReturned(vehicle).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Returns a creature without counters when the artifact and enchantment condition is not met")
    void returnsCreatureWithoutCountersWithoutArtifactAndEnchantment() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new OkibaSalvage()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

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

    private Permanent findReturned(Card card) {
        GameData gameData = harness.getGameData();
        return gameData.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
