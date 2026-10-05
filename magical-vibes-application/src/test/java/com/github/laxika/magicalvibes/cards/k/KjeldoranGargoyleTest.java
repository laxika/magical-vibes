package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.Backlash;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KjeldoranGargoyle.class, Backlash.class})
class KjeldoranGargoyleTest extends BaseCardTest {

    @Test
    void gainsLifeEqualToDamageDealtToOpponent() {
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);

        KjeldoranGargoyle gargoyleCard = new KjeldoranGargoyle();
        gargoyleCard.setPower(3);
        Permanent gargoyle = addCreatureReady(player1, gargoyleCard);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(gargoyle)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    void gainsLifeFromDamageDealtToCreatureEvenWhenItDies() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent attacker = addCreatureReady(player1, new KjeldoranGargoyle());
        Permanent blocker = addCreatureReady(player2, new KjeldoranGargoyle());
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);

        declareAttackersAndPrepareBlockers(List.of(attackerIndex));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Kjeldoran Gargoyle");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(23);
    }

    @Test
    void doesNotGainLifeWhenItDealsNoDamage() {
        harness.setLife(player1, 20);

        KjeldoranGargoyle gargoyleCard = new KjeldoranGargoyle();
        gargoyleCard.setPower(0);
        Permanent attacker = addCreatureReady(player1, gargoyleCard);
        Permanent blocker = addCreatureReady(player2, new KjeldoranGargoyle());
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);

        declareAttackersAndPrepareBlockers(List.of(attackerIndex));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void noncombatDamageAlsoGainsLife() {
        Permanent gargoyle = addCreatureReady(player2, new KjeldoranGargoyle());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new Backlash()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, gargoyle.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void lifeGainUsesDamageDealtRatherThanPowerAtResolution() {
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);
        Permanent gargoyle = addCreatureReady(player1, new KjeldoranGargoyle());
        gargoyle.setPowerModifier(2);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(gargoyle)));
        resolveCombat();

        harness.assertLife(player2, 15);
        harness.assertLife(player1, 15);

        gargoyle.setPowerModifier(0);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
    }
}
