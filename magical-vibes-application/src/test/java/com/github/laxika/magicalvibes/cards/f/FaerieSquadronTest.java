package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FaerieSquadron.class})
class FaerieSquadronTest extends BaseCardTest {

    @Test
    void castWithoutKickerDoesNotPutOnCounters() {
        harness.setHand(player1, List.of(new FaerieSquadron()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent faerie = findFaerie();
        assertThat(faerie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void castWithKickerEntersWithTwoPlusOnePlusOneCounters() {
        harness.setHand(player1, List.of(new FaerieSquadron()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent faerie = findFaerie();
        assertThat(faerie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void castWithKickerEntersWithFlying() {
        harness.setHand(player1, List.of(new FaerieSquadron()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent faerie = findFaerie();
        assertThat(gqs.hasKeyword(gd, faerie, Keyword.FLYING)).isTrue();
    }

    @Test
    void castWithKickerRequiresAdditionalFourMana() {
        harness.setHand(player1, List.of(new FaerieSquadron()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent findFaerie() {
        return gqs.findPermanentById(gd, harness.getPermanentId(player1, "Faerie Squadron"));
    }

    @Test
    void castWithoutKickerDoesNotGainFlying() {
        harness.setHand(player1, List.of(new FaerieSquadron()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, findFaerie(), Keyword.FLYING)).isFalse();
    }

    @Test
    void kickerAcceptsGenericManaButRequiresASecondBlueMana() {
        harness.setHand(player1, List.of(new FaerieSquadron()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void kickerCanBePaidWithTwoBlueAndThreeGenericMana() {
        harness.setHand(player1, List.of(new FaerieSquadron()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findFaerie().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, findFaerie(), Keyword.FLYING)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void kickedStatusDoesNotAffectAnotherSquadron() {
        Permanent unkicked = harness.addToBattlefieldAndReturn(player1, new FaerieSquadron());
        harness.setHand(player1, List.of(new FaerieSquadron()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent kicked = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getId().equals(unkicked.getId()))
                .findFirst().orElseThrow();
        assertThat(unkicked.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, unkicked, Keyword.FLYING)).isFalse();
        assertThat(kicked.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, kicked, Keyword.FLYING)).isTrue();
    }
}
