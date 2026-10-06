package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CinderPyromancer;
import com.github.laxika.magicalvibes.cards.i.IdleThoughts;
import com.github.laxika.magicalvibes.cards.i.IndigoFaerie;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShellSkulkin.class, IndigoFaerie.class, CinderPyromancer.class, IdleThoughts.class})
class ShellSkulkinTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability grants shroud to blue creature")
    void resolvingGrantsShroudToBlueCreature() {
        addReadySkulkin(player1);
        Permanent target = addReadyBlueCreature(player1);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Can target opponent's blue creature")
    void canTargetOpponentBlueCreature() {
        addReadySkulkin(player1);
        Permanent target = addReadyBlueCreature(player2);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Shroud is removed at end of turn")
    void shroudRemovedAtEndOfTurn() {
        addReadySkulkin(player1);
        Permanent target = addReadyBlueCreature(player1);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.hasKeyword(Keyword.SHROUD)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Cannot target non-blue creature")
    void cannotTargetNonBlueCreature() {
        addReadySkulkin(player1);
        Permanent target = addReadyRedCreature(player1);
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a blue creature");
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadySkulkin(player1);
        Permanent target = addReadyBlueCreature(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot target a blue noncreature permanent")
    void cannotTargetBlueNoncreature() {
        addReadySkulkin(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new IdleThoughts());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a blue creature");
    }

    @Test
    @DisplayName("Cannot target the colorless Shell Skulkin itself")
    void cannotTargetColorlessCreature() {
        Permanent skulkin = addReadySkulkin(player1);
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, skulkin.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a blue creature");
    }

    @Test
    @DisplayName("Ability works while tapped and summoning sick, with nonblue mana")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent skulkin = harness.addToBattlefieldAndReturn(player1, new ShellSkulkin());
        skulkin.setSummoningSick(true);
        skulkin.tap();
        Permanent target = addReadyBlueCreature(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.SHROUD)).isTrue();
        assertThat(skulkin.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Granted shroud prevents targeting by both players")
    void shroudPreventsBothPlayersFromTargeting() {
        addReadySkulkin(player1);
        Permanent target = addReadyBlueCreature(player1);
        addReadySkulkin(player2);
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
        harness.ensurePriority(player2);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("A red creature made blue in addition to red is a legal target")
    void canTargetCreatureWithBlueAmongItsColors() {
        addReadySkulkin(player1);
        addReadyBlueCreature(player1);
        Permanent target = addReadyRedCreature(player1);
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Ability resolves after Shell Skulkin leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent skulkin = addReadySkulkin(player1);
        Permanent target = addReadyBlueCreature(player1);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(skulkin);
        gd.playerGraveyards.get(player1.getId()).add(skulkin.getCard());

        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Ability does not grant shroud to a target that left the battlefield")
    void doesNotGrantShroudToRemovedTarget() {
        addReadySkulkin(player1);
        Permanent target = addReadyBlueCreature(player1);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.SHROUD)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Shell Skulkin can target itself after becoming blue")
    void canTargetItselfWhenBlue() {
        Permanent skulkin = addReadySkulkin(player1);
        addReadyBlueCreature(player1);
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.activateAbility(player1, 1, null, skulkin.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, skulkin.getId());
        harness.passBothPriorities();

        assertThat(skulkin.hasKeyword(Keyword.SHROUD)).isTrue();
    }

    private Permanent addReadySkulkin(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ShellSkulkin());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addReadyBlueCreature(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new IndigoFaerie());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addReadyRedCreature(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new CinderPyromancer());
        perm.setSummoningSick(false);
        return perm;
    }
}
