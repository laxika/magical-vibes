package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.m.ManaGeode;
import com.github.laxika.magicalvibes.cards.t.TithebearerGiant;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BattlefieldPromotion.class, TithebearerGiant.class, ManaGeode.class})
class BattlefieldPromotionTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a counter on and grants first strike to the target creature, then gains 2 life")
    void promotesCreatureAndGainsLife() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TithebearerGiant());
        harness.setHand(player1, List.of(new BattlefieldPromotion()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(creature.getEffectivePower()).isEqualTo(5);
        assertThat(creature.getEffectiveToughness()).isEqualTo(6);
        assertThat(creature.getGrantedKeywords()).contains(Keyword.FIRST_STRIKE);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("First strike wears off at cleanup while the counter remains")
    void firstStrikeWearsOffAtCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TithebearerGiant());
        harness.setHand(player1, List.of(new BattlefieldPromotion()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(creature.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new TithebearerGiant());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ManaGeode());
        harness.setHand(player1, List.of(new BattlefieldPromotion()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can promote an opponent's creature while only the caster gains life")
    void promotesOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new TithebearerGiant());
        harness.setHand(player1, List.of(new BattlefieldPromotion()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not gain life when the only target leaves before resolution")
    void doesNotGainLifeWhenTargetLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TithebearerGiant());
        harness.setHand(player1, List.of(new BattlefieldPromotion()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(creature.getGrantedKeywords()).doesNotContain(Keyword.FIRST_STRIKE);
        harness.assertInGraveyard(player1, "Battlefield Promotion");
        assertThat(gd.stack).isEmpty();
    }
}
