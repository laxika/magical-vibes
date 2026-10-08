package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.Ghostfire;
import com.github.laxika.magicalvibes.cards.i.Imperiosaur;
import com.github.laxika.magicalvibes.cards.n.NimbusMaze;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WitchsMist.class, Imperiosaur.class, Ghostfire.class, NimbusMaze.class})
class WitchsMistTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creature that was dealt damage this turn")
    void destroysDamagedCreature() {
        harness.addToBattlefield(player1, new WitchsMist());
        Permanent imperiosaur = harness.addToBattlefieldAndReturn(player2, new Imperiosaur());
        gd.permanentsDealtDamageThisTurn.add(imperiosaur.getId());
        addActivationMana();

        harness.activateAbility(player1, 0, null, imperiosaur.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Imperiosaur");
        harness.assertInGraveyard(player2, "Imperiosaur");
    }

    @Test
    @DisplayName("Destroys a creature dealt damage by a spell this turn")
    void destroysCreatureDealtDamageBySpell() {
        harness.addToBattlefield(player1, new WitchsMist());
        Permanent imperiosaur = harness.addToBattlefieldAndReturn(player2, new Imperiosaur());

        harness.setHand(player1, List.of(new Ghostfire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, imperiosaur.getId());

        addActivationMana();
        harness.activateAbility(player1, 0, null, imperiosaur.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Imperiosaur");
        harness.assertInGraveyard(player2, "Imperiosaur");
    }

    @Test
    @DisplayName("Cannot target a creature that was not dealt damage this turn")
    void cannotTargetUndamagedCreature() {
        harness.addToBattlefield(player1, new WitchsMist());
        Permanent imperiosaur = harness.addToBattlefieldAndReturn(player2, new Imperiosaur());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, imperiosaur.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature that was dealt damage this turn");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent even if it was dealt damage this turn")
    void cannotTargetDamagedNoncreaturePermanent() {
        harness.addToBattlefield(player1, new WitchsMist());
        Permanent maze = harness.addToBattlefieldAndReturn(player2, new NimbusMaze());
        gd.permanentsDealtDamageThisTurn.add(maze.getId());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, maze.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature that was dealt damage this turn");
    }

    @Test
    @DisplayName("A regeneration shield saves the creature")
    void regenerationShieldSavesTheCreature() {
        harness.addToBattlefield(player1, new WitchsMist());
        Permanent imperiosaur = harness.addToBattlefieldAndReturn(player2, new Imperiosaur());
        imperiosaur.setRegenerationShield(1);
        gd.permanentsDealtDamageThisTurn.add(imperiosaur.getId());
        addActivationMana();

        harness.activateAbility(player1, 0, null, imperiosaur.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(imperiosaur);
    }

    @Test
    @DisplayName("Activation taps Witch's Mist before the ability resolves")
    void activationPaysTapCost() {
        Permanent mist = harness.addToBattlefieldAndReturn(player1, new WitchsMist());
        Permanent imperiosaur = harness.addToBattlefieldAndReturn(player2, new Imperiosaur());
        gd.permanentsDealtDamageThisTurn.add(imperiosaur.getId());
        addActivationMana();

        harness.activateAbility(player1, 0, null, imperiosaur.getId());

        assertThat(mist.isTapped()).isTrue();
        harness.assertOnBattlefield(player2, "Imperiosaur");
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Imperiosaur");
    }

    @Test
    @DisplayName("A tapped Witch's Mist cannot activate")
    void cannotActivateWhileTapped() {
        Permanent mist = harness.addToBattlefieldAndReturn(player1, new WitchsMist());
        mist.tap();
        Permanent imperiosaur = harness.addToBattlefieldAndReturn(player2, new Imperiosaur());
        gd.permanentsDealtDamageThisTurn.add(imperiosaur.getId());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, imperiosaur.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        harness.assertOnBattlefield(player2, "Imperiosaur");
    }

    @Test
    @DisplayName("Activation requires black mana")
    void cannotPayWithOnlyColorlessMana() {
        Permanent mist = harness.addToBattlefieldAndReturn(player1, new WitchsMist());
        Permanent imperiosaur = harness.addToBattlefieldAndReturn(player2, new Imperiosaur());
        gd.permanentsDealtDamageThisTurn.add(imperiosaur.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, imperiosaur.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mist.isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Imperiosaur");
    }

    @Test
    @DisplayName("Can destroy its controller's damaged creature")
    void canTargetOwnDamagedCreature() {
        harness.addToBattlefield(player1, new WitchsMist());
        Permanent imperiosaur = harness.addToBattlefieldAndReturn(player1, new Imperiosaur());
        gd.permanentsDealtDamageThisTurn.add(imperiosaur.getId());
        addActivationMana();

        harness.activateAbility(player1, 0, null, imperiosaur.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Imperiosaur");
        harness.assertInGraveyard(player1, "Imperiosaur");
    }

    @Test
    @DisplayName("Regeneration removes marked damage but does not erase damage history")
    void regeneratedCreatureRemainsEligibleForAnotherMist() {
        harness.addToBattlefield(player1, new WitchsMist());
        harness.addToBattlefield(player1, new WitchsMist());
        Permanent imperiosaur = harness.addToBattlefieldAndReturn(player2, new Imperiosaur());
        harness.setHand(player1, List.of(new Ghostfire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, imperiosaur.getId());
        imperiosaur.setRegenerationShield(1);
        addActivationMana();

        harness.activateAbility(player1, 0, null, imperiosaur.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Imperiosaur");

        addActivationMana();
        harness.activateAbility(player1, 1, null, imperiosaur.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Imperiosaur");
        harness.assertInGraveyard(player2, "Imperiosaur");
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
