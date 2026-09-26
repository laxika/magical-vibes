package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HerculesOlympianHero.class, Shock.class})
class HerculesOlympianHeroTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking puts a +1/+1 counter on Hercules and grants indestructible until end of turn")
    void attackingPutsCounterAndGrantsIndestructible() {
        Permanent hercules = addCreatureReady(player1, new HerculesOlympianHero());
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(hercules.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, hercules, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("The first damage each turn puts that many +1/+1 counters on Hercules")
    void firstDamageEachTurnPutsDamageAmountOfCounters() {
        Permanent hercules = addCreatureReady(player2, new HerculesOlympianHero());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, hercules.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hercules.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.castInstant(player1, 0, hercules.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hercules.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Lethal damage destroys Hercules before his damage trigger resolves")
    void lethalDamagePreventsDamageTriggerResolution() {
        Permanent hercules = addCreatureReady(player2, new HerculesOlympianHero());
        hercules.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, hercules.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hercules, Olympian Hero");
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Hercules, Olympian Hero"));
    }
}
