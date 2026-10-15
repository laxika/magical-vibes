package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PolymorphistsJest.class, SerraAngel.class, GrizzlyBears.class, FountainOfYouth.class})
class PolymorphistsJestTest extends BaseCardTest {

    @Test
    @DisplayName("Turns every creature target player controls into a blue 1/1 Frog without abilities")
    void transformsTargetPlayersCreatures() {
        Permanent targetAngel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        Permanent targetBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent targetFountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent ownAngel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());

        castPolymorphistsJest(player2.getId());

        assertThat(targetAngel.getEffectivePower()).isEqualTo(1);
        assertThat(targetAngel.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, targetAngel, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasColor(gd, targetAngel, CardColor.BLUE)).isTrue();
        assertThat(gqs.hasColor(gd, targetAngel, CardColor.WHITE)).isFalse();
        assertThat(GameQueryService.permanentHasSubtype(targetAngel, CardSubtype.FROG)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(targetAngel, CardSubtype.ANGEL)).isFalse();

        assertThat(targetBears.getEffectivePower()).isEqualTo(1);
        assertThat(targetBears.getEffectiveToughness()).isEqualTo(1);
        assertThat(GameQueryService.permanentHasSubtype(targetBears, CardSubtype.FROG)).isTrue();

        assertThat(targetFountain.getEffectiveColor()).isNull();
        assertThat(ownAngel.getEffectivePower()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ownAngel, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasColor(gd, ownAngel, CardColor.WHITE)).isTrue();
    }

    @Test
    @DisplayName("The transformation wears off at end of turn")
    void transformationWearsOffAtCleanup() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castPolymorphistsJest(player2.getId());
        assertThat(angel.getEffectivePower()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(angel.getEffectivePower()).isEqualTo(4);
        assertThat(angel.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasColor(gd, angel, CardColor.WHITE)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(angel, CardSubtype.ANGEL)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(angel, CardSubtype.FROG)).isFalse();
    }

    @Test
    @DisplayName("Can target its caster without affecting the opponent's creatures")
    void canTargetItsCaster() {
        Permanent ownAngel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        Permanent opposingAngel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castPolymorphistsJest(player1.getId());

        assertThat(ownAngel.getEffectivePower()).isEqualTo(1);
        assertThat(ownAngel.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, ownAngel, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasColor(gd, ownAngel, CardColor.BLUE)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(ownAngel, CardSubtype.FROG)).isTrue();
        assertThat(opposingAngel.getEffectivePower()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, opposingAngel, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasColor(gd, opposingAngel, CardColor.WHITE)).isTrue();
    }

    @Test
    @DisplayName("Creatures entering after resolution are unaffected")
    void doesNotAffectCreaturesEnteringLater() {
        Permanent originalAngel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castPolymorphistsJest(player2.getId());
        Permanent laterAngel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        assertThat(originalAngel.getEffectivePower()).isEqualTo(1);
        assertThat(laterAngel.getEffectivePower()).isEqualTo(4);
        assertThat(laterAngel.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, laterAngel, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasColor(gd, laterAngel, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasColor(gd, laterAngel, CardColor.BLUE)).isFalse();
        assertThat(GameQueryService.permanentHasSubtype(laterAngel, CardSubtype.ANGEL)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(laterAngel, CardSubtype.FROG)).isFalse();
    }

    @Test
    @DisplayName("Can resolve targeting a player with no creatures")
    void resolvesWithNoCreatures() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        castPolymorphistsJest(player2.getId());

        harness.assertInGraveyard(player1, "Polymorphist's Jest");
        assertThat(gqs.hasColor(gd, fountain, CardColor.BLUE)).isFalse();
        assertThat(GameQueryService.permanentHasSubtype(fountain, CardSubtype.FROG)).isFalse();
    }

    @Test
    @DisplayName("Power and toughness counters still apply to the new base stats")
    void retainsPowerToughnessCounters() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        angel.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castPolymorphistsJest(player2.getId());

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(3);
        assertThat(angel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void castPolymorphistsJest(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new PolymorphistsJest()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAndResolveInstant(player1, 0, targetPlayerId);
    }
}
