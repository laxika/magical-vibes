package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.l.LeatherArmor;
import com.github.laxika.magicalvibes.cards.h.HobgoblinCaptain;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RustMonster.class, LeatherArmor.class, HobgoblinCaptain.class})
class RustMonsterTest extends BaseCardTest {

    @Test
    void sacrificingAnArtifactBoostsRustMonster() {
        Permanent rustMonster = harness.addToBattlefieldAndReturn(player1, new RustMonster());
        harness.addToBattlefield(player1, new LeatherArmor());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(rustMonster.getEffectivePower()).isEqualTo(4);
        assertThat(rustMonster.getEffectiveToughness()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Leather Armor");
    }

    @Test
    void boostExpiresAtEndOfTurn() {
        Permanent rustMonster = harness.addToBattlefieldAndReturn(player1, new RustMonster());
        harness.addToBattlefield(player1, new LeatherArmor());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(rustMonster.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(rustMonster.getEffectivePower()).isEqualTo(2);
    }

    @Test
    void choosingWhichArtifactToSacrifice() {
        harness.addToBattlefield(player1, new RustMonster());
        harness.addToBattlefield(player1, new LeatherArmor());
        harness.addToBattlefield(player1, new LeatherArmor());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    void cannotActivateWithoutAnArtifact() {
        harness.addToBattlefield(player1, new RustMonster());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");
    }

    @Test
    void sacrificeIsPaidBeforeTheBoostResolves() {
        Permanent rustMonster = harness.addToBattlefieldAndReturn(player1, new RustMonster());
        harness.addToBattlefield(player1, new LeatherArmor());

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Leather Armor");
        harness.assertNotOnBattlefield(player1, "Leather Armor");
        assertThat(rustMonster.getEffectivePower()).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(rustMonster.getEffectivePower()).isEqualTo(4);
    }

    @Test
    void repeatedActivationsStackTheirBoosts() {
        Permanent rustMonster = harness.addToBattlefieldAndReturn(player1, new RustMonster());
        harness.addToBattlefield(player1, new LeatherArmor());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player1, new LeatherArmor());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, secondArtifact.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(rustMonster.getEffectivePower()).isEqualTo(6);
        assertThat(rustMonster.getEffectiveToughness()).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Leather Armor");
    }

    @Test
    void cannotSacrificeAnOpponentsArtifact() {
        harness.addToBattlefield(player1, new RustMonster());
        harness.addToBattlefield(player2, new LeatherArmor());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");

        harness.assertOnBattlefield(player2, "Leather Armor");
    }

    @Test
    void firstStrikeKillsTheBlockerBeforeItCanDealDamage() {
        addCreatureReady(player1, new RustMonster());
        harness.addToBattlefield(player2, new HobgoblinCaptain());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Rust Monster");
        harness.assertInGraveyard(player2, "Hobgoblin Captain");
        harness.assertLife(player2, 20);
    }
}
