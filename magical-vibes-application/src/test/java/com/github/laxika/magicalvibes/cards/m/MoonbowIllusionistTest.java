package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoonbowIllusionistTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a land as a cost and changes the target land's basic type")
    void returnsLandAndChangesTargetType() {
        harness.addToBattlefield(player1, new MoonbowIllusionist());
        harness.addToBattlefield(player1, new Island());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, "Moonbow Illusionist"), 0, target.getId());

        harness.assertInHand(player1, "Island");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ISLAND");

        assertThat(target.getTransientLandTypeOverride()).isEqualTo(CardSubtype.ISLAND);
        assertThat(target.getTransientSubtypes()).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without a land to return")
    void cannotActivateWithoutLand() {
        harness.addToBattlefield(player1, new MoonbowIllusionist());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Moonbow Illusionist"), 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-land permanent")
    void cannotTargetNonLand() {
        harness.addToBattlefield(player1, new MoonbowIllusionist());
        harness.addToBattlefield(player1, new Island());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, "Moonbow Illusionist"), 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("The type change wears off at end of turn")
    void typeChangeWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new MoonbowIllusionist());
        harness.addToBattlefield(player1, new Island());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, "Moonbow Illusionist"), 0, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ISLAND");

        assertThat(target.getTransientLandTypeOverride()).isEqualTo(CardSubtype.ISLAND);
        target.resetModifiers();

        assertThat(target.getTransientLandTypeOverride()).isNull();
    }

    private int battlefieldIndex(Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).indexOf(findPermanent(owner, name));
    }
}
