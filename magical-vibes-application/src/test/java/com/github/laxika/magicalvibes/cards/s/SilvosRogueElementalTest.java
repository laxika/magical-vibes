package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
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

@CardUsed({SilvosRogueElemental.class, ElvishWarrior.class})
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
        silvos.setRegenerationShield(1);
        silvos.setBlocking(true);
        silvos.addBlockingTarget(0);

        ElvishWarrior attackerCard = new ElvishWarrior();
        attackerCard.setPower(6);
        attackerCard.setToughness(6);
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, attackerCard);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(silvos);
        assertThat(silvos.isTapped()).isTrue();
        assertThat(silvos.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Silvos's trample deals excess combat damage to the defending player")
    void trampleDealsExcessCombatDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new SilvosRogueElemental());
        Permanent blocker = addCreatureReady(player2, new ElvishWarrior());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
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
}
