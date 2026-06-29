package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfTargetingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AjanisResponseTest extends BaseCardTest {

    @Test
    @DisplayName("Has cost reduction and destroy target creature effects")
    void hasCorrectEffects() {
        AjanisResponse card = new AjanisResponse();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getTargetFilter()).isEqualTo(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ));
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isEqualTo(new ReduceOwnCastCostIfTargetingPermanentEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsTappedPredicate()
                        )), 3));
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(DestroyTargetPermanentEffect.class);
    }

    @Test
    @DisplayName("Costs {1}{W} when targeting a tapped creature")
    void reducedCostWhenTargetingTappedCreature() {
        Permanent tappedCreature = new Permanent(new GrizzlyBears());
        tappedCreature.tap();
        harness.getGameData().playerBattlefields.get(player2.getId()).add(tappedCreature);

        harness.setHand(player1, List.of(new AjanisResponse()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, tappedCreature.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Not playable with reduced-cost mana when only untapped creatures exist")
    void notPlayableWithReducedCostManaWhenNoTappedCreatures() {
        Permanent untappedCreature = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(untappedCreature);

        harness.setHand(player1, List.of(new AjanisResponse()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, untappedCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Full cost works when targeting an untapped creature with enough mana")
    void fullCostUntappedWithEnoughMana() {
        Permanent untappedCreature = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(untappedCreature);

        harness.setHand(player1, List.of(new AjanisResponse()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0, untappedCreature.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Can target any creature, not only tapped ones")
    void canTargetUntappedCreatureWithFullMana() {
        Permanent untappedCreature = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(untappedCreature);

        harness.setHand(player1, List.of(new AjanisResponse()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castInstant(player1, 0, untappedCreature.getId());

        assertThat(harness.getGameData().stack).hasSize(1);
        assertThat(harness.getGameData().stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("Reduced cost applies when targeting opponent's tapped creature")
    void reducedCostForOpponentTappedCreature() {
        Permanent tappedCreature = new Permanent(new GrizzlyBears());
        tappedCreature.tap();
        harness.getGameData().playerBattlefields.get(player2.getId()).add(tappedCreature);

        harness.setHand(player1, List.of(new AjanisResponse()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, tappedCreature.getId());

        assertThat(harness.getGameData().stack).hasSize(1);
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot afford reduced cost when targeting untapped creature even if a tapped creature exists")
    void cannotUseReducedCostWithUntappedTarget() {
        Permanent tappedCreature = new Permanent(new GrizzlyBears());
        tappedCreature.tap();
        harness.getGameData().playerBattlefields.get(player1.getId()).add(tappedCreature);

        Permanent untappedCreature = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(untappedCreature);

        harness.setHand(player1, List.of(new AjanisResponse()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, untappedCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cost reduction");
    }
}
