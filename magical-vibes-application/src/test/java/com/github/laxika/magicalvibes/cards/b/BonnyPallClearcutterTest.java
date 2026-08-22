package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BonnyPallClearcutter.class, Forest.class, GrizzlyBears.class, Island.class})
class BonnyPallClearcutterTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a legendary Beau whose power and toughness equal your land count")
    void createsDynamicBeauToken() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new BonnyPallClearcutter()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent beau = findPermanent(player1, "Beau");
        assertThat(beau.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(beau.getCard().getSubtypes()).contains(CardSubtype.OX);
        assertThat(beau.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(gqs.getEffectivePower(gd, beau)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, beau)).isEqualTo(2);

        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, beau)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, beau)).isEqualTo(3);
    }

    @Test
    @DisplayName("Draws before offering a land from hand to the battlefield when attacking")
    void drawsThenPutsLandFromHandOntoBattlefield() {
        addAttackingBonnyAndBear();
        Card landInHand = new Forest();
        Card drawnLand = new Island();
        harness.setHand(player1, List.of(landInHand));
        harness.setLibrary(player1, List.of(drawnLand));

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnLand, landInHand);
        PendingInteraction.PutCardFromHandOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutCardFromHandOrGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(landInHand.getId());

        harness.handleMultipleCardsChosen(player1, List.of(landInHand.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnLand);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(landInHand.getId()));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Offers a land from the graveyard and can decline the choice")
    void putsLandFromGraveyardOrDeclines() {
        addAttackingBonnyAndBear();
        Card landInGraveyard = new Forest();
        Card drawnLand = new Island();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnLand));
        harness.setGraveyard(player1, List.of(landInGraveyard));

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnLand);
        PendingInteraction.PutCardFromHandOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutCardFromHandOrGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(landInGraveyard.getId());

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(landInGraveyard);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .noneMatch(permanent -> permanent.getCard().getId().equals(landInGraveyard.getId())))
                .isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addAttackingBonnyAndBear() {
        addCreatureReady(player1, new BonnyPallClearcutter());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());
    }
}
