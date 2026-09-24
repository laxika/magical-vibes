package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BlastingStation;
import com.github.laxika.magicalvibes.cards.s.SkyhunterProwler;
import com.github.laxika.magicalvibes.cards.v.VulshokSorcerer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Retaliate.class, BlastingStation.class, SkyhunterProwler.class, VulshokSorcerer.class})
class RetaliateTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys each creature that dealt damage to you this turn")
    void destroysCreaturesThatDealtDamageToYou() {
        Permanent ownCreature = addCreatureReady(player1, new SkyhunterProwler());
        Permanent damagedCreature = addCreatureReady(player2, new VulshokSorcerer());
        Permanent undamagedCreature = addCreatureReady(player2, new SkyhunterProwler());

        harness.activateAbility(player2, indexOf(player2, damagedCreature), null, player1.getId());
        harness.passBothPriorities();

        harness.castFromHand(player1, new Retaliate(), "{2}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(undamagedCreature)
                .doesNotContain(damagedCreature);
        harness.assertInGraveyard(player2, "Vulshok Sorcerer");
    }

    @Test
    @DisplayName("Does nothing when no creature dealt damage to you this turn")
    void doesNothingWhenNoCreatureDealtDamage() {
        Permanent ownCreature = addCreatureReady(player1, new SkyhunterProwler());
        Permanent opponentCreature = addCreatureReady(player2, new SkyhunterProwler());

        harness.castFromHand(player1, new Retaliate(), "{2}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
    }

    @Test
    @DisplayName("Destroys a creature that dealt combat damage to you this turn")
    void destroysCreatureThatDealtCombatDamage() {
        Permanent attacker = addCreatureReady(player2, new SkyhunterProwler());

        declareAttackers(player2, List.of(indexOf(player2, attacker)));
        resolveCombat(player2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);

        harness.castFromHand(player1, new Retaliate(), "{2}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(attacker);
        harness.assertInGraveyard(player2, "Skyhunter Prowler");
    }

    @Test
    @DisplayName("Destroys qualifying creatures regardless of controller")
    void destroysQualifyingCreaturesRegardlessOfController() {
        Permanent ownDamagingCreature = addCreatureReady(player1, new VulshokSorcerer());
        Permanent opponentDamagingCreature = addCreatureReady(player2, new VulshokSorcerer());
        Permanent undamagedCreature = addCreatureReady(player2, new SkyhunterProwler());

        harness.activateAbility(player1, indexOf(player1, ownDamagingCreature), null, player1.getId());
        harness.passBothPriorities();
        harness.activateAbility(player2, indexOf(player2, opponentDamagingCreature), null, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);

        harness.castFromHand(player1, new Retaliate(), "{2}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownDamagingCreature);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(undamagedCreature)
                .doesNotContain(opponentDamagingCreature);
        harness.assertInGraveyard(player1, "Vulshok Sorcerer");
        harness.assertInGraveyard(player2, "Vulshok Sorcerer");
    }

    @Test
    @DisplayName("Does not destroy a noncreature permanent that dealt damage to you")
    void doesNotDestroyNoncreatureDamageSource() {
        Permanent station = harness.addToBattlefieldAndReturn(player2, new BlastingStation());
        addCreatureReady(player2, new SkyhunterProwler());

        harness.activateAbility(player2, indexOf(player2, station), null, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);

        harness.castFromHand(player1, new Retaliate(), "{2}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(station);
        harness.assertInGraveyard(player2, "Skyhunter Prowler");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName())
                .doesNotContain("Blasting Station");
    }

    @Test
    @DisplayName("Ignores damage dealt during a previous turn")
    void ignoresDamageDealtDuringPreviousTurn() {
        Permanent damagedCreature = addCreatureReady(player2, new VulshokSorcerer());

        harness.activateAbility(player2, indexOf(player2, damagedCreature), null, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.castFromHand(player1, new Retaliate(), "{2}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(damagedCreature);
    }

    private int indexOf(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
