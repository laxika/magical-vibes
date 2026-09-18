package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BasimIbnIshaq.class, Forest.class, GrizzlyBears.class, MindStone.class})
class BasimIbnIshaqTest extends BaseCardTest {

    @Test
    @DisplayName("Draws and becomes unblockable for the first historic spell each turn")
    void historicSpellTriggersOnlyOnceEachTurn() {
        Permanent basim = harness.addToBattlefieldAndReturn(player1, new BasimIbnIshaq());
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());
        harness.setHand(player1, List.of(new MindStone(), new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(basim.isCantBeBlocked()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A nonhistoric spell does not trigger Basim")
    void nonHistoricSpellDoesNotTrigger() {
        Permanent basim = harness.addToBattlefieldAndReturn(player1, new BasimIbnIshaq());
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(basim.isCantBeBlocked()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Puts a +1/+1 counter on itself after dealing combat damage")
    void combatDamageAddsCounter() {
        Permanent basim = addCreatureReady(player1, new BasimIbnIshaq());
        basim.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(basim.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
