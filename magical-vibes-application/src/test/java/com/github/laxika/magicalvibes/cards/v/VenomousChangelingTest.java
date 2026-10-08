package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.e.EnduringSliver;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VenomousChangeling.class, EnduringSliver.class})
class VenomousChangelingTest extends BaseCardTest {

    @Test
    @DisplayName("Deathtouch destroys a creature dealt combat damage by Venomous Changeling")
    void deathtouchDestroysBlocker() {
        Permanent changeling = addCreatureReady(player1, new VenomousChangeling());
        changeling.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new EnduringSliver());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(changeling);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Enduring Sliver"));
    }

    @Test
    @DisplayName("Changeling lets Venomous Changeling receive a Sliver-granted ability")
    void changelingReceivesSliverAbility() {
        addCreatureReady(player1, new EnduringSliver());
        Permanent changeling = addCreatureReady(player1, new VenomousChangeling());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(changeling.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Deathtouch also destroys an attacker blocked by Venomous Changeling")
    void deathtouchDestroysAttacker() {
        Permanent attacker = addCreatureReady(player1, new EnduringSliver());
        attacker.setAttacking(true);
        Permanent changeling = addCreatureReady(player2, new VenomousChangeling());
        changeling.setBlocking(true);
        changeling.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(attacker.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(changeling);
    }

    @Test
    @DisplayName("Deathtouch damage to a player causes ordinary life loss")
    void unblockedDamageDoesNotDestroyPlayer() {
        harness.setLife(player2, 20);
        Permanent changeling = addCreatureReady(player1, new VenomousChangeling());
        changeling.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(changeling);
    }
}
