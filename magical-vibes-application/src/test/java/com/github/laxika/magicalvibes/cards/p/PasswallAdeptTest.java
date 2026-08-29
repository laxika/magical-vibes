package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswallAdeptTest extends BaseCardTest {

    @Test
    @DisplayName("Ability makes target creature unblockable this turn")
    void makesTargetCreatureUnblockable() {
        harness.addToBattlefieldAndReturn(player1, new PasswallAdept());
        Permanent target = addCreature(player1);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Ability can target an opponent's creature")
    void targetsOpponentCreature() {
        harness.addToBattlefieldAndReturn(player1, new PasswallAdept());
        Permanent target = addCreature(player2);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Unblockable wears off at end of turn")
    void unblockableWearsOff() {
        harness.addToBattlefieldAndReturn(player1, new PasswallAdept());
        Permanent target = addCreature(player1);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefieldAndReturn(player1, new PasswallAdept());
        Permanent target = new Permanent(new Forest());
        harness.getGameData().playerBattlefields.get(player1.getId()).add(target);
        addAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addCreature(Player player) {
        Permanent perm = new Permanent(new RagingGoblin());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void addAbilityMana(Player player) {
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
