package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.c.Combust;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PlatedPegasus.class, Shock.class, ProdigalSorcerer.class,
        ChandraNalaar.class, Combust.class})
class PlatedPegasusTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents 1 damage from a spell to a player")
    void preventsSpellDamageToPlayer() {
        harness.addToBattlefield(player1, new PlatedPegasus());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Prevents 1 damage from a spell to a creature")
    void preventsSpellDamageToCreature() {
        harness.addToBattlefield(player1, new PlatedPegasus());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, createWhiteCreature("Shielded Creature", 2, 4));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents 1 damage from a spell to a planeswalker")
    void preventsSpellDamageToPlaneswalker() {
        harness.addToBattlefield(player1, new PlatedPegasus());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not prevent damage from an activated ability")
    void doesNotPreventAbilityDamage() {
        harness.addToBattlefield(player1, new PlatedPegasus());
        Permanent sorcerer = harness.addToBattlefieldAndReturn(player2, new ProdigalSorcerer());
        sorcerer.setSummoningSick(false);
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(sorcerer),
                null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Does not prevent damage that cannot be prevented")
    void doesNotPreventUnpreventableSpellDamage() {
        harness.addToBattlefield(player1, new PlatedPegasus());
        Permanent creature = harness.addToBattlefieldAndReturn(player2,
                createWhiteCreature("Unpreventable Target", 2, 6));
        harness.setHand(player1, List.of(new Combust()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(5);
    }

    private static Card createWhiteCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.WHITE);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
