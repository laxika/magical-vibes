package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MaskwoodNexusTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control gain every creature type")
    void grantsEveryCreatureTypeToOwnCreatures() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1,
                creature("Bear", CardSubtype.BEAR));
        harness.addToBattlefield(player1, new MaskwoodNexus());

        assertThat(gqs.computeStaticBonus(gd, bear).grantedSubtypes())
                .contains(CardSubtype.GOBLIN, CardSubtype.DRAGON)
                .doesNotContain(CardSubtype.AURA);
    }

    @Test
    @DisplayName("The all-types effect applies to owned creature cards outside the battlefield")
    void grantsEveryCreatureTypeToOwnedCreatureCards() {
        Card bear = creature("Bear card", CardSubtype.BEAR);
        gd.playerHands.get(player1.getId()).add(bear);
        gd.playerGraveyards.get(player1.getId()).add(creature("Graveyard Bear", CardSubtype.BEAR));
        harness.addToBattlefield(player1, new MaskwoodNexus());

        assertThat(gqs.getCardSubtypes(bear, gd, player1.getId()))
                .contains(CardSubtype.BEAR, CardSubtype.GOBLIN, CardSubtype.DRAGON)
                .doesNotContain(CardSubtype.AURA);
    }

    @Test
    @DisplayName("The token ability creates a 2/2 blue Shapeshifter with changeling")
    void createsChangelingToken() {
        harness.addToBattlefield(player1, new MaskwoodNexus());
        Permanent nexus = findPermanent(player1, "Maskwood Nexus");
        nexus.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Shapeshifter");
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SHAPESHIFTER);
        assertThat(token.getCard().getKeywords()).contains(Keyword.CHANGELING);
    }

    private static Card creature(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.GREEN);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(subtype));
        return card;
    }
}
