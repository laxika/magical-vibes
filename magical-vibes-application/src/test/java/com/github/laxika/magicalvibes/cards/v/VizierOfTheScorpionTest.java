package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VizierOfTheScorpion.class, GrizzlyBears.class})
class VizierOfTheScorpionTest extends BaseCardTest {

    @Test
    void amassesWithoutAnArmyAndGivesTheZombieTokenDeathtouch() {
        castVizierOfTheScorpion();

        Permanent army = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(army.getEffectivePower()).isEqualTo(1);
        assertThat(army.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, army, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void amassesOnAnExistingArmyAndDoesNotGiveDeathtouchToANontoken() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);

        castVizierOfTheScorpion();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .isEmpty();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(gqs.hasKeyword(gd, army, Keyword.DEATHTOUCH)).isFalse();
    }

    private void castVizierOfTheScorpion() {
        harness.setHand(player1, List.of(new VizierOfTheScorpion()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
