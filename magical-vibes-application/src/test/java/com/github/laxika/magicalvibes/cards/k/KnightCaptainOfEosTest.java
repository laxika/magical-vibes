package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.VeteranSwordsmith;
import com.github.laxika.magicalvibes.cards.v.VeteransArmaments;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({KnightCaptainOfEos.class, GrizzlyBears.class, Shock.class, VeteranSwordsmith.class,
        VeteransArmaments.class})
class KnightCaptainOfEosTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates two 1/1 white Soldier tokens")
    void etbCreatesTwoSoldierTokens() {
        castAndResolve();

        assertThat(countSoldierTokens(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activating the ability sacrifices a Soldier")
    void activatingSacrificesASoldier() {
        castAndResolve();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, getAnySoldierTokenId(player1));

        assertThat(countSoldierTokens(player1)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Resolving the ability prevents all combat damage this turn")
    void abilityPreventsCombatDamage() {
        castAndResolve();
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, getAnySoldierTokenId(player1));
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();

        addUnblockedAttacker(player1); // Grizzly Bears 2/2
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot activate the ability without a Soldier to sacrifice")
    void cannotActivateWithoutSoldier() {
        castAndResolve();
        // Remove both Soldier tokens, leaving only the Knight-Captain.
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getSubtypes().contains(CardSubtype.SOLDIER)
                        && !p.getCard().getName().equals("Knight-Captain of Eos"));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can sacrifice a nontoken Soldier as the activation cost")
    void canSacrificeNontokenSoldier() {
        castAndResolve();
        Permanent swordsmith = addCreatureReady(player1, new VeteranSwordsmith());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, swordsmith.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(swordsmith);
        assertThat(gd.preventAllCombatDamage).isTrue();
    }

    @Test
    @DisplayName("Ability prevents only combat damage - noncombat damage still lands")
    void doesNotPreventNoncombatDamage() {
        castAndResolve();
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, getAnySoldierTokenId(player1));
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Can sacrifice a noncreature Soldier permanent")
    void canSacrificeNoncreatureSoldier() {
        harness.addToBattlefield(player1, new KnightCaptainOfEos());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new VeteransArmaments());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(equipment);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof VeteransArmaments);
        addUnblockedAttacker(player2);
        harness.forceActivePlayer(player2);
        harness.resolveCombatDamage();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Knight-Captain can sacrifice a tapped Soldier")
    void canActivateWhileTappedAndSummoningSick() {
        castAndResolve();
        Permanent captain = findPermanent(player1, "Knight-Captain of Eos");
        captain.tap();
        captain.setSummoningSick(true);
        Permanent soldier = findPermanent(player1, "Soldier");
        soldier.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, soldier.getId());
        harness.passBothPriorities();

        assertThat(countSoldierTokens(player1)).isEqualTo(1);
        addUnblockedAttacker(player2);
        harness.forceActivePlayer(player2);
        harness.resolveCombatDamage();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An opponent's Soldier cannot pay the sacrifice cost")
    void cannotActivateWithOnlyOpponentsSoldier() {
        harness.addToBattlefield(player1, new KnightCaptainOfEos());
        harness.addToBattlefield(player2, new VeteranSwordsmith());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player2, "Veteran Swordsmith")).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate without white mana")
    void cannotActivateWithoutWhiteMana() {
        harness.addToBattlefield(player1, new KnightCaptainOfEos());
        harness.addToBattlefield(player1, new VeteranSwordsmith());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Veteran Swordsmith")).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage prevention expires at the end of the turn")
    void preventionExpiresAtEndOfTurn() {
        castAndResolve();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, getAnySoldierTokenId(player1));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        addUnblockedAttacker(player2);
        harness.resolveCombatDamage();

        harness.assertLife(player1, 18);
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new KnightCaptainOfEos()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger
    }

    private int countSoldierTokens(Player player) {
        return (int) findPermanents(player, "Soldier").stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.SOLDIER))
                .count();
    }

    private UUID getAnySoldierTokenId(Player player) {
        return findPermanent(player, "Soldier").getId();
    }

    private Permanent addUnblockedAttacker(Player player) {
        Permanent perm = addCreatureReady(player, new GrizzlyBears());
        perm.setAttacking(true);
        return perm;
    }
}
