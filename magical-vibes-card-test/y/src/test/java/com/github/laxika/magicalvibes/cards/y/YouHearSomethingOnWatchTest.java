package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YouHearSomethingOnWatch.class, GrizzlyBears.class, HillGiantHerdgorger.class})
class YouHearSomethingOnWatchTest extends BaseCardTest {

    @Test
    @DisplayName("Rouses creatures you control until end of turn")
    void rousesOwnCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(0, List.of());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Rouse bonus wears off at cleanup")
    void rouseBonusWearsOffAtCleanup() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(0, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Set Off Traps deals 5 damage to an attacking creature")
    void damagesAttackingCreature() {
        Permanent attacker = addAttacker(player2, player1, new GrizzlyBears());

        cast(1, List.of(attacker.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Set Off Traps cannot target a non-attacking creature")
    void cannotTargetNonAttackingCreature() {
        Permanent nonAttacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSpell();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(nonAttacker.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature");
    }

    private void cast(int modeIndex, List<UUID> targetIds) {
        prepareSpell();
        harness.castModalInstant(player1, 0, modeIndex, targetIds);
        harness.passBothPriorities();
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new YouHearSomethingOnWatch()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Permanent addAttacker(Player controller, Player defender, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(controller, card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        permanent.setAttackTarget(defender.getId());
        return permanent;
    }

    @Test
    @DisplayName("Set Off Traps deals exactly five damage without boosting creatures")
    void dealsExactlyFiveDamage() {
        Permanent attacker = addAttacker(player2, player1, new HillGiantHerdgorger());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HillGiantHerdgorger());

        cast(1, List.of(attacker.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
        assertThat(attacker.getMarkedDamage()).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(6);
    }

    @Test
    @DisplayName("Set Off Traps can target your own attacking creature")
    void canDamageOwnAttacker() {
        Permanent attacker = addAttacker(player1, player2, new HillGiantHerdgorger());

        cast(1, List.of(attacker.getId()));

        assertThat(attacker.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("Set Off Traps does not damage a creature that stops attacking before resolution")
    void rechecksAttackingRestrictionAtResolution() {
        Permanent attacker = addAttacker(player2, player1, new HillGiantHerdgorger());
        prepareSpell();
        harness.castModalInstant(player1, 0, 1, List.of(attacker.getId()));

        attacker.setAttacking(false);
        attacker.setAttackTarget(null);
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "You Hear Something on Watch");
    }

    @Test
    @DisplayName("Rouse the Party does not boost creatures entering after resolution")
    void excludesCreaturesEnteringLater() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new HillGiantHerdgorger());

        cast(0, List.of());
        Permanent later = harness.enterBattlefieldAndReturn(player1, new HillGiantHerdgorger());

        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, later)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, later)).isEqualTo(6);
    }

    @Test
    @DisplayName("Rouse the Party resolves with no creatures or targets")
    void canRouseEmptyBattlefield() {
        cast(0, List.of());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "You Hear Something on Watch");
    }
}
