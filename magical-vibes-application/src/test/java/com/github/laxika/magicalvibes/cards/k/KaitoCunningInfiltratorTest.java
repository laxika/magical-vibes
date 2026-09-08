package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KaitoCunningInfiltrator.class, GrizzlyBears.class, Shock.class})
class KaitoCunningInfiltratorTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage from a creature you control adds a loyalty counter")
    void combatDamageAddsLoyalty() {
        Permanent kaito = addReadyKaito(3);
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(kaito.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("+1 can be activated without choosing a creature")
    void plusOneCanUseNoTarget() {
        Permanent kaito = addReadyKaito(3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(kaito.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("+1 makes a target creature unable to be blocked this turn")
    void plusOneMakesCreatureUnblockable() {
        addReadyKaito(3);
        Permanent creature = addReadyCreature(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, creature.getId(), null);
        harness.passBothPriorities();

        assertThat(creature.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("+1 draws a card and then discards a card")
    void plusOneLoots() {
        addReadyKaito(3);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Shock()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Shock");
    }

    @Test
    @DisplayName("-2 creates a 2/1 blue Ninja token")
    void minusTwoCreatesNinja() {
        addReadyKaito(3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent ninja = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(ninja.getCard().getName()).isEqualTo("Ninja");
        assertThat(ninja.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(ninja.getCard().getSubtypes()).contains(CardSubtype.NINJA);
        assertThat(gqs.getEffectivePower(gd, ninja)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ninja)).isEqualTo(1);
    }

    @Test
    @DisplayName("-9 emblem creates a Ninja when an opponent casts a spell")
    void minusNineEmblemTriggersForOpponentSpell() {
        addReadyKaito(9);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(gd.emblems).hasSize(1);
        Emblem emblem = gd.emblems.getFirst();
        assertThat(emblem.controllerId()).isEqualTo(player1.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castInstant(player2, 0, player1.getId());

        assertThat(countPermanents(player1, "Ninja")).isZero();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Ninja")).isEqualTo(1);
    }

    private Permanent addReadyKaito(int loyalty) {
        Permanent kaito = new Permanent(new KaitoCunningInfiltrator());
        kaito.setCounterCount(CounterType.LOYALTY, loyalty);
        kaito.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(kaito);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return kaito;
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent creature = new Permanent(card);
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }
}
