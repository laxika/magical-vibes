package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OldFlitterfang.class, GrizzlyBears.class, Spellbook.class})
class OldFlitterfangTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Food token at the end step if a creature died this turn")
    void createsFoodAtEndStepWithMorbid() {
        harness.addToBattlefield(player1, new OldFlitterfang());
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.withAutoStop(TurnStep.END_STEP, this::resolveAllTriggers);

        assertThat(countPermanents(player1, "Food")).isOne();
    }

    @Test
    @DisplayName("Does not create a Food token when no creature died this turn")
    void doesNotCreateFoodWithoutMorbid() {
        harness.addToBattlefield(player1, new OldFlitterfang());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    @DisplayName("Sacrificing another creature boosts Old Flitterfang until end of turn")
    void sacrificesCreatureAndBoostsSelf() {
        Permanent flitterfang = harness.addToBattlefieldAndReturn(player1, new OldFlitterfang());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.activateAbility(player1, battlefieldIndex(flitterfang), null, null);

            PendingInteraction.PermanentChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
            assertThat(choice.validIds()).contains(creature.getId(), artifact.getId())
                    .doesNotContain(flitterfang.getId());

            harness.handlePermanentChosen(player1, creature.getId());
            resolveAllTriggers();
        });

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(flitterfang.getEffectivePower()).isEqualTo(5);
        assertThat(flitterfang.getEffectiveToughness()).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(flitterfang.getEffectivePower()).isEqualTo(3);
        assertThat(flitterfang.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Sacrificing another artifact boosts Old Flitterfang")
    void sacrificesArtifactAndBoostsSelf() {
        Permanent flitterfang = harness.addToBattlefieldAndReturn(player1, new OldFlitterfang());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.activateAbility(player1, battlefieldIndex(flitterfang), null, null);
            harness.handlePermanentChosen(player1, artifact.getId());
            resolveAllTriggers();
        });

        harness.assertInGraveyard(player1, "Spellbook");
        assertThat(flitterfang.getEffectivePower()).isEqualTo(5);
        assertThat(flitterfang.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Cannot sacrifice Old Flitterfang to its own ability")
    void cannotSacrificeSelf() {
        Permanent flitterfang = harness.addToBattlefieldAndReturn(player1, new OldFlitterfang());
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(flitterfang), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
