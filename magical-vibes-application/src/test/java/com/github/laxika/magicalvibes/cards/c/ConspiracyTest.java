package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConspiracyTest extends BaseCardTest {

    @Test
    void choosesCreatureTypeAndGrantsItToControlledCreatures() {
        Card bear = creature("Bear Cub", CardSubtype.BEAR);
        harness.addToBattlefield(player1, bear);

        harness.setHand(player1, List.of(new Conspiracy()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");

        Permanent bearPermanent = findPermanent(player1, "Bear Cub");
        assertThat(gqs.computeStaticBonus(gd, bearPermanent).grantedSubtypes())
                .contains(CardSubtype.GOBLIN);
    }

    @Test
    void grantsChosenTypeToOwnedCreatureCardsOutsideTheBattlefield() {
        Card handCreature = creature("Hand Bear", CardSubtype.BEAR);
        Card graveyardCreature = creature("Graveyard Bear", CardSubtype.BEAR);
        Card opponentCreature = creature("Opponent Bear", CardSubtype.BEAR);
        gd.playerHands.get(player1.getId()).add(handCreature);
        gd.playerGraveyards.get(player1.getId()).add(graveyardCreature);
        gd.playerHands.get(player2.getId()).add(opponentCreature);

        Conspiracy conspiracy = new Conspiracy();
        Permanent conspiracyPermanent = new Permanent(conspiracy);
        conspiracyPermanent.setChosenSubtype(CardSubtype.GOBLIN);
        gd.playerBattlefields.get(player1.getId()).add(conspiracyPermanent);

        assertThat(gqs.cardHasSubtype(handCreature, CardSubtype.GOBLIN, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasSubtype(graveyardCreature, CardSubtype.GOBLIN, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasSubtype(opponentCreature, CardSubtype.GOBLIN, gd, player2.getId())).isFalse();
    }

    private static Card creature(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.GREEN);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(subtype));
        return card;
    }
}
