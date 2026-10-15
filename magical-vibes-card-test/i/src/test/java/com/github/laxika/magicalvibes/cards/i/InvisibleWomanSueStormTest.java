package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AgentMariaHill;
import com.github.laxika.magicalvibes.cards.a.AgentPhilCoulson;
import com.github.laxika.magicalvibes.cards.b.BurstOfStrength;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JusticeVanceAstrovik;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InvisibleWomanSueStorm.class, AgentMariaHill.class, AgentPhilCoulson.class,
        JusticeVanceAstrovik.class, BurstOfStrength.class, GrizzlyBears.class})
class InvisibleWomanSueStormTest extends BaseCardTest {

    @Test
    @DisplayName("Putting a +1/+1 counter on another controlled Hero may create a Wall")
    void createsWallForOtherControlledHero() {
        addCreatureReady(player1, new InvisibleWomanSueStorm());
        Permanent hero = addCreatureReady(player1, new AgentMariaHill());

        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, hero.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent wall = findPermanent(player1, "Wall");
        assertThat(wall.getCard().getColor()).isNull();
        assertThat(wall.getCard().getSubtypes()).containsExactly(CardSubtype.WALL);
        assertThat(gqs.getEffectivePower(gd, wall)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, wall, Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("The trigger ignores Sue herself, non-Heroes, and opposing placements")
    void ignoresInvalidCounterPlacements() {
        Permanent sue = addCreatureReady(player1, new InvisibleWomanSueStorm());
        Permanent nonHero = addCreatureReady(player1, new GrizzlyBears());
        Permanent hero = addCreatureReady(player1, new AgentMariaHill());

        putCounter(player1, sue);
        putCounter(player1, nonHero);

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new BurstOfStrength()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, hero.getId());

        assertThat(findPermanents(player1, "Wall")).isEmpty();
    }

    @Test
    @DisplayName("Sue gains life when she deals combat damage")
    void lifelinkGainsLife() {
        addCreatureReady(player1, new InvisibleWomanSueStorm());
        harness.setLife(player1, 10);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The controller may decline to create a Wall")
    void mayDeclineWall() {
        addCreatureReady(player1, new InvisibleWomanSueStorm());
        Permanent hero = addCreatureReady(player1, new AgentMariaHill());

        putCounter(player1, hero);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(hero.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(findPermanents(player1, "Wall")).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Counters placed on an opponent's Hero do not trigger Sue")
    void ignoresOpponentHero() {
        addCreatureReady(player1, new InvisibleWomanSueStorm());
        Permanent hero = addCreatureReady(player2, new AgentMariaHill());

        putCounter(player1, hero);

        assertThat(hero.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanents(player1, "Wall")).isEmpty();
    }

    @Test
    @DisplayName("Separate counter placements can each create a Wall in the same turn")
    void triggersForEachSeparatePlacement() {
        addCreatureReady(player1, new InvisibleWomanSueStorm());
        Permanent hero = addCreatureReady(player1, new AgentMariaHill());

        for (int i = 0; i < 2; i++) {
            putCounter(player1, hero);
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                    .isEqualTo(player1.getId());
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        }

        assertThat(findPermanents(player1, "Wall")).hasSize(2);
    }

    @Test
    @DisplayName("Simultaneous counters on multiple other Heroes create only one Wall")
    void simultaneousPlacementTriggersOnce() {
        addCreatureReady(player1, new InvisibleWomanSueStorm());
        Permanent coulson = addCreatureReady(player1, new AgentPhilCoulson());
        Permanent maria = addCreatureReady(player1, new AgentMariaHill());
        Permanent justice = addCreatureReady(player1, new JusticeVanceAstrovik());

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(coulson.getPlusOnePlusOneCounters()).isZero();
        assertThat(maria.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(justice.getPlusOnePlusOneCounters()).isEqualTo(1);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Wall")).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void putCounter(com.github.laxika.magicalvibes.model.Player player, Permanent target) {
        harness.setHand(player, List.of(new BurstOfStrength()));
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player, 0, target.getId());
    }
}
