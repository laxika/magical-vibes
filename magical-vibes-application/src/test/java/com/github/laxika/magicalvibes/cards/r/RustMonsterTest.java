package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RustMonster.class, Spellbook.class})
class RustMonsterTest extends BaseCardTest {

    @Test
    void sacrificingAnArtifactBoostsRustMonster() {
        harness.addToBattlefield(player1, new RustMonster());
        harness.addToBattlefield(player1, new Spellbook());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent rustMonster = findPermanent(player1, "Rust Monster");
        assertThat(rustMonster.getEffectivePower()).isEqualTo(4);
        assertThat(rustMonster.getEffectiveToughness()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    void boostExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new RustMonster());
        harness.addToBattlefield(player1, new Spellbook());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent rustMonster = findPermanent(player1, "Rust Monster");
        assertThat(rustMonster.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(rustMonster.getEffectivePower()).isEqualTo(2);
    }

    @Test
    void choosingWhichArtifactToSacrifice() {
        harness.addToBattlefield(player1, new RustMonster());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Spellbook());

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
}
