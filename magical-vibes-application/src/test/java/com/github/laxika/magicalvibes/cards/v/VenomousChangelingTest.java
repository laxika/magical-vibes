package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.e.EnduringSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VenomousChangeling.class, EnduringSliver.class, GrizzlyBears.class})
class VenomousChangelingTest extends BaseCardTest {

    @Test
    @DisplayName("Deathtouch destroys a creature dealt combat damage by Venomous Changeling")
    void deathtouchDestroysBlocker() {
        Permanent changeling = addCreatureReady(player1, new VenomousChangeling());
        changeling.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(changeling);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
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
}
