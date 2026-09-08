package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FieldOfRuin;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RavagerWurmTest extends BaseCardTest {

    @Test
    void fightModeFightsTargetCreatureAndRiotCanAddCounter() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castRavager(0, bears.getId());
        resolveCreatureAndChooseRiotCounter();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        Permanent ravager = findPermanent(player1, "Ravager Wurm");
        assertThat(ravager.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void landModeDestroysLandWithNonManaActivatedAbility() {
        Permanent field = harness.addToBattlefieldAndReturn(player2, new FieldOfRuin());

        castRavager(1, field.getId());
        resolveCreatureAndChooseRiotCounter();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Field of Ruin");
    }

    @Test
    void choosingNoModeLeavesTargetPermanentsAlone() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new FieldOfRuin());

        castRavager(-1, null);
        resolveCreatureAndChooseRiotCounter();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Field of Ruin");
        assertThat(findPermanent(player1, "Ravager Wurm")).isNotNull();
    }

    @Test
    void landModeRejectsLandWithOnlyManaAbilities() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> castRavager(1, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fightModeRejectsCreatureYouControl() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> castRavager(0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castRavager(int mode, UUID targetId) {
        harness.setHand(player1, List.of(new RavagerWurm()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        if (targetId == null) {
            harness.castCreature(player1, 0, mode);
        } else {
            harness.castCreature(player1, 0, mode, targetId);
        }
    }

    private void resolveCreatureAndChooseRiotCounter() {
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }
    }
}
