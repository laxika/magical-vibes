package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.InvasionOfInnistrad;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

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

    @Test
    @DisplayName("The controller may choose no target even when an Incubator is available")
    void mayChooseNoTarget() {
        Permanent incubator = addIncubator(player1);
        Permanent attacker = addCreatureReady(player1, new AttentiveSkywarden());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());

        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPlayerIds()).contains(player1.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(incubator.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Opposing Incubators and non-token permanents are not legal targets")
    void onlyOwnIncubatorTokensAreTargets() {
        Permanent own = addIncubator(player1);
        Permanent opposing = addIncubator(player2);
        Permanent nonToken = addIncubator(player1);
        TestCards.mutableCard(nonToken).setToken(false);
        Permanent attacker = addCreatureReady(player1, new AttentiveSkywarden());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).containsExactly(own.getId());
        harness.handlePermanentChosen(player1, own.getId());
        harness.passBothPriorities();

        assertThat(own.isTransformed()).isTrue();
        assertThat(opposing.isTransformed()).isFalse();
        assertThat(nonToken.isTransformed()).isFalse();
        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
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
        phyrexian.setAdditionalTypes(Set.of(CardType.ARTIFACT));
        phyrexian.setSubtypes(List.of(CardSubtype.PHYREXIAN));
        phyrexian.setToken(true);
        phyrexian.setPower(0);
        phyrexian.setToughness(0);
        incubator.setBackFaceCard(phyrexian);
        Permanent permanent = harness.addToBattlefieldAndReturn(player, incubator);
        permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        return permanent;
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
