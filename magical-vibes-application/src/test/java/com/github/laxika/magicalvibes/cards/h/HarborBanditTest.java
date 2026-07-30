package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HarborBanditTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 while its controller controls an Island")
    void getsBoostWithIsland() {
        harness.addToBattlefield(player1, new HarborBandit());
        harness.addToBattlefield(player1, new Island());

        Permanent bandit = findPermanent(player1, "Harbor Bandit");
        assertThat(gqs.getEffectivePower(gd, bandit)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bandit)).isEqualTo(3);
    }

    @Test
    @DisplayName("No boost without an Island")
    void noBoostWithoutIsland() {
        harness.addToBattlefield(player1, new HarborBandit());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bandit = findPermanent(player1, "Harbor Bandit");
        assertThat(gqs.getEffectivePower(gd, bandit)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bandit)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's Island does not grant the boost")
    void opponentIslandDoesNotCount() {
        harness.addToBattlefield(player1, new HarborBandit());
        harness.addToBattlefield(player2, new Island());

        Permanent bandit = findPermanent(player1, "Harbor Bandit");
        assertThat(gqs.getEffectivePower(gd, bandit)).isEqualTo(2);
    }

    @Test
    @DisplayName("Loses the boost when the Island leaves the battlefield")
    void losesBoostWhenIslandLeaves() {
        harness.addToBattlefield(player1, new HarborBandit());
        harness.addToBattlefield(player1, new Island());

        Permanent bandit = findPermanent(player1, "Harbor Bandit");
        assertThat(gqs.getEffectivePower(gd, bandit)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Island"));

        assertThat(gqs.getEffectivePower(gd, bandit)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activated ability makes Harbor Bandit unblockable this turn")
    void abilityMakesUnblockable() {
        Permanent bandit = addBanditReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(bandit.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Unblockable wears off at end of turn")
    void unblockableWearsOff() {
        Permanent bandit = addBanditReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(bandit.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bandit.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Ability cannot be activated without enough mana")
    void cannotActivateWithoutMana() {
        addBanditReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(RuntimeException.class);

        assertThat(gd.stack).isEmpty();
    }

    private Permanent addBanditReady(Player player) {
        Permanent perm = new Permanent(new HarborBandit());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
