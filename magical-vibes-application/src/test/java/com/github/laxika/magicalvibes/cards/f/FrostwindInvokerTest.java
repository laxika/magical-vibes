package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.n.NestInvader;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrostwindInvoker.class, NestInvader.class})
class FrostwindInvokerTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control gain flying until end of turn")
    void ownCreaturesGainFlyingUntilEndOfTurn() {
        Permanent invoker = addCreatureReady(player1, new FrostwindInvoker());
        Permanent ownCreature = addCreatureReady(player1, new NestInvader());
        Permanent opponentCreature = addCreatureReady(player2, new NestInvader());

        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(invoker), null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The flying granted by Frostwind Invoker wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent invoker = addCreatureReady(player1, new FrostwindInvoker());
        Permanent ownCreature = addCreatureReady(player1, new NestInvader());

        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(invoker), null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isTrue();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Only creatures controlled at resolution gain flying")
    void affectedCreaturesAreDeterminedAtResolution() {
        Permanent invoker = addCreatureReady(player1, new FrostwindInvoker());
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(invoker), null, null);

        Permanent beforeResolution = harness.enterBattlefieldAndReturn(player1, new NestInvader());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.FLYING)).isTrue();

        Permanent afterResolution = harness.enterBattlefieldAndReturn(player1, new NestInvader());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A tapped and summoning-sick Invoker can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent invoker = harness.addToBattlefieldAndReturn(player1, new FrostwindInvoker());
        invoker.setSummoningSick(true);
        invoker.setTapped(true);
        Permanent creature = addCreatureReady(player1, new NestInvader());

        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(invoker), null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(invoker.isTapped()).isTrue();
    }
}
