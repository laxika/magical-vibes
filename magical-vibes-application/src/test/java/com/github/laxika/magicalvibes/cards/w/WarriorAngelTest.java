package com.github.laxika.magicalvibes.cards.w;

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

@CardUsed(WarriorAngel.class)
class WarriorAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life equal to damage dealt to an opposing creature")
    void gainsLifeEqualToDamageDealtToCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent angel = addCreatureReady(player1, new WarriorAngel());
        Permanent blocker = addCreatureReady(player2, new WarriorAngel());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(angel)));
        prepareDeclareBlockers();
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
    @CardUsed(Delirium.class)
    @DisplayName("Noncombat damage also gains that much life")
    void gainsLifeFromNoncombatDamage() {
        Permanent angel = addCreatureReady(player2, new WarriorAngel());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new Delirium()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, angel.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
