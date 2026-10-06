package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.t.ToweringBaloth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilvosRogueElemental.class, ElvishWarrior.class, ToweringBaloth.class})
class SilvosRogueElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Silvos's regeneration ability grants a regeneration shield")
    void resolvingRegenerationAbilityGrantsShield() {
        Permanent silvos = addCreatureReady(player1, new SilvosRogueElemental());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(silvos.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Silvos's regeneration ability costs one green mana and does not tap it")
    void regenerationAbilityCostsGreenManaWithoutTapping() {
        Permanent silvos = addCreatureReady(player1, new SilvosRogueElemental());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(silvos.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Silvos cannot activate regeneration without green mana")
    void cannotActivateRegenerationWithoutMana() {
        addCreatureReady(player1, new SilvosRogueElemental());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("A regeneration shield saves Silvos from lethal combat damage")
    void regenerationShieldSavesFromLethalCombatDamage() {
        Permanent silvos = addCreatureReady(player1, new SilvosRogueElemental());
        addCreatureReady(player2, new ToweringBaloth());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(silvos);
        assertThat(silvos.isTapped()).isTrue();
        assertThat(silvos.getRegenerationShield()).isZero();
        assertThat(silvos.getMarkedDamage()).isZero();
        assertThat(silvos.isBlocking()).isFalse();
        assertThat(silvos.getBlockingTargets()).isEmpty();
        harness.assertInGraveyard(player2, "Towering Baloth");
    }

    @Test
    @DisplayName("Silvos's trample deals excess combat damage to the defending player")
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new SilvosRogueElemental());
        Permanent blocker = addCreatureReady(player2, new ElvishWarrior());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 3,
                player2.getId(), 5
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        harness.assertInGraveyard(player2, "Elvish Warrior");
    }

    @Test
    @DisplayName("Tapped summoning-sick Silvos can activate regeneration repeatedly")
    void tappedSummoningSickSilvosCanActivateRepeatedly() {
        Permanent silvos = harness.addToBattlefieldAndReturn(player1, new SilvosRogueElemental());
        silvos.setSummoningSick(true);
        silvos.setTapped(true);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(silvos.getRegenerationShield()).isEqualTo(2);
        assertThat(silvos.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Creating a regeneration shield does not remove damage or tap Silvos")
    void grantingShieldDoesNotImmediatelyRegenerate() {
        Permanent silvos = addCreatureReady(player1, new SilvosRogueElemental());
        silvos.setMarkedDamage(2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(silvos.getRegenerationShield()).isEqualTo(1);
        assertThat(silvos.getMarkedDamage()).isEqualTo(2);
        assertThat(silvos.isTapped()).isFalse();
    }
}
