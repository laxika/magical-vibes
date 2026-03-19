package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.UntapAllControlledPermanentsEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VillageBellRingerTest extends BaseCardTest {

    @Test
    @DisplayName("Has ON_ENTER_BATTLEFIELD trigger with UntapAllControlledPermanentsEffect")
    void hasCorrectEffect() {
        VillageBellRinger card = new VillageBellRinger();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(UntapAllControlledPermanentsEffect.class);
    }

    @Test
    @DisplayName("ETB trigger puts triggered ability on the stack")
    void etbPutsTriggerOnStack() {
        harness.setHand(player1, List.of(new VillageBellRinger()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Untaps all tapped creatures you control when ETB resolves")
    void untapsAllTappedCreaturesYouControl() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        Permanent bear1 = battlefield.get(0);
        bear1.tap();
        Permanent bear2 = battlefield.get(1);
        bear2.tap();

        harness.setHand(player1, List.of(new VillageBellRinger()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(bear1.isTapped()).isFalse();
        assertThat(bear2.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not untap opponent's creatures")
    void doesNotUntapOpponentCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent opponentBear = gd.playerBattlefields.get(player2.getId()).getFirst();
        opponentBear.tap();

        harness.setHand(player1, List.of(new VillageBellRinger()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(opponentBear.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not untap non-creature permanents")
    void doesNotUntapNonCreaturePermanents() {
        harness.addToBattlefield(player1, new Island());
        Permanent island = gd.playerBattlefields.get(player1.getId()).getFirst();
        island.tap();

        harness.setHand(player1, List.of(new VillageBellRinger()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(island.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Village Bell-Ringer enters the battlefield")
    void entersBattlefield() {
        harness.setHand(player1, List.of(new VillageBellRinger()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Village Bell-Ringer"));
    }
}
