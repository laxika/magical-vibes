package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FieldOfRuin;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SauroformHybrid;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RavagerWurm.class, FieldOfRuin.class, Forest.class, SauroformHybrid.class})
class RavagerWurmTest extends BaseCardTest {

    @Test
    void fightModeFightsTargetCreatureAndRiotCanAddCounter() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());

        castRavager(0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Sauroform Hybrid");
        Permanent ravager = findPermanent(player1, "Ravager Wurm");
        assertThat(ravager.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void landModeDestroysLandWithNonManaActivatedAbility() {
        Permanent field = harness.addToBattlefieldAndReturn(player2, new FieldOfRuin());

        castRavager(1, field.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Field of Ruin");
    }

    @Test
    void choosingNoModeLeavesTargetPermanentsAlone() {
        harness.addToBattlefield(player2, new SauroformHybrid());
        harness.addToBattlefield(player2, new FieldOfRuin());

        castRavager(-1, null);

        harness.assertOnBattlefield(player2, "Sauroform Hybrid");
        harness.assertOnBattlefield(player2, "Field of Ruin");
        assertThat(findPermanent(player1, "Ravager Wurm")).isNotNull();
    }

    @Test
    void landModeRejectsLandWithOnlyManaAbilities() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent field = harness.addToBattlefieldAndReturn(player2, new FieldOfRuin());

        castWithMode(1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, field.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Field of Ruin");
    }

    @Test
    void fightModeRejectsCreatureYouControl() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new SauroformHybrid());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());

        castWithMode(0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, opposingCreature.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Sauroform Hybrid");
        harness.assertInGraveyard(player2, "Sauroform Hybrid");
    }

    @Test
    void riotCanGiveHasteInsteadOfCounter() {
        harness.castFromHand(player1, new RavagerWurm(), "{3}{R}{G}{G}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent ravager = findPermanent(player1, "Ravager Wurm");
        assertThat(ravager.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, ravager, Keyword.HASTE)).isTrue();
    }

    @Test
    void landModeCanDestroyLandYouControl() {
        Permanent field = harness.addToBattlefieldAndReturn(player1, new FieldOfRuin());

        castRavager(1, field.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Field of Ruin");
    }

    @Test
    void canCastWithoutTargetsWhenBattlefieldIsEmpty() {
        harness.castFromHand(player1, new RavagerWurm(), "{3}{R}{G}{G}");
        resolveCreatureAndChooseRiotCounter();

        harness.assertOnBattlefield(player1, "Ravager Wurm");
    }

    @Test
    void entryModeCanTargetCreatureThatAppearedAfterCasting() {
        harness.castFromHand(player1, new RavagerWurm(), "{3}{R}{G}{G}");
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SauroformHybrid());
        resolveCreatureAndChooseRiotCounter();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Sauroform Hybrid");
        assertThat(findPermanent(player1, "Ravager Wurm").getMarkedDamage()).isEqualTo(2);
    }

    private void castWithMode(int mode) {
        harness.setHand(player1, java.util.List.of(new RavagerWurm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, mode);
        resolveCreatureAndChooseRiotCounter();
    }

    private void castRavager(int mode, UUID targetId) {
        castWithMode(mode);
        if (targetId != null) {
            harness.handlePermanentChosen(player1, targetId);
        }
    }

    private void resolveCreatureAndChooseRiotCounter() {
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }
    }
}
