package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RaccoonRallier;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({BrambleguardVeteran.class, GrizzlyBears.class, RaccoonRallier.class, Shock.class})
class BrambleguardVeteranTest extends BaseCardTest {

    @Test
    @DisplayName("Gives your Raccoons +1/+1 and vigilance when you expend four")
    void boostsRaccoonsWhenControllerExpendsFour() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent raccoon = harness.addToBattlefieldAndReturn(player1, new RaccoonRallier());
        Permanent nonRaccoon = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BrambleguardVeteran(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent veteran = findPermanent(player1, "Brambleguard Veteran");
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(PermanentGrants.powerBoostOf(gd, veteran)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, veteran)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, veteran, Keyword.VIGILANCE)).isTrue();
        assertThat(PermanentGrants.powerBoostOf(gd, raccoon)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, raccoon)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, raccoon, Keyword.VIGILANCE)).isTrue();
        assertThat(PermanentGrants.powerBoostOf(gd, nonRaccoon)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, nonRaccoon)).isZero();
        assertThat(gqs.hasKeyword(gd, nonRaccoon, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger before four mana is spent")
    void doesNotTriggerBelowExpendThreshold() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent veteran = harness.addToBattlefieldAndReturn(player1, new BrambleguardVeteran());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(PermanentGrants.powerBoostOf(gd, veteran)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, veteran)).isZero();
        assertThat(gqs.hasKeyword(gd, veteran, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The expend boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent veteran = harness.addToBattlefieldAndReturn(player1, new BrambleguardVeteran());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 4);

        for (int i = 0; i < 4; i++) {
            harness.castAndResolveInstant(player1, 0, player2.getId());
        }

        assertThat(PermanentGrants.powerBoostOf(gd, veteran)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, veteran)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, veteran, Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.powerBoostOf(gd, veteran)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, veteran)).isZero();
        assertThat(gqs.hasKeyword(gd, veteran, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @CardUsed({BrambleguardVeteran.class, RaccoonRallier.class})
    void triggersOnlyOnceAndDoesNotBoostOpposingOrLaterRaccoons() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent veteran = harness.addToBattlefieldAndReturn(player1, new BrambleguardVeteran());
        Permanent opposingRaccoon = harness.addToBattlefieldAndReturn(player2, new RaccoonRallier());
        harness.setHand(player1, List.of(new RaccoonRallier(), new RaccoonRallier(), new RaccoonRallier()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
        Permanent firstRaccoon = findPermanent(player1, "Raccoon Rallier");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(PermanentGrants.powerBoostOf(gd, veteran)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, veteran)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, veteran, Keyword.VIGILANCE)).isTrue();
        assertThat(PermanentGrants.powerBoostOf(gd, firstRaccoon)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, firstRaccoon)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, firstRaccoon, Keyword.VIGILANCE)).isTrue();
        assertThat(PermanentGrants.powerBoostOf(gd, opposingRaccoon)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, opposingRaccoon)).isZero();
        assertThat(gqs.hasKeyword(gd, opposingRaccoon, Keyword.VIGILANCE)).isFalse();
        for (Permanent permanent : gd.playerBattlefields.get(player1.getId())) {
            if (permanent != veteran && permanent != firstRaccoon) {
                assertThat(PermanentGrants.powerBoostOf(gd, permanent)).isZero();
                assertThat(PermanentGrants.toughnessBoostOf(gd, permanent)).isZero();
                assertThat(gqs.hasKeyword(gd, permanent, Keyword.VIGILANCE)).isFalse();
            }
        }
    }

    @Test
    @CardUsed({BrambleguardVeteran.class})
    void crossingThresholdWithOneSpellTriggersBeforeThatCreatureEnters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BrambleguardVeteran(), new BrambleguardVeteran()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
        Permanent firstVeteran = findPermanent(player1, "Brambleguard Veteran");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(PermanentGrants.powerBoostOf(gd, firstVeteran)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, firstVeteran)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, firstVeteran, Keyword.VIGILANCE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        Permanent secondVeteran = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != firstVeteran)
                .findFirst().orElseThrow();
        assertThat(PermanentGrants.powerBoostOf(gd, secondVeteran)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, secondVeteran)).isZero();
        assertThat(gqs.hasKeyword(gd, secondVeteran, Keyword.VIGILANCE)).isFalse();
    }
}
