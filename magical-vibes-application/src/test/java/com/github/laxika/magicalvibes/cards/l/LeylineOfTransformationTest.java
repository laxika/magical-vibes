package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeylineOfTransformation.class, GrizzlyBears.class})
class LeylineOfTransformationTest extends BaseCardTest {

    @Test
    @DisplayName("Leyline in the opening hand may begin the game on the battlefield")
    void leylineInOpeningHandMayStartOnBattlefield() {
        GameTestHarness openingHarness = new GameTestHarness();
        openingHarness.setHand(openingHarness.getPlayer1(), List.of(new LeylineOfTransformation()));
        openingHarness.skipMulligan();

        assertThat(openingHarness.getGameData().interaction.isAwaitingInput()).isTrue();

        openingHarness.handleMayAbilityChosen(openingHarness.getPlayer1(), true);
        assertThat(openingHarness.getGameData().interaction.isAwaitingInput()).isTrue();
        openingHarness.handleListChoice(openingHarness.getPlayer1(), CardSubtype.GOBLIN.name());

        Permanent leyline = openingHarness.getGameData().playerBattlefields
                .get(openingHarness.getPlayer1().getId()).stream()
                .filter(p -> p.getCard().getName().equals("Leyline of Transformation"))
                .findFirst()
                .orElseThrow();
        assertThat(leyline.getChosenSubtype()).isEqualTo(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("Leyline grants the chosen type to own creatures and creature cards outside the battlefield")
    void grantsChosenTypeToOwnCreaturesAndCreatureCardsOutsideBattlefield() {
        Card battlefieldBear = new GrizzlyBears();
        Card handBear = creature("Hand Bear", CardSubtype.BEAR);
        Card graveyardBear = creature("Graveyard Bear", CardSubtype.BEAR);
        Card opponentBear = creature("Opponent Bear", CardSubtype.BEAR);
        harness.addToBattlefield(player1, battlefieldBear);
        harness.setHand(player1, List.of(new LeylineOfTransformation(), handBear));
        gd.playerGraveyards.get(player1.getId()).add(graveyardBear);
        gd.playerHands.get(player2.getId()).add(opponentBear);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        Permanent battlefieldBearPermanent = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.computeStaticBonus(gd, battlefieldBearPermanent).grantedSubtypes())
                .contains(CardSubtype.GOBLIN);
        assertThat(gqs.cardHasSubtype(handBear, CardSubtype.GOBLIN, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasSubtype(graveyardBear, CardSubtype.GOBLIN, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasSubtype(opponentBear, CardSubtype.GOBLIN, gd, player2.getId())).isFalse();
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
