package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BalduvianTradingPost.class, Forest.class, GrizzlyBears.class, Mountain.class})
class BalduvianTradingPostTest extends BaseCardTest {

    @Test
    @DisplayName("Entering sacrifices a chosen untapped Mountain and the land enters")
    void entersBySacrificingUntappedMountain() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new BalduvianTradingPost()));

        harness.playLand(player1, 0);

        harness.handlePermanentChosen(player1, mountain.getId());

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertOnBattlefield(player1, "Balduvian Trading Post");
    }

    @Test
    @DisplayName("Declining the sacrifice puts the land into its owner's graveyard")
    void declinedSacrificeSendsLandToGraveyard() {
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new BalduvianTradingPost()));

        harness.playLand(player1, 0);

        harness.handlePermanentChosen(player1, player1.getId());

        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertNotOnBattlefield(player1, "Balduvian Trading Post");
        harness.assertInGraveyard(player1, "Balduvian Trading Post");
    }

    @Test
    @DisplayName("With no untapped Mountain the land goes straight to the graveyard without a prompt")
    void noUntappedMountainSendsLandToGraveyard() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        mountain.tap();
        harness.setHand(player1, List.of(new BalduvianTradingPost()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Balduvian Trading Post");
        harness.assertInGraveyard(player1, "Balduvian Trading Post");
    }

    @Test
    @DisplayName("An untapped non-Mountain does not satisfy the entry cost")
    void nonMountainCannotBeSacrificed() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new BalduvianTradingPost()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Balduvian Trading Post");
        harness.assertInGraveyard(player1, "Balduvian Trading Post");
    }

    @Test
    @DisplayName("Mana ability adds {C} and {R}")
    void manaAbilityAddsColorlessAndRed() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new BalduvianTradingPost());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Damage ability deals 1 damage to target attacking creature")
    void damageAbilityHitsAttackingCreature() {
        harness.addToBattlefield(player1, new BalduvianTradingPost());
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Damage ability cannot target an attacking noncreature")
    void damageAbilityRejectsAttackingNoncreature() {
        harness.addToBattlefield(player1, new BalduvianTradingPost());
        Permanent attackingMountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        attackingMountain.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, attackingMountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Damage ability does nothing if the target stops attacking before resolution")
    void damageAbilityFizzlesIfTargetStopsAttacking() {
        harness.addToBattlefield(player1, new BalduvianTradingPost());
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Damage ability can't target a non-attacking creature")
    void damageAbilityRejectsNonAttackingCreature() {
        harness.addToBattlefield(player1, new BalduvianTradingPost());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
