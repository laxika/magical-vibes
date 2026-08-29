package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EliteHeadhunter.class, GrizzlyBears.class, Spellbook.class})
class EliteHeadhunterTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature deals 2 damage to a target creature")
    void sacrificesCreatureAndDealsDamage() {
        harness.addToBattlefield(player1, new EliteHeadhunter());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addHybridMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrificing an artifact deals 2 damage to a target creature")
    void sacrificesArtifactAndDealsDamage() {
        harness.addToBattlefield(player1, new EliteHeadhunter());
        harness.addToBattlefield(player1, new Spellbook());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addHybridMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.assertInGraveyard(player1, "Spellbook");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.addToBattlefield(player1, new EliteHeadhunter());
        harness.addToBattlefield(player1, new GrizzlyBears());
        addHybridMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without another creature or an artifact")
    void cannotActivateWithoutSacrifice() {
        harness.addToBattlefield(player1, new EliteHeadhunter());
        addHybridMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature or an artifact");
    }

    private void addHybridMana() {
        harness.addMana(player1, ManaColor.BLACK, 3);
    }
}
