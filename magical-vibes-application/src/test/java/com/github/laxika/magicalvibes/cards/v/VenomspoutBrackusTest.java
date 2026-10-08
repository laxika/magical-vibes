package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AscendingAven;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VenomspoutBrackus.class, AscendingAven.class, GlorySeeker.class})
class VenomspoutBrackusTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage to an attacking creature with flying")
    void damagesAttackingFlyer() {
        Permanent brackus = addCreatureReady(player1, new VenomspoutBrackus());
        Permanent attacker = addCreatureReady(player2, new AscendingAven());
        attacker.setAttacking(true);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(brackus.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Ascending Aven");
    }

    @Test
    @DisplayName("Deals 5 damage to a blocking creature with flying")
    void damagesBlockingFlyer() {
        addCreatureReady(player1, new VenomspoutBrackus());
        Permanent blocker = addCreatureReady(player2, new AscendingAven());
        blocker.setBlocking(true);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Ascending Aven");
    }

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetNonFlyingCreature() {
        addCreatureReady(player1, new VenomspoutBrackus());
        Permanent attacker = addCreatureReady(player2, new GlorySeeker());
        attacker.setAttacking(true);
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Cannot target a flyer that is neither attacking nor blocking")
    void cannotTargetNonCombatFlyer() {
        addCreatureReady(player1, new VenomspoutBrackus());
        Permanent flyer = addCreatureReady(player2, new AscendingAven());
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, flyer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking");
    }

    @Test
    @DisplayName("Fizzles if the target stops attacking before resolution")
    void fizzlesIfTargetStopsAttackingBeforeResolution() {
        addCreatureReady(player1, new VenomspoutBrackus());
        Permanent attacker = addCreatureReady(player2, new AscendingAven());
        attacker.setAttacking(true);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new VenomspoutBrackus()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent brackus = findPermanent(player1, "Venomspout Brackus");
        assertThat(brackus.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(brackus));
        harness.passBothPriorities();

        assertThat(brackus.isFaceDown()).isFalse();
    }

    @Test
    void canDamageOwnAttackingFlyer() {
        addCreatureReady(player1, new VenomspoutBrackus());
        Permanent attacker = addCreatureReady(player1, new AscendingAven());
        attacker.setAttacking(true);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ascending Aven");
    }

    @Test
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent brackus = addCreatureReady(player1, new VenomspoutBrackus());
        Permanent attacker = addCreatureReady(player2, new AscendingAven());
        attacker.setAttacking(true);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, brackus));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Venomspout Brackus");
        harness.assertInGraveyard(player2, "Ascending Aven");
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent brackus = addCreatureReady(player1, new VenomspoutBrackus());
        brackus.tap();
        Permanent attacker = addCreatureReady(player2, new AscendingAven());
        attacker.setAttacking(true);
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent brackus = addCreatureReady(player1, new VenomspoutBrackus());
        brackus.setSummoningSick(true);
        Permanent attacker = addCreatureReady(player2, new AscendingAven());
        attacker.setAttacking(true);
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(brackus.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void faceDownBrackusCannotUseDamageAbility() {
        Permanent brackus = addCreatureReady(player1, new VenomspoutBrackus());
        brackus.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        Permanent attacker = addCreatureReady(player2, new AscendingAven());
        attacker.setAttacking(true);
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(brackus.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTurnFaceUpWithoutSecondGreenMana() {
        Permanent brackus = addCreatureReady(player1, new VenomspoutBrackus());
        brackus.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(brackus.isFaceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void turningFaceUpMakesDamageAbilityAvailableWithoutUsingStack() {
        Permanent brackus = addCreatureReady(player1, new VenomspoutBrackus());
        brackus.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        Permanent attacker = addCreatureReady(player2, new AscendingAven());
        attacker.setAttacking(true);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.turnFaceUp(player1, 0);

        assertThat(brackus.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
        addAbilityMana();
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(brackus.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Ascending Aven");
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
