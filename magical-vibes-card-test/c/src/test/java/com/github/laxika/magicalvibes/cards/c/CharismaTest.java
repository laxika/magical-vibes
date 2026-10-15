package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AlabasterWall;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.h.HornedTroll;
import com.github.laxika.magicalvibes.cards.k.KrisMage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlabasterWall.class, Charisma.class, CrossbowInfantry.class, Disenchant.class,
        HornedTroll.class, KrisMage.class})
@DisplayName("Charisma")
class CharismaTest extends BaseCardTest {

    @Test
    @DisplayName("Gains control of a creature damaged by the enchanted creature")
    void gainsControlOfDamagedCreature() {
        Permanent enchantedCreature = setUpCombat();
        Permanent damagedCreature = addCreatureReady(player2, new AlabasterWall());
        damagedCreature.setBlocking(true);
        damagedCreature.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(damagedCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(damagedCreature);
        assertThat(enchantedCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Control ends when Charisma leaves the battlefield")
    void controlEndsWhenCharismaLeavesBattlefield() {
        setUpCombat();
        Permanent damagedCreature = addCreatureReady(player2, new AlabasterWall());
        damagedCreature.setBlocking(true);
        damagedCreature.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        Permanent charisma = findPermanent(player1, "Charisma");
        harness.castInstant(player1, 0, charisma.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(damagedCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(damagedCreature);
    }

    @Test
    @DisplayName("Gains control after noncombat damage to another creature")
    void gainsControlAfterNoncombatDamage() {
        Permanent enchantedCreature = addCreatureReady(player1, new CrossbowInfantry());
        attachCharisma(enchantedCreature);
        Permanent damagedCreature = addCreatureReady(player2, new AlabasterWall());
        damagedCreature.setBlocking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.activateAbility(player1, 0, null, damagedCreature.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(damagedCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(damagedCreature);
    }

    @Test
    @DisplayName("Does not trigger when the enchanted creature damages a player")
    void doesNotTriggerOnDamageToPlayer() {
        Permanent enchantedCreature = addCreatureReady(player1, new KrisMage());
        attachCharisma(enchantedCreature);
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when the enchanted creature damages itself")
    void doesNotTriggerOnDamageToItself() {
        CrossbowInfantry card = new CrossbowInfantry();
        card.setToughness(2);
        Permanent enchantedCreature = addCreatureReady(player1, card);
        attachCharisma(enchantedCreature);
        enchantedCreature.setBlocking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.activateAbility(player1, 0, null, enchantedCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(enchantedCreature);
        assertThat(enchantedCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not gain control if Charisma leaves before its trigger resolves")
    void auraLeavesBeforeTriggerResolves() {
        Permanent enchantedCreature = addCreatureReady(player1, new CrossbowInfantry());
        Permanent charisma = attachCharisma(enchantedCreature);
        Permanent damagedCreature = addCreatureReady(player2, new AlabasterWall());
        damagedCreature.setBlocking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.activateAbility(player1, 0, null, damagedCreature.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, charisma.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(damagedCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(damagedCreature);
    }

    @Test
    @DisplayName("The Aura controller gains control even when an opponent controls the enchanted creature")
    void auraControllerGainsControl() {
        Permanent enchantedCreature = addCreatureReady(player2, new CrossbowInfantry());
        attachCharisma(enchantedCreature);
        Permanent damagedCreature = addCreatureReady(player2, new AlabasterWall());
        damagedCreature.setBlocking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.activateAbility(player2, 0, null, damagedCreature.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(damagedCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchantedCreature)
                .doesNotContain(damagedCreature);
    }

    @Test
    @DisplayName("A creature killed by the damage is not returned by Charisma")
    void lethalDamageDoesNotReturnCreature() {
        Permanent enchantedCreature = addCreatureReady(player1, new CrossbowInfantry());
        attachCharisma(enchantedCreature);
        Permanent damagedCreature = addCreatureReady(player2, new KrisMage());
        damagedCreature.setBlocking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.activateAbility(player1, 0, null, damagedCreature.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(damagedCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(damagedCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(damagedCreature.getCard());
    }

    private Permanent setUpCombat() {
        Permanent enchantedCreature = addCreatureReady(player1, new HornedTroll());
        enchantedCreature.setAttacking(true);

        attachCharisma(enchantedCreature);
        return enchantedCreature;
    }

    private Permanent attachCharisma(Permanent enchantedCreature) {
        Permanent charisma = harness.addToBattlefieldAndReturn(player1, new Charisma());
        charisma.setAttachedTo(enchantedCreature.getId());
        return charisma;
    }
}
