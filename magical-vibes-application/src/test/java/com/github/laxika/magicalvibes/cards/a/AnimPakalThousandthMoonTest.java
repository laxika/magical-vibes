package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AnimPakalThousandthMoon.class)
class AnimPakalThousandthMoonTest extends BaseCardTest {

    @Test
    void attacksWithNonGnomeCreaturePutsCounterAndCreatesGnome() {
        Permanent anim = addAnimReady(player1);
        addCreatureReady(player1, creature("Soldier", CardSubtype.SOLDIER));

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(anim.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gnomeTokens()).hasSize(1);
        Permanent token = gnomeTokens().getFirst();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttackedThisTurn()).isTrue();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
    }

    @Test
    void createsTokensEqualToCountersAfterAddingTheCounter() {
        Permanent anim = addAnimReady(player1);
        anim.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        addCreatureReady(player1, creature("Soldier", CardSubtype.SOLDIER));

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(anim.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gnomeTokens()).hasSize(3);
    }

    @Test
    void triggersOnceForMultipleNonGnomeAttackers() {
        addAnimReady(player1);
        addCreatureReady(player1, creature("Soldier", CardSubtype.SOLDIER));
        addCreatureReady(player1, creature("Knight", CardSubtype.KNIGHT));

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(gnomeTokens()).hasSize(1);
    }

    @Test
    void doesNotTriggerForOnlyGnomeAttackers() {
        Permanent anim = addAnimReady(player1);
        addCreatureReady(player1, creature("Gnome", CardSubtype.GNOME));

        declareAttackers(List.of(1));

        assertThat(anim.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addAnimReady(Player player) {
        Permanent anim = new Permanent(new AnimPakalThousandthMoon());
        anim.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(anim);
        return anim;
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
