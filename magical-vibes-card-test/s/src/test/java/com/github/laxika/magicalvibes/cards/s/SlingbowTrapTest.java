package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VampireNighthawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlingbowTrap.class, SuntailHawk.class, VampireNighthawk.class, GrizzlyBears.class})
class SlingbowTrapTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys an attacking creature with flying")
    void destroysAttackingCreatureWithFlying() {
        Permanent attacker = addAttacker(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new SlingbowTrap()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Suntail Hawk");
        harness.assertInGraveyard(player1, "Slingbow Trap");
    }

    @Test
    @DisplayName("Can be cast for {G} when a black creature with flying is attacking")
    void castsForAlternateCostWhenBlackFlyingCreatureIsAttacking() {
        Permanent blackAttacker = addAttacker(player2, new VampireNighthawk());
        Permanent target = addAttacker(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new SlingbowTrap()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithAlternateCost(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Suntail Hawk");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blackAttacker);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Alternate cost requires a black attacking creature with flying")
    void alternateCostRequiresBlackFlyingAttacker() {
        Permanent attacker = addAttacker(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new SlingbowTrap()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("condition is not met");
    }

    @Test
    @DisplayName("Cannot target a non-flying attacker")
    void cannotTargetNonFlyingAttacker() {
        Permanent attacker = addAttacker(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SlingbowTrap()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("May pay the normal mana cost even when the alternate cost is available")
    void canPayNormalCostWhenAlternateCostIsAvailable() {
        Permanent target = addAttacker(player2, new VampireNighthawk());
        harness.setHand(player1, List.of(new SlingbowTrap()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Vampire Nighthawk");
        harness.assertInGraveyard(player1, "Slingbow Trap");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot target a creature with flying that is not attacking")
    void cannotTargetNonattackingFlyer() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new SlingbowTrap()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A nonattacking black flyer does not enable the alternate cost")
    void nonattackingBlackFlyerDoesNotEnableAlternateCost() {
        harness.addToBattlefield(player2, new VampireNighthawk());
        Permanent target = addAttacker(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new SlingbowTrap()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("condition is not met");
    }

    @Test
    @DisplayName("A black attacker that lost flying does not enable the alternate cost")
    void blackAttackerWithoutFlyingDoesNotEnableAlternateCost() {
        Permanent blackAttacker = addAttacker(player2, new VampireNighthawk());
        blackAttacker.getRemovedKeywords().add(Keyword.FLYING);
        Permanent target = addAttacker(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new SlingbowTrap()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("condition is not met");
    }

    @Test
    @DisplayName("Does not destroy a target that stops attacking before resolution")
    void targetMustStillBeAttackingOnResolution() {
        Permanent target = addAttacker(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new SlingbowTrap()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, target.getId());

        target.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Suntail Hawk");
        harness.assertNotInGraveyard(player2, "Suntail Hawk");
        harness.assertInGraveyard(player1, "Slingbow Trap");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not destroy a target that loses flying before resolution")
    void targetMustStillHaveFlyingOnResolution() {
        Permanent target = addAttacker(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new SlingbowTrap()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, target.getId());

        target.getRemovedKeywords().add(Keyword.FLYING);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Suntail Hawk");
        harness.assertNotInGraveyard(player2, "Suntail Hawk");
        harness.assertInGraveyard(player1, "Slingbow Trap");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The alternate-cost condition need not remain true on resolution")
    void alternateCostConditionIsNotRecheckedOnResolution() {
        Permanent blackAttacker = addAttacker(player2, new VampireNighthawk());
        Permanent target = addAttacker(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new SlingbowTrap()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castWithAlternateCost(player1, 0, target.getId());

        blackAttacker.setAttacking(false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Suntail Hawk");
        harness.assertOnBattlefield(player2, "Vampire Nighthawk");
        harness.assertInGraveyard(player1, "Slingbow Trap");
    }

    @Test
    @DisplayName("The caster's own black flying attacker enables the alternate cost and is a legal target")
    void canDestroyOwnBlackFlyingAttackerForAlternateCost() {
        Permanent target = addAttacker(player1, new VampireNighthawk());
        target.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new SlingbowTrap()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithAlternateCost(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Vampire Nighthawk");
        harness.assertInGraveyard(player1, "Slingbow Trap");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Player owner, Card card) {
        Permanent attacker = harness.addToBattlefieldAndReturn(owner, card);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }
}
