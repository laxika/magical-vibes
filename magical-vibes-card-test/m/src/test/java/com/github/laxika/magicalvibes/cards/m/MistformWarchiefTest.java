package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.d.DaruSpiritualist;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MistformWarchief.class, DaruSpiritualist.class, Conspiracy.class})
class MistformWarchiefTest extends BaseCardTest {

    @Test
    @DisplayName("Creature spells sharing this creature's type cost {1} less")
    void reducesCreatureSpellsSharingType() {
        harness.addToBattlefield(player1, new MistformWarchief());
        harness.setHand(player1, List.of(new MistformWarchief()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(MistformWarchief.class);
    }

    @Test
    @DisplayName("Creature spells without a shared type are not reduced")
    void doesNotReduceCreatureSpellsWithoutSharedType() {
        harness.addToBattlefield(player1, new MistformWarchief());
        harness.setHand(player1, List.of(new DaruSpiritualist()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Changing this creature's type changes which creature spells are reduced")
    void usesCurrentCreatureTypeForCostReduction() {
        addCreatureReady(player1, new MistformWarchief());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.CLERIC.name());

        harness.setHand(player1, List.of(new DaruSpiritualist()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(DaruSpiritualist.class);
    }

    @Test
    @DisplayName("Changing this creature's type stops reducing spells of its former type")
    void changedCreatureTypeNoLongerMatchesFormerType() {
        addCreatureReady(player1, new MistformWarchief());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.CLERIC.name());

        harness.setHand(player1, List.of(new MistformWarchief()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The temporary creature type no longer affects cost reduction after end of turn")
    void changedCreatureTypeWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new MistformWarchief());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.CLERIC.name());

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player1, List.of(new DaruSpiritualist()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reduction does not apply to matching creature spells controlled by an opponent")
    void doesNotReduceOpponentCreatureSpells() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MistformWarchief());
        harness.setHand(player2, List.of(new MistformWarchief()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Multiple Warchiefs stack their reductions without reducing colored mana")
    void multipleWarchiefsReduceAllGenericMana() {
        harness.addToBattlefield(player1, new MistformWarchief());
        harness.addToBattlefield(player1, new MistformWarchief());
        harness.setHand(player1, List.of(new MistformWarchief()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(MistformWarchief.class);
    }

    @Test
    @DisplayName("Even excess reductions cannot pay the colored mana cost")
    void reductionsCannotPayColoredMana() {
        harness.addToBattlefield(player1, new MistformWarchief());
        harness.addToBattlefield(player1, new MistformWarchief());
        harness.addToBattlefield(player1, new MistformWarchief());
        harness.setHand(player1, List.of(new MistformWarchief()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sharing the Human type is sufficient for a Human Cleric spell")
    void sharingOneOfSeveralCreatureTypesIsSufficient() {
        addCreatureReady(player1, new MistformWarchief());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.HUMAN.name());
        harness.setHand(player1, List.of(new DaruSpiritualist()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(DaruSpiritualist.class);
    }

    @Test
    @DisplayName("The type-changing ability taps its source and cannot be activated again")
    void typeChangingAbilityPaysTapCost() {
        var warchief = addCreatureReady(player1, new MistformWarchief());

        harness.activateAbility(player1, 0, null, null);

        assertThat(warchief.isTapped()).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, warchief, CardSubtype.ILLUSION)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, warchief, CardSubtype.CLERIC)).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.CLERIC.name());
        assertThat(gqs.hasEffectiveSubtype(gd, warchief, CardSubtype.CLERIC)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, warchief, CardSubtype.ILLUSION)).isFalse();
    }

    @Test
    @DisplayName("Summoning sickness prevents activating the tap ability")
    void summoningSicknessPreventsTypeChange() {
        harness.addToBattlefield(player1, new MistformWarchief());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A later Conspiracy replaces the earlier temporary creature type")
    void laterConspiracyOverridesEarlierTypeChange() {
        var warchief = addCreatureReady(player1, new MistformWarchief());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.DRAGON.name());

        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.HUMAN.name());

        assertThat(gqs.hasEffectiveSubtype(gd, warchief, CardSubtype.HUMAN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, warchief, CardSubtype.DRAGON)).isFalse();
        harness.setHand(player1, List.of(new MistformWarchief()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(MistformWarchief.class);
    }
}
