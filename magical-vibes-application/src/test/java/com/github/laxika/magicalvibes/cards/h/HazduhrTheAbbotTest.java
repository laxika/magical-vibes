package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AnabaShaman;
import com.github.laxika.magicalvibes.cards.a.AysenCrusader;
import com.github.laxika.magicalvibes.cards.d.DeathSpeakers;
import com.github.laxika.magicalvibes.cards.g.GreaterWerewolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HazduhrTheAbbot.class, AnabaShaman.class, AysenCrusader.class, DeathSpeakers.class,
        GreaterWerewolf.class})
class HazduhrTheAbbotTest extends BaseCardTest {

    private Permanent addHazduhrReady() {
        return addCreatureReady(player1, new HazduhrTheAbbot());
    }

    private Permanent addShamanReady() {
        return addCreatureReady(player1, new AnabaShaman());
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private void activateHazduhr(Permanent hazduhr, int xValue, Permanent target) {
        harness.addMana(player1, ManaColor.WHITE, xValue);
        harness.activateAbility(player1, battlefieldIndex(player1, hazduhr), xValue, target.getId());
        harness.passBothPriorities();
    }

    private void ping(Permanent shaman, Permanent target) {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, battlefieldIndex(player1, shaman), null, target.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Activating for X registers an X-limited, any-source redirect shield protecting the target")
    void activationCreatesShield() {
        Permanent hazduhr = addHazduhrReady();
        Permanent target = addCreatureReady(player1, new AysenCrusader());

        activateHazduhr(hazduhr, 2, target);

        assertThat(gd.creatureDamageRedirectShields).hasSize(1);
        var shield = gd.creatureDamageRedirectShields.getFirst();
        assertThat(shield.protectedPermanentId()).isEqualTo(target.getId());
        assertThat(shield.damageSourceId()).isNull();
        assertThat(shield.remainingAmount()).isEqualTo(2);
        assertThat(shield.redirectTargetId()).isEqualTo(hazduhr.getId());
    }

    @Test
    @DisplayName("Noncombat damage to the targeted white creature is dealt to Hazduhr instead")
    void redirectsNoncombatDamage() {
        Permanent hazduhr = addHazduhrReady();
        Permanent target = addCreatureReady(player1, new AysenCrusader());
        Permanent shaman = addShamanReady();

        activateHazduhr(hazduhr, 2, target);
        ping(shaman, target);

        assertThat(target.getMarkedDamage()).isEqualTo(0);
        assertThat(hazduhr.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Only the paid X damage is redirected; further damage stays on the protected creature")
    void redirectsOnlyXDamage() {
        Permanent hazduhr = addHazduhrReady();
        Permanent target = addCreatureReady(player1, new AysenCrusader());
        Permanent firstShaman = addShamanReady();
        Permanent secondShaman = addShamanReady();

        activateHazduhr(hazduhr, 1, target);
        ping(firstShaman, target);

        assertThat(hazduhr.getMarkedDamage()).isEqualTo(1);
        assertThat(target.getMarkedDamage()).isEqualTo(0);

        ping(secondShaman, target);

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(hazduhr.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage to the targeted white creature is dealt to Hazduhr first")
    void redirectsCombatDamage() {
        Permanent hazduhr = addHazduhrReady();
        Permanent target = addCreatureReady(player1, new AysenCrusader());
        Permanent attacker = addCreatureReady(player2, new DeathSpeakers());

        activateHazduhr(hazduhr, 1, target);

        declareAttackers(player2, List.of(battlefieldIndex(player2, attacker)));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                battlefieldIndex(player1, target), battlefieldIndex(player2, attacker))));
        resolveCombat(player2);

        assertThat(target.getMarkedDamage()).isEqualTo(0);
        assertThat(hazduhr.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Damage is not lost when Hazduhr leaves before the next damage")
    void doesNotLoseDamageWhenHazduhrLeavesBeforeDamage() {
        Permanent hazduhr = addHazduhrReady();
        Permanent target = addCreatureReady(player1, new AysenCrusader());
        Permanent shaman = addShamanReady();

        activateHazduhr(hazduhr, 1, target);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, hazduhr));
        harness.passBothPriorities();

        ping(shaman, target);

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a nonwhite creature you control")
    void cannotTargetNonwhiteCreature() {
        Permanent hazduhr = addHazduhrReady();
        Permanent nonwhiteCreature = addCreatureReady(player1, new GreaterWerewolf());

        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, hazduhr), 2, nonwhiteCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a white creature an opponent controls")
    void cannotTargetOpponentsWhiteCreature() {
        Permanent hazduhr = addHazduhrReady();
        Permanent target = addCreatureReady(player2, new AysenCrusader());

        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, hazduhr), 2, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
