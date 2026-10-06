package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DimirGuildgate;
import com.github.laxika.magicalvibes.cards.w.WishcoinCrab;
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

@CardUsed({PasswallAdept.class, WishcoinCrab.class, DimirGuildgate.class})
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
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DimirGuildgate());
        addAbilityMana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addCreature(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new WishcoinCrab());
        perm.setSummoningSick(false);
        return perm;
    }

    @Test
    void canTargetItselfWhileSummoningSickAndTapped() {
        Permanent adept = harness.addToBattlefieldAndReturn(player1, new PasswallAdept());
        adept.setSummoningSick(true);
        adept.tap();
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, adept.getId());
        harness.passBothPriorities();

        assertThat(adept.isCantBeBlocked()).isTrue();
    }

    @Test
    void canActivateTwiceForDifferentCreatures() {
        Permanent adept = harness.addToBattlefieldAndReturn(player1, new PasswallAdept());
        Permanent first = addCreature(player1);
        Permanent second = addCreature(player2);
        addAbilityMana(player1);
        addAbilityMana(player1);

        harness.activateAbility(player1, 0, null, first.getId());
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.isCantBeBlocked()).isTrue();
        assertThat(second.isCantBeBlocked()).isTrue();
        assertThat(adept.isTapped()).isFalse();
        assertThat(adept.isCantBeBlocked()).isFalse();
    }

    @Test
    void cannotActivateWithoutBlueMana() {
        Permanent adept = harness.addToBattlefieldAndReturn(player1, new PasswallAdept());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, adept.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(adept.isCantBeBlocked()).isFalse();
    }

    @Test
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent adept = harness.addToBattlefieldAndReturn(player1, new PasswallAdept());
        Permanent target = addCreature(player1);
        addAbilityMana(player1);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(adept);
        gd.playerGraveyards.get(player1.getId()).add(adept.getCard());

        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityDoesNotAffectOtherCreaturesWhenTargetLeavesBattlefield() {
        Permanent adept = harness.addToBattlefieldAndReturn(player1, new PasswallAdept());
        Permanent target = addCreature(player2);
        Permanent other = addCreature(player2);
        addAbilityMana(player1);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(other.isCantBeBlocked()).isFalse();
        assertThat(adept.isCantBeBlocked()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void addAbilityMana(Player player) {
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
