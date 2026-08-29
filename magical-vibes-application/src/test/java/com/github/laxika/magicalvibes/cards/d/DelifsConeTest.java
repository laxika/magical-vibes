package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.ArmorThrull;
import com.github.laxika.magicalvibes.cards.i.IcatianPhalanx;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DelifsCone.class, IcatianPhalanx.class, ArmorThrull.class})
class DelifsConeTest extends BaseCardTest {

    private Permanent activateForAttacker() {
        harness.addToBattlefield(player1, new DelifsCone());
        Permanent attacker = addCreatureReady(player1, new IcatianPhalanx());

        int coneIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Delif's Cone"));
        harness.activateAbility(player1, coneIndex, null, attacker.getId());
        harness.assertInGraveyard(player1, "Delif's Cone");
        harness.passBothPriorities();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        return attacker;
    }

    private void declareUnblockedAttack() {
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Accepting gains life equal to the unblocked creature's power and prevents combat damage")
    void acceptingGainsLifeAndPreventsCombatDamage() {
        harness.setLife(player1, 20);
        Permanent attacker = activateForAttacker();

        declareUnblockedAttack();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());

        resolveCombat();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Declining leaves the chosen creature able to deal combat damage")
    void decliningDealsCombatDamage() {
        harness.setLife(player2, 20);
        activateForAttacker();

        declareUnblockedAttack();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).isEmpty();
        resolveCombat();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A blocked chosen creature does not trigger")
    void blockedChosenCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new DelifsCone());
        Permanent attacker = addCreatureReady(player1, new IcatianPhalanx());
        Permanent blocker = addCreatureReady(player2, new IcatianPhalanx());

        int coneIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Delif's Cone"));
        harness.activateAbility(player1, coneIndex, null, attacker.getId());
        harness.assertInGraveyard(player1, "Delif's Cone");
        harness.passBothPriorities();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
    }

    @Test
    @DisplayName("Only a creature you control can be targeted")
    void onlyCreatureYouControlCanBeTargeted() {
        harness.addToBattlefield(player1, new DelifsCone());
        Permanent opponentCreature = addCreatureReady(player2, new IcatianPhalanx());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Activating after blockers are declared does not create the delayed trigger")
    void activatingAfterBlockersAreDeclaredDoesNotTrigger() {
        harness.addToBattlefield(player1, new DelifsCone());
        Permanent attacker = addCreatureReady(player1, new IcatianPhalanx());
        addCreatureReady(player2, new IcatianPhalanx());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        int coneIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Delif's Cone"));
        harness.activateAbility(player1, coneIndex, null, attacker.getId());
        harness.assertInGraveyard(player1, "Delif's Cone");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        resolveCombat();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The accepted life gain uses the creature's power when the may ability resolves")
    void acceptedLifeGainUsesPowerAtResolution() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new DelifsCone());
        Permanent attacker = addCreatureReady(player1, new IcatianPhalanx());
        Permanent armorThrull = addCreatureReady(player1, new ArmorThrull());

        int coneIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Delif's Cone"));
        harness.activateAbility(player1, coneIndex, null, attacker.getId());
        harness.passBothPriorities();
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        int armorIndex = gd.playerBattlefields.get(player1.getId()).indexOf(armorThrull);
        harness.activateAbility(player1, armorIndex, null, attacker.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
    }

    @Test
    @DisplayName("The delayed life gain uses last-known power after the attacker leaves")
    void delayedLifeGainUsesLastKnownPowerAfterAttackerLeaves() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new DelifsCone());
        Permanent attacker = addCreatureReady(player1, new IcatianPhalanx());
        addCreatureReady(player2, new IcatianPhalanx());

        int coneIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Delif's Cone"));
        harness.activateAbility(player1, coneIndex, null, attacker.getId());
        harness.passBothPriorities();
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(harness.getPermanentRemovalService().removePermanentToGraveyard(gd, attacker)).isTrue();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }
}
