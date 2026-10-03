package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CinderStrike.class, GrizzlyBears.class, HillGiant.class})
class CinderStrikeTest extends BaseCardTest {

    @Test
    void withoutBlightDealsTwoDamageToTargetCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setToughnessModifier(1);
        harness.setHand(player1, List.of(new CinderStrike()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void blightDealsFourDamageToTargetCreature() {
        Permanent blightCreature = addCreatureReady(player1, new HillGiant());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setToughnessModifier(3);
        harness.setHand(player1, List.of(new CinderStrike()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), blightCreature.getId());
        harness.passBothPriorities();

        assertThat(blightCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void cannotTargetAPlayer() {
        harness.setHand(player1, List.of(new CinderStrike()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotBlightAnOpponentsCreature() {
        Permanent target = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new CinderStrike()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0,
                target.getId(), target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    void blightRemainsPaidWhenTheBlightedCreatureDiesBeforeResolution() {
        Permanent blightCreature = addCreatureReady(player1, new GrizzlyBears());
        blightCreature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Permanent target = addCreatureReady(player2, new HillGiant());
        target.setToughnessModifier(2);
        harness.setHand(player1, List.of(new CinderStrike()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), blightCreature.getId());
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blightCreature);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void canBlightTheCreatureBeingTargeted() {
        Permanent target = addCreatureReady(player1, new HillGiant());
        target.setToughnessModifier(3);
        harness.setHand(player1, List.of(new CinderStrike()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), target.getId());

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(target.getMarkedDamage()).isZero();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }
}
