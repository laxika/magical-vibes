package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.SpinelessThug;
import com.github.laxika.magicalvibes.cards.p.PlatinumAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MurderousBetrayal.class, MoggToady.class, SpinelessThug.class, PlatinumAngel.class})
class MurderousBetrayalTest extends BaseCardTest {

    private Permanent betrayal(int life) {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new MurderousBetrayal());
        harness.setLife(player1, life);
        harness.addMana(player1, ManaColor.BLACK, 2);
        return enchantment;
    }

    @Test
    @DisplayName("Destroys target nonblack creature and pays half life rounded up")
    void destroysNonblackCreature() {
        Permanent enchantment = betrayal(20);
        Permanent toady = harness.addToBattlefieldAndReturn(player2, new MoggToady());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(enchantment);
        harness.activateAbility(player1, idx, null, toady.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertNotOnBattlefield(player2, "Mogg Toady");
        harness.assertInGraveyard(player2, "Mogg Toady");
    }

    @Test
    @DisplayName("Half-life cost rounds up on odd life totals")
    void roundsUpOddLife() {
        Permanent enchantment = betrayal(21);
        Permanent toady = harness.addToBattlefieldAndReturn(player2, new MoggToady());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(enchantment);
        harness.activateAbility(player1, idx, null, toady.getId());
        harness.passBothPriorities();

        // 21 / 2 rounded up = 11 paid, 10 remaining
        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent enchantment = betrayal(20);
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new SpinelessThug());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(enchantment);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, zombie.getId()))
                .isInstanceOf(IllegalStateException.class);

        // Life untouched because the illegal activation rewinds with no cost paid.
        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player2, "Spineless Thug");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent enchantment = betrayal(20);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MurderousBetrayal());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(enchantment);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player2, "Murderous Betrayal");
    }

    @Test
    @DisplayName("Requires two black mana to activate")
    void requiresTwoBlackMana() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new MurderousBetrayal());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        Permanent toady = harness.addToBattlefieldAndReturn(player2, new MoggToady());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(enchantment);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, toady.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player2, "Mogg Toady");
    }

    @Test
    @DisplayName("Destroys a nonblack creature despite a regeneration shield")
    void cannotBeRegenerated() {
        Permanent enchantment = betrayal(20);
        Permanent toady = harness.addToBattlefieldAndReturn(player2, new MoggToady());
        toady.setRegenerationShield(1);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(enchantment);
        harness.activateAbility(player1, idx, null, toady.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mogg Toady");
        harness.assertInGraveyard(player2, "Mogg Toady");
    }

    @Test
    @DisplayName("Pays costs even when the target becomes illegal before resolution")
    void paysCostsWhenTargetBecomesIllegalBeforeResolution() {
        Permanent enchantment = betrayal(20);
        Permanent toady = harness.addToBattlefieldAndReturn(player2, new MoggToady());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(enchantment);
        harness.activateAbility(player1, idx, null, toady.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, toady));
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        harness.assertInGraveyard(player2, "Mogg Toady");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can destroy a nonblack creature you control")
    void destroysOwnCreature() {
        Permanent enchantment = betrayal(20);
        Permanent toady = harness.addToBattlefieldAndReturn(player1, new MoggToady());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(enchantment);
        harness.activateAbility(player1, idx, null, toady.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
        harness.assertNotOnBattlefield(player1, "Mogg Toady");
        harness.assertInGraveyard(player1, "Mogg Toady");
        harness.assertOnBattlefield(player1, "Murderous Betrayal");
    }

    @Test
    @DisplayName("Each activation recalculates the life cost and does not tap the enchantment")
    void repeatedActivationsRecalculateLifeCost() {
        Permanent enchantment = betrayal(21);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new MoggToady());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new MoggToady());
        harness.addMana(player1, ManaColor.BLACK, 2);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(enchantment);
        harness.activateAbility(player1, idx, null, first.getId());
        harness.assertLife(player1, 10);
        harness.activateAbility(player1, idx, null, second.getId());
        harness.assertLife(player1, 5);
        assertThat(enchantment.isTapped()).isFalse();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mogg Toady");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Mogg Toady"))
                .hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Can pay zero life at zero life while Platinum Angel prevents losing")
    void activatesAtZeroLife() {
        harness.addToBattlefield(player1, new PlatinumAngel());
        Permanent enchantment = betrayal(0);
        Permanent toady = harness.addToBattlefieldAndReturn(player2, new MoggToady());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(enchantment);
        harness.activateAbility(player1, idx, null, toady.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 0);
        harness.assertNotOnBattlefield(player2, "Mogg Toady");
        harness.assertInGraveyard(player2, "Mogg Toady");
    }

    @Test
    @DisplayName("Can pay zero life at minus one life while Platinum Angel prevents losing")
    void activatesAtNegativeLifeWithZeroRoundedCost() {
        harness.addToBattlefield(player1, new PlatinumAngel());
        Permanent enchantment = betrayal(-1);
        Permanent toady = harness.addToBattlefieldAndReturn(player2, new MoggToady());

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(enchantment);
        harness.activateAbility(player1, idx, null, toady.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, -1);
        harness.assertNotOnBattlefield(player2, "Mogg Toady");
        harness.assertInGraveyard(player2, "Mogg Toady");
    }
}
