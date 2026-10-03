package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DwarvenWarriors;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AndRilFlameOfTheWest.class, DwarvenWarriors.class, ArwenMortalQueen.class})
class AndRilFlameOfTheWestTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsPlusThreePlusOne() {
        Permanent anduril = addAnduril();
        Permanent creature = addCreatureReady(player1, new DwarvenWarriors());
        anduril.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void attackingWithNonlegendaryCreatureCreatesTappedNonattackingSpirits() {
        Permanent anduril = addAnduril();
        Permanent creature = addCreatureReady(player1, new DwarvenWarriors());
        anduril.setAttachedTo(creature.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1));
            resolveAllTriggers();

            List<Permanent> spirits = findPermanents(player1, "Spirit");
            assertThat(spirits).hasSize(2);
            assertThat(spirits).allSatisfy(spirit -> {
                assertThat(spirit.isTapped()).isTrue();
                assertThat(spirit.isAttackedThisTurn()).isFalse();
                assertThat(gqs.getEffectivePower(gd, spirit)).isEqualTo(1);
                assertThat(gqs.getEffectiveToughness(gd, spirit)).isEqualTo(1);
                assertThat(gqs.hasKeyword(gd, spirit, Keyword.FLYING)).isTrue();
            });
        });
    }

    @Test
    void attackingWithLegendaryCreatureCreatesTappedAttackingSpirits() {
        Permanent anduril = addAnduril();
        Permanent creature = addCreatureReady(player1, new ArwenMortalQueen());
        anduril.setAttachedTo(creature.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1));
            resolveAllTriggers();

            List<Permanent> spirits = findPermanents(player1, "Spirit");
            assertThat(spirits).hasSize(2);
            assertThat(spirits).allSatisfy(spirit -> {
                assertThat(spirit.isTapped()).isTrue();
                assertThat(spirit.isAttacking()).isTrue();
                assertThat(spirit.getAttackTarget()).isEqualTo(player2.getId());
            });
        });
    }

    @Test
    void equipPaysTwoManaAndMovesTheBonus() {
        Permanent anduril = addAnduril();
        Permanent first = addCreatureReady(player1, new DwarvenWarriors());
        Permanent second = addCreatureReady(player1, new DwarvenWarriors());
        anduril.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(anduril.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
    }

    @Test
    void attackingWithAnUnequippedCreatureDoesNotCreateSpirits() {
        addAnduril();
        addCreatureReady(player1, new DwarvenWarriors());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    void legendaryAttackerStillCreatesAttackingSpiritsAfterEquipmentMoves() {
        Permanent anduril = addAnduril();
        Permanent attacker = addCreatureReady(player1, new ArwenMortalQueen());
        Permanent other = addCreatureReady(player1, new DwarvenWarriors());
        anduril.setAttachedTo(attacker.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1));
            assertThat(gd.stack).hasSize(1);
            anduril.setAttachedTo(other.getId());
            resolveAllTriggers();

            assertThat(findPermanents(player1, "Spirit")).hasSize(2).allSatisfy(spirit -> {
                assertThat(spirit.isTapped()).isTrue();
                assertThat(spirit.isAttacking()).isTrue();
            });
        });
    }

    @Test
    void nonlegendaryAttackerDoesNotCreateAttackingSpiritsAfterEquipmentMovesToLegendaryCreature() {
        Permanent anduril = addAnduril();
        Permanent attacker = addCreatureReady(player1, new DwarvenWarriors());
        Permanent other = addCreatureReady(player1, new ArwenMortalQueen());
        anduril.setAttachedTo(attacker.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1));
            assertThat(gd.stack).hasSize(1);
            anduril.setAttachedTo(other.getId());
            resolveAllTriggers();

            assertThat(findPermanents(player1, "Spirit")).hasSize(2).allSatisfy(spirit -> {
                assertThat(spirit.isTapped()).isTrue();
                assertThat(spirit.isAttacking()).isFalse();
            });
        });
    }

    @Test
    void opponentsLegendaryAttackerCreatesTappedNonattackingSpiritsForEquipmentController() {
        Permanent anduril = addAnduril();
        Permanent attacker = addCreatureReady(player2, new ArwenMortalQueen());
        anduril.setAttachedTo(attacker.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(0));
            resolveAllTriggers();

            assertThat(findPermanents(player2, "Spirit")).isEmpty();
            assertThat(findPermanents(player1, "Spirit")).hasSize(2).allSatisfy(spirit -> {
                assertThat(spirit.isTapped()).isTrue();
                assertThat(spirit.isAttacking()).isFalse();
            });
        });
    }

    private Permanent addAnduril() {
        return harness.addToBattlefieldAndReturn(player1, new AndRilFlameOfTheWest());
    }
}
