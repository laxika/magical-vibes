package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.t.TrollAscetic;
import com.github.laxika.magicalvibes.cards.w.WitheredWretch;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeathmarkPrelate.class, WitheredWretch.class, FugitiveWizard.class, Swamp.class, TrollAscetic.class})
class DeathmarkPrelateTest extends BaseCardTest {

    @Test
    @DisplayName("Taps, sacrifices a Zombie, and destroys the target non-Zombie creature")
    void sacrificesZombieAndDestroysNonZombieCreature() {
        Permanent prelate = addCreatureReady(player1, new DeathmarkPrelate());
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new WitheredWretch());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard()).getId();

        addAbilityMana(player1);
        harness.activateAbility(player1, battlefieldIndex(player1, prelate), null, targetId);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(zombie);
        harness.passBothPriorities();

        assertThat(prelate.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Withered Wretch");
        harness.assertInGraveyard(player2, "Fugitive Wizard");
    }

    @Test
    @DisplayName("Cannot target a Zombie creature")
    void cannotTargetZombieCreature() {
        Permanent prelate = addCreatureReady(player1, new DeathmarkPrelate());
        harness.addToBattlefield(player1, new WitheredWretch());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new WitheredWretch()).getId();

        addAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, prelate), null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without a Zombie to sacrifice")
    void cannotActivateWithoutZombie() {
        Permanent prelate = addCreatureReady(player1, new DeathmarkPrelate());
        harness.addToBattlefield(player1, new FugitiveWizard());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard()).getId();

        addAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, prelate), null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate only as a sorcery")
    void cannotActivateOutsideMainPhase() {
        Permanent prelate = addCreatureReady(player1, new DeathmarkPrelate());
        harness.addToBattlefield(player1, new WitheredWretch());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard()).getId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, prelate), null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        Permanent prelate = addCreatureReady(player1, new DeathmarkPrelate());
        harness.addToBattlefield(player1, new WitheredWretch());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Swamp()).getId();

        addAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, prelate), null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroys a creature even when it has a regeneration shield")
    void cannotBeRegenerated() {
        Permanent prelate = addCreatureReady(player1, new DeathmarkPrelate());
        harness.addToBattlefield(player1, new WitheredWretch());
        Permanent troll = addCreatureReady(player1, new TrollAscetic());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, battlefieldIndex(player1, troll), null, null);
        harness.passBothPriorities();

        addAbilityMana(player1);
        harness.activateAbility(player1, battlefieldIndex(player1, prelate), null, troll.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Troll Ascetic");
        harness.assertInGraveyard(player1, "Troll Ascetic");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's Zombie")
    void cannotSacrificeOpponentsZombie() {
        Permanent prelate = addCreatureReady(player1, new DeathmarkPrelate());
        harness.addToBattlefield(player2, new WitheredWretch());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard()).getId();
        addAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, prelate), null, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Withered Wretch");
        assertThat(prelate.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent prelate = harness.addToBattlefieldAndReturn(player1, new DeathmarkPrelate());
        prelate.setSummoningSick(true);
        harness.addToBattlefield(player1, new WitheredWretch());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard()).getId();
        addAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, prelate), null, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Withered Wretch");
        assertThat(prelate.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent prelate = addCreatureReady(player1, new DeathmarkPrelate());
        prelate.tap();
        harness.addToBattlefield(player1, new WitheredWretch());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard()).getId();
        addAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, prelate), null, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Withered Wretch");
    }

    @Test
    @DisplayName("Cannot activate during an opponent's main phase")
    void cannotActivateDuringOpponentsMainPhase() {
        Permanent prelate = addCreatureReady(player1, new DeathmarkPrelate());
        harness.addToBattlefield(player1, new WitheredWretch());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard()).getId();
        addAbilityMana(player1);
        harness.forceActivePlayer(player2);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, prelate), null, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Withered Wretch");
        assertThat(prelate.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate with another ability on the stack")
    void cannotActivateWithNonemptyStack() {
        Permanent prelate = addCreatureReady(player1, new DeathmarkPrelate());
        harness.addToBattlefield(player1, new WitheredWretch());
        Permanent troll = addCreatureReady(player1, new TrollAscetic());
        addAbilityMana(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, battlefieldIndex(player1, troll), null, null);
        assertThat(gd.stack).isNotEmpty();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, prelate), null, troll.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Withered Wretch");
        assertThat(prelate.isTapped()).isFalse();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Cannot activate without the black mana in its cost")
    void cannotActivateWithoutBlackMana() {
        Permanent prelate = addCreatureReady(player1, new DeathmarkPrelate());
        harness.addToBattlefield(player1, new WitheredWretch());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard()).getId();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, prelate), null, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Withered Wretch");
        assertThat(prelate.isTapped()).isFalse();
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private void addAbilityMana(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.addMana(player, ManaColor.BLACK, 1);
    }
}
