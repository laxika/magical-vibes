package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RukarumelBiologist.class)
class RukarumelBiologistTest extends BaseCardTest {

    @Test
    @DisplayName("Rukarumel grants the chosen type to nontoken creatures and Slivers")
    void grantsChosenTypeToNontokensAndSlivers() {
        Permanent nontokenCreature = addCreatureReady(player1,
                creature("Bear", CardSubtype.BEAR, false));
        Permanent sliverToken = addCreatureReady(player1,
                creature("Sliver Token", CardSubtype.SLIVER, true));
        Permanent otherToken = addCreatureReady(player1,
                creature("Bear Token", CardSubtype.BEAR, true));
        Permanent opponentCreature = addCreatureReady(player2,
                creature("Opponent Bear", CardSubtype.BEAR, false));

        harness.setHand(player1, List.of(new RukarumelBiologist()));
        for (ManaColor color : List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK,
                ManaColor.RED, ManaColor.GREEN)) {
            harness.addMana(player1, color, 1);
        }
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(gqs.computeStaticBonus(gd, nontokenCreature).grantedSubtypes())
                .contains(CardSubtype.GOBLIN);
        assertThat(gqs.computeStaticBonus(gd, sliverToken).grantedSubtypes())
                .contains(CardSubtype.GOBLIN);
        assertThat(gqs.computeStaticBonus(gd, otherToken).grantedSubtypes())
                .doesNotContain(CardSubtype.GOBLIN);
        assertThat(gqs.computeStaticBonus(gd, opponentCreature).grantedSubtypes())
                .doesNotContain(CardSubtype.GOBLIN);

        Card creatureInHand = creature("Hand Bear", CardSubtype.BEAR, false);
        gd.playerHands.get(player1.getId()).add(creatureInHand);
        assertThat(gqs.getCardSubtypes(creatureInHand, gd, player1.getId()))
                .contains(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Rukarumel creates a 1/1 colorless Sliver token")
    void createsSliverToken() {
        addCreatureReady(player1, new RukarumelBiologist());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Sliver");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SLIVER);
    }

    private static Card creature(String name, CardSubtype subtype, boolean token) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.GREEN);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(subtype));
        card.setToken(token);
        return card;
    }
}
