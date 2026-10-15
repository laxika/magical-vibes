package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FatalFumes;
import com.github.laxika.magicalvibes.cards.k.KraulWarrior;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BorosMastiff.class, KraulWarrior.class, FatalFumes.class})
class BorosMastiffTest extends BaseCardTest {

    @Test
    @DisplayName("Battalion grants lifelink to Boros Mastiff only")
    void battalionGrantsLifelink() {
        Permanent mastiff = addCreatureReady(player1, new BorosMastiff());
        Permanent attacker = addCreatureReady(player1, new KraulWarrior());
        addCreatureReady(player1, new KraulWarrior());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(mastiff.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(attacker.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Battalion does not trigger without two other attackers")
    void battalionDoesNotTriggerWithFewerThanTwoOtherAttackers() {
        Permanent mastiff = addCreatureReady(player1, new BorosMastiff());
        addCreatureReady(player1, new KraulWarrior());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(mastiff.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Lifelink from battalion gains life on combat damage")
    void lifelinkGainsLife() {
        addCreatureReady(player1, new BorosMastiff());
        addCreatureReady(player1, new KraulWarrior());
        addCreatureReady(player1, new KraulWarrior());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Battalion's lifelink grant wears off at end of turn")
    void lifelinkWearsOff() {
        Permanent mastiff = addCreatureReady(player1, new BorosMastiff());
        addCreatureReady(player1, new KraulWarrior());
        addCreatureReady(player1, new KraulWarrior());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();
        assertThat(mastiff.hasKeyword(Keyword.LIFELINK)).isTrue();

        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(mastiff.hasKeyword(Keyword.LIFELINK)).isTrue();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(mastiff.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Battalion requires Boros Mastiff itself to attack")
    void battalionDoesNotTriggerWhileMastiffStaysBehind() {
        Permanent mastiff = addCreatureReady(player1, new BorosMastiff());
        addCreatureReady(player1, new KraulWarrior());
        addCreatureReady(player1, new KraulWarrior());
        addCreatureReady(player1, new KraulWarrior());

        declareAttackers(player1, List.of(1, 2, 3));
        resolveAllTriggers();

        assertThat(mastiff.hasKeyword(Keyword.LIFELINK)).isFalse();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Battalion still grants lifelink after another attacker dies in response")
    void battalionResolvesAfterAnotherAttackerDies() {
        Permanent mastiff = addCreatureReady(player1, new BorosMastiff());
        Permanent attacker = addCreatureReady(player1, new KraulWarrior());
        addCreatureReady(player1, new KraulWarrior());
        harness.setHand(player2, List.of(new FatalFumes()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0, 1, 2));
            assertThat(mastiff.hasKeyword(Keyword.LIFELINK)).isFalse();
            harness.castInstant(player2, 0, attacker.getId());
            harness.passBothPriorities();
            harness.assertInGraveyard(player1, "Kraul Warrior");
            resolveAllTriggers();
        });

        assertThat(mastiff.hasKeyword(Keyword.LIFELINK)).isTrue();
        resolveCombat();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("A nonattacking Boros Mastiff does not gain lifelink from another Mastiff's battalion")
    void battalionDoesNotGrantLifelinkToAnotherMastiff() {
        Permanent attacker = addCreatureReady(player1, new BorosMastiff());
        Permanent nonattacker = addCreatureReady(player1, new BorosMastiff());
        addCreatureReady(player1, new KraulWarrior());
        addCreatureReady(player1, new KraulWarrior());

        declareAttackers(player1, List.of(0, 2, 3));
        resolveAllTriggers();

        assertThat(attacker.hasKeyword(Keyword.LIFELINK)).isTrue();
        assertThat(nonattacker.hasKeyword(Keyword.LIFELINK)).isFalse();
    }
}
