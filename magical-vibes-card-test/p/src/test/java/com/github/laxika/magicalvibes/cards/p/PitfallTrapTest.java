package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.w.WelkinTern;
import com.github.laxika.magicalvibes.cards.v.VastwoodGorger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PitfallTrap.class, VastwoodGorger.class, WelkinTern.class})
class PitfallTrapTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys one attacking creature without flying")
    void destroysAttackingCreatureWithoutFlying() {
        Permanent attacker = addAttacker(new VastwoodGorger());
        harness.setHand(player1, List.of(new PitfallTrap()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, attacker.getId());

        harness.assertInGraveyard(player2, "Vastwood Gorger");
        harness.assertInGraveyard(player1, "Pitfall Trap");
    }

    @Test
    @DisplayName("Can be cast for {W} when exactly one creature is attacking")
    void castsForAlternateCostWithExactlyOneAttacker() {
        Permanent attacker = addAttacker(new VastwoodGorger());
        harness.setHand(player1, List.of(new PitfallTrap()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castWithAlternateCost(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Vastwood Gorger");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Alternate cost requires exactly one attacking creature")
    void alternateCostRequiresExactlyOneAttacker() {
        addAttacker(new VastwoodGorger());
        addAttacker(new VastwoodGorger());
        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.setHand(player1, List.of(new PitfallTrap()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an attacking creature with flying")
    void cannotTargetAttackingCreatureWithFlying() {
        Permanent flyer = addAttacker(new WelkinTern());
        harness.setHand(player1, List.of(new PitfallTrap()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, flyer.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Normal cost remains available with multiple attackers")
    void normalCostWithMultipleAttackers() {
        Permanent attacker = addAttacker(new VastwoodGorger());
        Permanent other = addAttacker(new VastwoodGorger());
        harness.setHand(player1, List.of(new PitfallTrap()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, attacker.getId());

        harness.assertInGraveyard(player2, "Vastwood Gorger");
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(other);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Flying attackers also count against the alternate cost")
    void flyingAttackerPreventsAlternateCost() {
        Permanent attacker = addAttacker(new VastwoodGorger());
        addAttacker(new WelkinTern());
        harness.setHand(player1, List.of(new PitfallTrap()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Pitfall Trap");
    }

    @Test
    @DisplayName("Cannot target a nonattacking creature")
    void cannotTargetNonattackingCreature() {
        addAttacker(new VastwoodGorger());
        Permanent idle = harness.addToBattlefieldAndReturn(player2, new VastwoodGorger());
        harness.setHand(player1, List.of(new PitfallTrap()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, idle.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Pitfall Trap");
    }

    @Test
    @DisplayName("Nonattacking creatures do not prevent the alternate cost")
    void nonattackingCreaturesDoNotPreventAlternateCost() {
        Permanent attacker = addAttacker(new VastwoodGorger());
        harness.addToBattlefield(player1, new VastwoodGorger());
        harness.addToBattlefield(player2, new WelkinTern());
        harness.setHand(player1, List.of(new PitfallTrap()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castWithAlternateCost(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Vastwood Gorger");
        harness.assertOnBattlefield(player1, "Vastwood Gorger");
        harness.assertOnBattlefield(player2, "Welkin Tern");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A target that stops attacking is not destroyed")
    void targetStopsAttackingBeforeResolution() {
        Permanent attacker = addAttacker(new VastwoodGorger());
        harness.setHand(player1, List.of(new PitfallTrap()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castWithAlternateCost(player1, 0, attacker.getId());
        attacker.setAttacking(false);
        attacker.setAttackTarget(null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Vastwood Gorger");
        harness.assertNotInGraveyard(player2, "Vastwood Gorger");
        harness.assertInGraveyard(player1, "Pitfall Trap");
    }

    @Test
    @DisplayName("A target that gains flying is not destroyed")
    void targetGainsFlyingBeforeResolution() {
        Permanent attacker = addAttacker(new VastwoodGorger());
        harness.setHand(player1, List.of(new PitfallTrap()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castWithAlternateCost(player1, 0, attacker.getId());
        attacker.getPersistentGrantedKeywords().add(Keyword.FLYING);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Vastwood Gorger");
        harness.assertNotInGraveyard(player2, "Vastwood Gorger");
        harness.assertInGraveyard(player1, "Pitfall Trap");
    }

    @Test
    @DisplayName("An additional attacker after casting does not invalidate the spell")
    void alternateCostConditionIsNotRecheckedOnResolution() {
        Permanent attacker = addAttacker(new VastwoodGorger());
        harness.setHand(player1, List.of(new PitfallTrap()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castWithAlternateCost(player1, 0, attacker.getId());
        Permanent other = addAttacker(new WelkinTern());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Vastwood Gorger");
        harness.assertInGraveyard(player1, "Pitfall Trap");
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(other);
    }

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Card card) {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, card);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }
}
