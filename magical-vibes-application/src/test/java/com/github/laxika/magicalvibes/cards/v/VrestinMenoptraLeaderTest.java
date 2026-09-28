package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(VrestinMenoptraLeader.class)
class VrestinMenoptraLeaderTest extends BaseCardTest {

    @Test
    void entersWithXCountersAndCreatesXFlyingAlienInsects() {
        harness.setHand(player1, List.of(new VrestinMenoptraLeader()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0, 2);
        harness.passBothPriorities();

        Permanent vrestin = findPermanent(player1, "Vrestin, Menoptra Leader");
        assertThat(vrestin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        List<Permanent> tokens = findPermanents(player1, "Alien Insect");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ALIEN, CardSubtype.INSECT);
            assertThat(token.hasKeyword(Keyword.FLYING)).isTrue();
            assertThat(token.getEffectivePower()).isEqualTo(1);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    void putsCountersOnEachAttackingInsectOnce() {
        addReady(new VrestinMenoptraLeader());
        Permanent attackingInsect = addReady(creature("Attacking Insect", CardSubtype.INSECT));
        Permanent secondAttackingInsect = addReady(creature("Second Attacking Insect", CardSubtype.INSECT));
        Permanent nonAttackingInsect = addReady(creature("Nonattacking Insect", CardSubtype.INSECT));
        Permanent soldier = addReady(creature("Soldier", CardSubtype.SOLDIER));

        declareAttackers(List.of(1, 2, 4));
        resolveAllTriggers();

        assertThat(attackingInsect.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondAttackingInsect.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonAttackingInsect.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(soldier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerWhenOnlyNonInsectsAttack() {
        addReady(new VrestinMenoptraLeader());
        Permanent soldier = addReady(creature("Soldier", CardSubtype.SOLDIER));

        declareAttackers(List.of(1));

        assertThat(soldier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReady(Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    private Card creature(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(subtype));
        card.setPower(1);
        card.setToughness(1);
        return card;
    }
}
