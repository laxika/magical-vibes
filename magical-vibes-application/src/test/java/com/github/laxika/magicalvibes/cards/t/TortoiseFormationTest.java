package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.m.MagmaSpray;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TortoiseFormation.class, CylianElf.class, MagmaSpray.class})
class TortoiseFormationTest extends BaseCardTest {

    @Test
    @DisplayName("Grants shroud to own creatures only")
    void grantsShroudToOwnCreatures() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        Permanent enemy = harness.addToBattlefieldAndReturn(player2, new CylianElf());
        harness.setHand(player1, List.of(new TortoiseFormation()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(own.hasKeyword(Keyword.SHROUD)).isTrue();
        assertThat(enemy.hasKeyword(Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Shroud wears off at end of turn")
    void shroudWearsOffAtEndOfTurn() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        harness.setHand(player1, List.of(new TortoiseFormation()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(own.hasKeyword(Keyword.SHROUD)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(own.hasKeyword(Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Only creatures present when the spell resolves gain shroud")
    void affectsCreaturesAtResolutionOnly() {
        harness.setHand(player1, List.of(new TortoiseFormation()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castInstant(player1, 0);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new CylianElf());

        harness.passBothPriorities();
        Permanent afterResolution = harness.enterBattlefieldAndReturn(player1, new CylianElf());

        assertThat(beforeResolution.hasKeyword(Keyword.SHROUD)).isTrue();
        assertThat(afterResolution.hasKeyword(Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Shroud prevents targeting by either player")
    void preventsBothPlayersFromTargeting() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        harness.setHand(player1, List.of(new TortoiseFormation(), new MagmaSpray()));
        harness.setHand(player2, List.of(new MagmaSpray()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, own.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, own.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Gaining shroud makes an existing targeted spell fail to resolve")
    void protectsAgainstSpellAlreadyOnStack() {
        harness.forceActivePlayer(player2);
        Permanent own = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        harness.setHand(player1, List.of(new TortoiseFormation()));
        harness.setHand(player2, List.of(new MagmaSpray()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, own.getId());
        harness.castInstant(player1, 0);

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cylian Elf");
        assertThat(own.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Magma Spray");
    }
}
