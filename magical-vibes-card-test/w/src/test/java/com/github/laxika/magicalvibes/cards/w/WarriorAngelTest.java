package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.ConstantMists;
import com.github.laxika.magicalvibes.cards.d.Delirium;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarriorAngel.class, Delirium.class, ConstantMists.class})
class WarriorAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life equal to damage dealt to an opposing creature")
    void gainsLifeEqualToDamageDealtToCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent angel = addCreatureReady(player1, new WarriorAngel());
        Permanent blocker = addCreatureReady(player2, new WarriorAngel());

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(angel)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(angel))));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(23);
    }

    @Test
    void gainsLifeEqualToDamageDealtToOpponent() {
        harness.setLife(player1, 15);
        harness.setLife(player2, 20);

        Permanent angel = addCreatureReady(player1, new WarriorAngel());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(angel)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Noncombat damage also gains that much life")
    void gainsLifeFromNoncombatDamage() {
        Permanent angel = addCreatureReady(player2, new WarriorAngel());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new Delirium()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, angel.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Damage dealt while dying in combat still triggers life gain")
    void gainsLifeEvenWhenItDiesDealingDamage() {
        Permanent angel = addCreatureReady(player1, new WarriorAngel());
        angel.setMarkedDamage(1);
        Permanent blocker = addCreatureReady(player2, new WarriorAngel());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(angel);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        harness.assertInGraveyard(player1, "Warrior Angel");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        resolveAllTriggers();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 23);
    }

    @Test
    @DisplayName("Prevented damage does not trigger life gain")
    void preventedDamageDoesNotGainLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new ConstantMists(), "{1}{G}");
        harness.passBothPriorities();
        addCreatureReady(player1, new WarriorAngel());

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Another Angel's damage does not trigger an idle Angel")
    void onlyDamageFromThisAngelTriggersLifeGain() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new WarriorAngel());
        addCreatureReady(player1, new WarriorAngel());

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }
}
