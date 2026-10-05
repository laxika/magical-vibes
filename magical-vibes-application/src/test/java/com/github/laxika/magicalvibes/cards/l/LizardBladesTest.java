package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BearerOfMemory;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LizardBlades.class, BearerOfMemory.class})
class LizardBladesTest extends BaseCardTest {

    @Test
    void equippedCreatureGainsDoubleStrike() {
        Permanent blades = addReadyBlades();
        Permanent creature = addCreatureReady(player1, new BearerOfMemory());
        blades.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.isCreature(gd, blades)).isFalse();
    }

    @Test
    void reconfigureAttachesAndUnattachesTheBlades() {
        Permanent blades = addReadyBlades();
        Permanent creature = addCreatureReady(player1, new BearerOfMemory());
        addReconfigureMana();

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(blades.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.isCreature(gd, blades)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();

        addReconfigureMana();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(blades.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, blades)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void reconfigureCannotTargetAnOpponentsCreature() {
        Permanent blades = addReadyBlades();
        Permanent opponentCreature = addCreatureReady(player2, new BearerOfMemory());
        addReconfigureMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blades.getAttachedTo()).isNull();
    }

    @Test
    void reconfigureCannotTargetItself() {
        Permanent blades = addReadyBlades();
        addReconfigureMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, blades.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void unattachingRequiresBeingAttached() {
        addReadyBlades();
        addReconfigureMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothReconfigureAbilitiesRequireSorceryTiming() {
        Permanent blades = addReadyBlades();
        Permanent creature = addCreatureReady(player1, new BearerOfMemory());
        addReconfigureMana();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        blades.setAttachedTo(creature.getId());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blades.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void reconfigureCanMoveDirectlyBetweenCreatures() {
        Permanent blades = addReadyBlades();
        Permanent first = addCreatureReady(player1, new BearerOfMemory());
        Permanent second = addCreatureReady(player1, new BearerOfMemory());
        blades.setAttachedTo(first.getId());
        addReconfigureMana();

        harness.activateAbility(player1, 0, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(blades.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.isCreature(gd, blades)).isFalse();
        assertThat(gqs.hasKeyword(gd, first, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void reconfigureDoesNotRequireHasteOrUntapping() {
        Permanent blades = harness.addToBattlefieldAndReturn(player1, new LizardBlades());
        blades.setSummoningSick(true);
        blades.setTapped(true);
        Permanent creature = addCreatureReady(player1, new BearerOfMemory());
        addReconfigureMana();

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(blades.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(blades.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void attachedCreatureDealsDamageInBothCombatDamageSteps() {
        Permanent blades = addReadyBlades();
        Permanent creature = addCreatureReady(player1, new BearerOfMemory());
        blades.setAttachedTo(creature.getId());
        harness.setLife(player2, 20);

        declareAttackers(List.of(1));
        resolveCombat();

        harness.assertLife(player2, 14);
    }

    @Test
    void unattachedBladesDealDamageInBothCombatDamageSteps() {
        addReadyBlades();
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    private Permanent addReadyBlades() {
        return addCreatureReady(player1, new LizardBlades());
    }

    private void addReconfigureMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
