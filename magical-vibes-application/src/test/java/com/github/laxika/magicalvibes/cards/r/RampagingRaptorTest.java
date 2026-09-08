package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.i.InvasionOfInnistrad;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RampagingRaptor.class, ChandraNalaar.class, InvasionOfInnistrad.class})
class RampagingRaptorTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability gives Rampaging Raptor +2/+0 until end of turn")
    void activatedAbilityBoostsPower() {
        Permanent raptor = addCreatureReady(player1, new RampagingRaptor());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, raptor)).isEqualTo(6);
    }

    @Test
    @DisplayName("Combat damage to an opponent can target that opponent's planeswalker")
    void combatDamageTargetsOpponentsPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        Permanent raptor = addCreatureReady(player1, new RampagingRaptor());
        raptor.setAttacking(true);
        raptor.setAttackTarget(player2.getId());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(planeswalker.getId());
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage to an opponent can target a battle that opponent protects")
    void combatDamageTargetsBattleOpponentProtects() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfInnistrad());
        battle.setProtectorPlayerId(player2.getId());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        Permanent raptor = addCreatureReady(player1, new RampagingRaptor());
        raptor.setAttacking(true);
        raptor.setAttackTarget(player2.getId());

        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(battle.getId());
        harness.handlePermanentChosen(player1, battle.getId());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(1);
    }
}
