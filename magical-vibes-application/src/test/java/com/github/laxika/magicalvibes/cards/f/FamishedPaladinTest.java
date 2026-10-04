package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FamishedPaladin.class, SoulWarden.class, GrizzlyBears.class, AngelOfMercy.class})
class FamishedPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("Famished Paladin does not untap during its controller's untap step")
    void doesNotUntapDuringUntapStep() {
        Permanent paladin = addCreatureReady(player1, new FamishedPaladin());
        paladin.tap();

        harness.performUntapStep(player1);

        assertThat(paladin.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Famished Paladin untaps when its controller gains life")
    void untapsWhenControllerGainsLife() {
        Permanent paladin = addCreatureReady(player1, new FamishedPaladin());
        paladin.tap();
        harness.addToBattlefield(player1, new SoulWarden());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(paladin.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Famished Paladin does not untap when an opponent gains life")
    void doesNotUntapWhenOpponentGainsLife() {
        Permanent paladin = addCreatureReady(player1, new FamishedPaladin());
        paladin.tap();

        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(paladin.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Life gain queues an untap trigger rather than immediately untapping")
    void untapsOnlyWhenLifeGainTriggerResolves() {
        Permanent paladin = addCreatureReady(player1, new FamishedPaladin());
        paladin.tap();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.tap();
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(paladin.isTapped()).isTrue();
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        assertThat(paladin.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(paladin.isTapped()).isFalse();
        assertThat(bears.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An untapped Paladin still triggers and can untap if tapped before resolution")
    void triggersEvenWhenAlreadyUntapped() {
        Permanent paladin = addCreatureReady(player1, new FamishedPaladin());
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(paladin.isTapped()).isFalse();
        paladin.tap();

        harness.passBothPriorities();

        assertThat(paladin.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Each separate life gain can untap the Paladin again in the same turn")
    void untapsForRepeatedLifeGainInSameTurn() {
        Permanent paladin = addCreatureReady(player1, new FamishedPaladin());
        harness.setHand(player1, List.of(new AngelOfMercy(), new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 10);

        for (int i = 0; i < 2; i++) {
            paladin.tap();
            harness.castCreature(player1, 0);
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.passBothPriorities();

            assertThat(paladin.isTapped()).isFalse();
            harness.assertLife(player1, 20 + 3 * (i + 1));
        }
    }

    @Test
    @DisplayName("Life gain untaps every controlled Paladin but no opposing Paladin")
    void eachControlledPaladinTriggersIndependently() {
        Permanent first = addCreatureReady(player1, new FamishedPaladin());
        Permanent second = addCreatureReady(player1, new FamishedPaladin());
        Permanent opponent = addCreatureReady(player2, new FamishedPaladin());
        first.tap();
        second.tap();
        opponent.tap();
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(opponent.isTapped()).isTrue();
    }
}
