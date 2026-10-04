package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
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
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiftOfTusks.class, FountainOfYouth.class, GrizzlyBears.class, SerraAngel.class, AmoeboidChangeling.class})
class GiftOfTusksTest extends BaseCardTest {

    @Test
    @DisplayName("Makes the target creature a green 3/3 Elephant without abilities")
    void transformsTargetCreature() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        assertThat(gqs.hasColor(gd, angel, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();

        castGiftOfTusks(angel.getId());

        assertThat(angel.getEffectivePower()).isEqualTo(3);
        assertThat(angel.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasColor(gd, angel, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasColor(gd, angel, CardColor.WHITE)).isFalse();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isFalse();
        assertThat(angel.getTransientCreatureTypeOverride()).isEqualTo(CardSubtype.ELEPHANT);
    }

    @Test
    @DisplayName("All effects wear off at end of turn")
    void effectsWearOffAtCleanup() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        castGiftOfTusks(angel.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(angel.getEffectivePower()).isEqualTo(4);
        assertThat(angel.getEffectiveToughness()).isEqualTo(4);
        assertThat(gqs.hasColor(gd, angel, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
        assertThat(angel.getTransientCreatureTypeOverride()).isNull();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiftOfTusks()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID fountainId = fountain.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, fountainId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Counters remain on top of the new base stats for an own creature")
    void preservesCountersAndDoesNotAffectOtherCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castGiftOfTusks(target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
        assertThat(target.getPlusOnePlusOneCounters()).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).containsExactly(CardSubtype.ELEPHANT);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasColor(gd, other, CardColor.WHITE)).isTrue();
    }

    @Test
    @DisplayName("A later ability can remove the Elephant creature type")
    void laterCreatureTypeRemovalOverridesElephant() {
        addCreatureReady(player1, new AmoeboidChangeling());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castGiftOfTusks(target.getId());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, target)).isEmpty();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.hasColor(gd, target, CardColor.GREEN)).isTrue();
    }

    @Test
    @DisplayName("A later ability can grant creature types in addition to Elephant")
    void laterCreatureTypeGrantAddsOtherTypes() {
        addCreatureReady(player1, new AmoeboidChangeling());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castGiftOfTusks(target.getId());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, target))
                .contains(CardSubtype.ELEPHANT, CardSubtype.BEAR, CardSubtype.ANGEL);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("The spell has no effect when its only target leaves before resolution")
    void doesNotAffectOtherCreatureWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.setHand(player1, List.of(new GiftOfTusks()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Gift of Tusks");
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasColor(gd, other, CardColor.WHITE)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, other)).containsExactly(CardSubtype.ANGEL);
    }

    private void castGiftOfTusks(UUID targetId) {
        harness.setHand(player1, List.of(new GiftOfTusks()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
