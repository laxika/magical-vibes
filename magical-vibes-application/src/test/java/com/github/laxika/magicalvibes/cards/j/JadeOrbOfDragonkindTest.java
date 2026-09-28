package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(JadeOrbOfDragonkind.class)
class JadeOrbOfDragonkindTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Jade Orb of Dragonkind adds green mana")
    void tappingAddsGreenMana() {
        Permanent orb = addReadyOrb();

        harness.activateAbility(player1, 0, null, null);

        assertThat(manaPool().get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(orb.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mana spent on a Dragon creature spell grants a counter and hexproof")
    void dragonCastWithOrbManaGainsCounterAndHexproof() {
        addReadyOrb();
        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player1, List.of(createCreature("Test Dragon", CardSubtype.DRAGON)));

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent dragon = findPermanent(player1, "Test Dragon");
        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(dragon.hasKeyword(Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Mana spent on a non-Dragon creature spell does not grant the bonus")
    void nonDragonCastWithOrbManaDoesNotGainBonus() {
        addReadyOrb();
        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player1, List.of(createCreature("Test Goblin", CardSubtype.GOBLIN)));

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent creature = findPermanent(player1, "Test Goblin");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(creature.hasKeyword(Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("A Dragon cast with mana from another source does not get the bonus")
    void dragonCastWithOtherManaDoesNotGainBonus() {
        addReadyOrb();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(createCreature("Test Dragon", CardSubtype.DRAGON)));

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent dragon = findPermanent(player1, "Test Dragon");
        assertThat(dragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(dragon.hasKeyword(Keyword.HEXPROOF)).isFalse();
    }

    private Permanent addReadyOrb() {
        Permanent orb = harness.addToBattlefieldAndReturn(player1, new JadeOrbOfDragonkind());
        orb.setSummoningSick(false);
        return orb;
    }

    private ManaPool manaPool() {
        return gd.playerManaPools.get(player1.getId());
    }

    private static Card createCreature(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.RED);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(subtype));
        return card;
    }
}
