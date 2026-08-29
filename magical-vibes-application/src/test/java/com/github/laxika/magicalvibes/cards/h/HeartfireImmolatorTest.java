package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeartfireImmolatorTest extends BaseCardTest {

    @Test
    @DisplayName("Prowess gives Heartfire Immolator +1/+1 for a noncreature spell")
    void prowessPumpsForNoncreatureSpell() {
        Permanent immolator = addReadyImmolator();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, immolator)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, immolator)).isEqualTo(3);
    }

    @Test
    @DisplayName("Sacrificing Heartfire Immolator deals damage equal to its power to a creature")
    void sacrificeAbilityDealsPowerDamageToCreature() {
        Permanent immolator = addReadyImmolator();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.assertInGraveyard(player1, "Heartfire Immolator");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(immolator);
    }

    @Test
    @DisplayName("Sacrifice ability can target a planeswalker")
    void sacrificeAbilityDamagesPlaneswalker() {
        Permanent immolator = addReadyImmolator();
        Permanent planeswalker = addPlaneswalker(5);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Heartfire Immolator");
    }

    @Test
    @DisplayName("Sacrifice ability cannot target a land")
    void sacrificeAbilityCannotTargetLand() {
        addReadyImmolator();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forest.getId()))
                .isInstanceOf(RuntimeException.class);
    }

    private Permanent addReadyImmolator() {
        Permanent immolator = harness.addToBattlefieldAndReturn(player1, new HeartfireImmolator());
        immolator.setSummoningSick(false);
        return immolator;
    }

    private Permanent addPlaneswalker(int loyalty) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        card.setLoyalty(loyalty);
        Permanent planeswalker = new Permanent(card);
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        gd.playerBattlefields.get(player2.getId()).add(planeswalker);
        return planeswalker;
    }
}
