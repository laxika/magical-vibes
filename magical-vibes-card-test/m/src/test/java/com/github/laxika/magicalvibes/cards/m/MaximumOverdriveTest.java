package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MaximumOverdrive.class, GrizzlyBears.class, FountainOfYouth.class, Unsummon.class, Terror.class})
class MaximumOverdriveTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on target creature and grants deathtouch and indestructible")
    void boostsTargetCreatureAndGrantsKeywords() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MaximumOverdrive()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bear.getGrantedKeywords()).contains(Keyword.DEATHTOUCH, Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("The granted keywords wear off at cleanup, but the counter remains")
    void keywordsWearOffAtCleanup() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MaximumOverdrive()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bear.getGrantedKeywords()).doesNotContain(Keyword.DEATHTOUCH, Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new MaximumOverdrive()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void canTargetOpponentsCreatureWithoutAffectingOtherCreatures() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MaximumOverdrive()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, opposingBear.getId());
        harness.passBothPriorities();

        assertThat(opposingBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingBear.getGrantedKeywords()).contains(Keyword.DEATHTOUCH, Keyword.INDESTRUCTIBLE);
        assertThat(ownBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ownBear.getGrantedKeywords()).doesNotContain(Keyword.DEATHTOUCH, Keyword.INDESTRUCTIBLE);
    }

    @Test
    void repeatedCastsAddCountersThatRemainAfterKeywordsExpire() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MaximumOverdrive(), new MaximumOverdrive()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bear.getGrantedKeywords()).contains(Keyword.DEATHTOUCH, Keyword.INDESTRUCTIBLE);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bear.getGrantedKeywords()).doesNotContain(Keyword.DEATHTOUCH, Keyword.INDESTRUCTIBLE);
    }

    @Test
    void removedTargetDoesNotRedirectEffectsToAnotherCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MaximumOverdrive(), new Unsummon()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Maximum Overdrive");
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(other.getGrantedKeywords()).doesNotContain(Keyword.DEATHTOUCH, Keyword.INDESTRUCTIBLE);
    }

    @Test
    void indestructibleSavesCreatureFromDestructionAlreadyOnStack() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Terror(), new MaximumOverdrive()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castInstant(player1, 0, bear.getId());
        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Terror");
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bear.getGrantedKeywords()).contains(Keyword.DEATHTOUCH, Keyword.INDESTRUCTIBLE);
    }
}
