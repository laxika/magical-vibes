package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.e.EzioAuditoreDaFirenze;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.t.TheAesirEscapeValhalla;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BasimIbnIshaq.class, Forest.class, GrizzlyBears.class, MindStone.class,
        EzioAuditoreDaFirenze.class, TheAesirEscapeValhalla.class})
class BasimIbnIshaqTest extends BaseCardTest {

    @Test
    @DisplayName("Draws and becomes unblockable for the first historic spell each turn")
    void historicSpellTriggersOnlyOnceEachTurn() {
        Permanent basim = harness.addToBattlefieldAndReturn(player1, new BasimIbnIshaq());
        harness.setLibrary(player1, List.of(new Forest()));
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
        harness.setLibrary(player1, List.of(new Forest()));
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

    @Test
    @DisplayName("A legendary creature spell triggers Basim before the spell resolves")
    void legendarySpellTriggersBeforeResolving() {
        Permanent basim = harness.addToBattlefieldAndReturn(player1, new BasimIbnIshaq());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new EzioAuditoreDaFirenze()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        assertThat(basim.isCantBeBlocked()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(basim.isCantBeBlocked()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Ezio Auditore da Firenze");
        resolveAllTriggers();
    }

    @Test
    @DisplayName("A nonlegendary Saga spell is historic")
    void sagaSpellTriggers() {
        Permanent basim = harness.addToBattlefieldAndReturn(player1, new BasimIbnIshaq());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new TheAesirEscapeValhalla()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(basim.isCantBeBlocked()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("A nonhistoric spell does not consume the once-per-turn trigger")
    void nonHistoricSpellDoesNotConsumeTrigger() {
        Permanent basim = harness.addToBattlefieldAndReturn(player1, new BasimIbnIshaq());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new GrizzlyBears(), new MindStone()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(basim.isCantBeBlocked()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's historic spell does not trigger Basim")
    void opponentsHistoricSpellDoesNotTrigger() {
        Permanent basim = harness.addToBattlefieldAndReturn(player1, new BasimIbnIshaq());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new MindStone()));
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castArtifact(player2, 0);
        resolveAllTriggers();

        assertThat(basim.isCantBeBlocked()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Unblockability expires and the historic trigger resets on the next turn")
    void triggerResetsAndUnblockabilityExpires() {
        harness.setHand(player2, List.of());
        Permanent basim = harness.addToBattlefieldAndReturn(player1, new BasimIbnIshaq());
        Permanent opposingBasim = harness.addToBattlefieldAndReturn(player2, new BasimIbnIshaq());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();
        assertThat(basim.isCantBeBlocked()).isTrue();
        assertThat(opposingBasim.isCantBeBlocked()).isFalse();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(basim.isCantBeBlocked()).isFalse();
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(basim.isCantBeBlocked()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(opposingBasim.isCantBeBlocked()).isFalse();
    }
}
