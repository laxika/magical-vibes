package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.Archangel;
import com.github.laxika.magicalvibes.cards.c.Cloudshift;
import com.github.laxika.magicalvibes.cards.v.Vorstclaw;
import com.github.laxika.magicalvibes.cards.x.Xenograft;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DefyDeath.class, Vorstclaw.class, Archangel.class, Cloudshift.class, Xenograft.class})
class DefyDeathTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a non-Angel creature with no counters")
    void returnsNonAngelWithoutCounters() {
        Card creature = new Vorstclaw();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DefyDeath()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Vorstclaw");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotInGraveyard(player1, "Vorstclaw");
    }

    @Test
    @DisplayName("Returns an Angel with two +1/+1 counters")
    void returnsAngelWithTwoCounters() {
        Card angel = new Archangel();
        harness.setGraveyard(player1, List.of(angel));
        harness.setHand(player1, List.of(new DefyDeath()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castSorcery(player1, 0, angel.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Archangel");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a non-creature card in the graveyard")
    void cannotTargetNonCreatureCard() {
        Card instant = new Cloudshift();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new DefyDeath()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card creature = new Vorstclaw();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new DefyDeath()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }

    @Test
    @DisplayName("A creature that becomes an Angel on the battlefield receives two counters")
    void creatureMadeAngelByXenograftReceivesCounters() {
        harness.setHand(player1, List.of(new Xenograft()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ANGEL");

        Card creature = new Vorstclaw();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DefyDeath()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Vorstclaw");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Vorstclaw");
    }

    @Test
    @DisplayName("Does not return a creature that leaves the graveyard before resolution")
    void doesNotReturnMissingTarget() {
        Card angel = new Archangel();
        harness.setGraveyard(player1, List.of(angel));
        harness.setHand(player1, List.of(new DefyDeath()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castSorcery(player1, 0, angel.getId());

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(angel));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Archangel");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(angel);
        harness.assertInGraveyard(player1, "Defy Death");
    }
}
