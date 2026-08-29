package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.InvasionOfInnistrad;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AttentiveSkywarden.class, InvasionOfInnistrad.class})
class AttentiveSkywardenTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player transforms a targeted Incubator token you control")
    void combatDamageToPlayerTransformsIncubator() {
        Permanent incubator = addIncubator(player1);
        Permanent otherToken = addToken(player1, "Treasure");
        Permanent attacker = addCreatureReady(player1, new AttentiveSkywarden());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(incubator.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(otherToken.getId());

        harness.handlePermanentChosen(player1, incubator.getId());
        harness.passBothPriorities();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(incubator.getCard().getName()).isEqualTo("Phyrexian");
    }

    @Test
    @DisplayName("Combat damage to a battle also triggers Attentive Skywarden")
    void combatDamageToBattleTransformsIncubator() {
        Permanent incubator = addIncubator(player1);
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfInnistrad());
        battle.setProtectorPlayerId(player2.getId());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        Permanent attacker = addCreatureReady(player1, new AttentiveSkywarden());
        attacker.setAttacking(true);
        attacker.setAttackTarget(battle.getId());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).containsExactly(incubator.getId());
        harness.handlePermanentChosen(player1, incubator.getId());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(3);
        assertThat(incubator.isTransformed()).isTrue();
    }

    private Permanent addIncubator(com.github.laxika.magicalvibes.model.Player player) {
        Card incubator = new Card();
        incubator.setName("Incubator");
        incubator.setType(CardType.ARTIFACT);
        incubator.setManaCost("");
        incubator.setToken(true);

        Card phyrexian = new Card();
        phyrexian.setName("Phyrexian");
        phyrexian.setType(CardType.CREATURE);
        phyrexian.setManaCost("");
        phyrexian.setPower(2);
        phyrexian.setToughness(2);
        incubator.setBackFaceCard(phyrexian);
        return harness.addToBattlefieldAndReturn(player, incubator);
    }

    private Permanent addToken(com.github.laxika.magicalvibes.model.Player player, String name) {
        Card token = new Card();
        token.setName(name);
        token.setType(CardType.ARTIFACT);
        token.setManaCost("");
        token.setToken(true);
        return harness.addToBattlefieldAndReturn(player, token);
    }
}
