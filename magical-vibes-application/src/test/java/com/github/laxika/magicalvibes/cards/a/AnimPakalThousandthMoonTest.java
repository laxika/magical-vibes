package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GoblinTombRaider;
import com.github.laxika.magicalvibes.cards.m.MarketGnome;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AnimPakalThousandthMoon.class, GoblinTombRaider.class, MarketGnome.class})
class AnimPakalThousandthMoonTest extends BaseCardTest {

    @Test
    void attacksWithNonGnomeCreaturePutsCounterAndCreatesGnome() {
        Permanent anim = addCreatureReady(player1, new AnimPakalThousandthMoon());
        addCreatureReady(player1, creature("Soldier", CardSubtype.SOLDIER));

        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1));
            resolveAllTriggers();

            assertThat(anim.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
            assertThat(gnomeTokens()).hasSize(1);
            Permanent token = gnomeTokens().getFirst();
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttacking()).isTrue();
            assertThat(token.isAttackedThisTurn()).isFalse();
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        });
    }

    @Test
    void createsTokensEqualToCountersAfterAddingTheCounter() {
        Permanent anim = addCreatureReady(player1, new AnimPakalThousandthMoon());
        anim.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        addCreatureReady(player1, creature("Soldier", CardSubtype.SOLDIER));

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(anim.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gnomeTokens()).hasSize(3);
    }

    @Test
    void triggersOnceForMultipleNonGnomeAttackers() {
        addCreatureReady(player1, new AnimPakalThousandthMoon());
        addCreatureReady(player1, creature("Soldier", CardSubtype.SOLDIER));
        addCreatureReady(player1, creature("Knight", CardSubtype.KNIGHT));

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(gnomeTokens()).hasSize(1);
    }

    @Test
    void doesNotTriggerForOnlyGnomeAttackers() {
        Permanent anim = addCreatureReady(player1, new AnimPakalThousandthMoon());
        addCreatureReady(player1, creature("Gnome", CardSubtype.GNOME));

        declareAttackers(List.of(1));

        assertThat(anim.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mixedGnomeAndNonGnomeAttackersTriggerOnlyOnce() {
        Permanent anim = addCreatureReady(player1, new AnimPakalThousandthMoon());
        addCreatureReady(player1, new MarketGnome());
        addCreatureReady(player1, new GoblinTombRaider());

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(anim.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gnomeTokens()).hasSize(1);
    }

    @Test
    void triggerStillResolvesAfterOnlyNonGnomeAttackerDies() {
        Permanent anim = addCreatureReady(player1, new AnimPakalThousandthMoon());
        Permanent attacker = addCreatureReady(player1, new GoblinTombRaider());

        declareAttackers(List.of(1));
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, attacker);
        resolveAllTriggers();

        assertThat(anim.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gnomeTokens()).hasSize(1);
    }

    @Test
    void createsTokensUsingLastKnownCountersWhenAnimDiesBeforeResolution() {
        Permanent anim = addCreatureReady(player1, new AnimPakalThousandthMoon());
        anim.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        addCreatureReady(player1, new GoblinTombRaider());

        declareAttackers(List.of(1));
        anim.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, anim);
        resolveAllTriggers();

        assertThat(gnomeTokens()).hasSize(3);
    }

    @Test
    void animCanBeTheOnlyDeclaredAttacker() {
        Permanent anim = addCreatureReady(player1, new AnimPakalThousandthMoon());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(anim.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gnomeTokens()).hasSize(1);
    }

    @Test
    void doesNotTriggerForOpponentsNonGnomeAttackers() {
        Permanent anim = addCreatureReady(player1, new AnimPakalThousandthMoon());
        addCreatureReady(player2, new GoblinTombRaider());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(anim.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gnomeTokens()).isEmpty();
    }

    private List<Permanent> gnomeTokens() {
        return findPermanents(player1, "Gnome").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }

    private Card creature(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(subtype));
        card.setPower(2);
        card.setToughness(2);
        return card;
    }
}
