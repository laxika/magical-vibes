package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.n.NettleSwine;
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

@CardUsed({MaliciousIntent.class, NettleSwine.class})
class MaliciousIntentTest extends BaseCardTest {

    private Permanent setUpEnchantedCreature() {
        Permanent bears = addCreatureReady(player1, new NettleSwine());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MaliciousIntent());
        aura.setAttachedTo(bears.getId());
        return bears;
    }

    @Test
    @DisplayName("Granted ability makes the target creature unable to block")
    void grantedAbilityMakesTargetCantBlock() {
        setUpEnchantedCreature();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NettleSwine());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Activating the granted ability taps the enchanted creature")
    void grantedAbilityTapsEnchantedCreature() {
        Permanent bears = setUpEnchantedCreature();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NettleSwine());

        harness.activateAbility(player1, 0, 0, null, target.getId());

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Granted ability goes away when the aura leaves the battlefield")
    void grantedAbilityRemovedWhenAuraLeaves() {
        Permanent bears = setUpEnchantedCreature();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Malicious Intent"));

        assertThat(gqs.computeStaticBonus(gd, bears).grantedActivatedAbilities()).isEmpty();
    }

    @Test
    void castingAuraGrantsAbilityToOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new NettleSwine());
        harness.setHand(player1, List.of(new MaliciousIntent()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Malicious Intent").getAttachedTo()).isEqualTo(creature.getId());
        harness.activateAbility(player2, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.isCantBlockThisTurn()).isTrue();
    }

    @Test
    void summoningSickCreatureCannotPayTapCost() {
        Permanent creature = setUpEnchantedCreature();
        creature.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedCreatureCannotActivateAgain() {
        Permanent creature = setUpEnchantedCreature();
        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    void abilityResolvesAfterAuraLeavesBattlefield() {
        setUpEnchantedCreature();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NettleSwine());
        harness.activateAbility(player1, 0, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Malicious Intent"));

        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    void grantedAbilityCannotTargetNoncreatureAura() {
        Permanent creature = setUpEnchantedCreature();
        Permanent aura = findPermanent(player1, "Malicious Intent");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, aura.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityDoesNothingWhenTargetLeavesBeforeResolution() {
        setUpEnchantedCreature();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NettleSwine());
        harness.activateAbility(player1, 0, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        Permanent replacement = harness.addToBattlefieldAndReturn(player2, new NettleSwine());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(replacement.isCantBlockThisTurn()).isFalse();
    }

    @Test
    void restrictionExpiresAtEndOfTurn() {
        setUpEnchantedCreature();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NettleSwine());
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.isCantBlockThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(target.isCantBlockThisTurn()).isFalse();
    }
}
