package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SephirothFallenHero.class, GrizzlyBears.class})
class SephirothFallenHeroTest extends BaseCardTest {

    @Test
    @DisplayName("Attack trigger may put a cell counter on a creature and sets modified creatures to 7/5")
    void attackTriggerAddsCellCounterAndSetsModifiedCreaturesBaseStats() {
        Permanent sephiroth = addCreatureReady(player1, new SephirothFallenHero());
        Permanent modified = addCreatureReady(player1, new GrizzlyBears());
        modified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent unmodified = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(opponentCreature.getCounterCount(CounterType.CELL)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, modified)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, modified)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, unmodified)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, unmodified)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, sephiroth)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, sephiroth)).isEqualTo(5);
    }

    @Test
    @DisplayName("Reunion sacrifices a modified creature and returns Sephiroth tapped")
    void reunionReturnsSephirothTapped() {
        Card sephiroth = new SephirothFallenHero();
        Permanent modified = addCreatureReady(player1, new GrizzlyBears());
        modified.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setGraveyard(player1, List.of(sephiroth));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);
        if (gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null) {
            harness.handlePermanentChosen(player1, modified.getId());
        }
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Sephiroth, Fallen Hero");
        assertThat(returned.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Reunion cannot be activated without a modified creature to sacrifice")
    void reunionRequiresModifiedCreature() {
        harness.setGraveyard(player1, List.of(new SephirothFallenHero()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void decliningCellCounterStillSetsAlreadyModifiedCreaturesBaseStats() {
        addCreatureReady(player1, new SephirothFallenHero());
        Permanent modified = addCreatureReady(player1, new GrizzlyBears());
        modified.setCounterCount(CounterType.CELL, 1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.CELL)).isZero();
        assertThat(gqs.getEffectivePower(gd, modified)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, modified)).isEqualTo(5);
    }

    @Test
    void newlyModifiedTargetIsIncludedAndAffectedCreaturesAreLockedAtResolution() {
        addCreatureReady(player1, new SephirothFallenHero());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.CELL)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
        target.setCounterCount(CounterType.CELL, 0);
        other.setCounterCount(CounterType.CELL, 1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    void illegalAttackTargetPreventsEntireAbilityFromResolving() {
        addCreatureReady(player1, new SephirothFallenHero());
        Permanent modified = addCreatureReady(player1, new GrizzlyBears());
        modified.setCounterCount(CounterType.CELL, 1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, target));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, modified)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, modified)).isEqualTo(2);
    }

    @Test
    void oldReunionActivationCannotReturnSephirothAfterItReturnsAndDiesAgain() {
        Card sephiroth = new SephirothFallenHero();
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        first.setCounterCount(CounterType.CELL, 1);
        second.setCounterCount(CounterType.CELL, 1);
        harness.setGraveyard(player1, List.of(sephiroth));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateGraveyardAbility(player1, 0);
        if (gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null) {
            harness.handlePermanentChosen(player1, first.getId());
        }
        harness.activateGraveyardAbility(player1, 0);
        if (gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null) {
            harness.handlePermanentChosen(player1, second.getId());
        }
        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Sephiroth, Fallen Hero");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, returned));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Sephiroth, Fallen Hero");
        harness.assertInGraveyard(player1, "Sephiroth, Fallen Hero");
    }
}
