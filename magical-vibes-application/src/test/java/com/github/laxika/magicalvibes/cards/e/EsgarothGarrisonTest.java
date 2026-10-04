package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EsgarothGarrison.class, Forest.class, GrizzlyBears.class})
class EsgarothGarrisonTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of creatures you control and toughness stays 5")
    void powerEqualsControlledCreatures() {
        Permanent garrison = addCreatureReady(player1, new EsgarothGarrison());

        assertThat(gqs.getEffectivePower(gd, garrison)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, garrison)).isEqualTo(5);

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, garrison)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, garrison)).isEqualTo(5);
    }

    @Test
    @DisplayName("Recruit creates a Soldier after discarding a nonland card")
    void recruitCreatesSoldierForNonlandDiscard() {
        castAndResolve(new GrizzlyBears(), new Forest());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Recruit does not create a Soldier after discarding a land card")
    void recruitDoesNotCreateSoldierForLandDiscard() {
        castAndResolve(new Forest(), new GrizzlyBears());

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Recruit creates a white 1/1 Human Soldier token")
    void recruitTokenHasBothCreatureTypes() {
        castAndResolve(new EsgarothGarrison(), new Forest());

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.SOLDIER);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Esgaroth Garrison"))).isEqualTo(2);
    }

    @Test
    @DisplayName("Recruit can discard the drawn card and creates its token during the same resolution")
    void recruitDiscardsDrawnCardWithoutAnotherTrigger() {
        harness.setLibrary(player1, List.of(new EsgarothGarrison()));
        harness.castFromHand(player1, new EsgarothGarrison(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Esgaroth Garrison");
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Esgaroth Garrison");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).count()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The power-defining ability also works in hand and updates as creatures leave")
    void powerInHandTracksControlledCreatures() {
        EsgarothGarrison card = new EsgarothGarrison();
        harness.setHand(player1, List.of(card));
        assertThat(gqs.getEffectiveCardPower(gd, card)).isZero();
        harness.addToBattlefield(player1, new EsgarothGarrison());
        harness.addToBattlefield(player2, new EsgarothGarrison());
        assertThat(gqs.getEffectiveCardPower(gd, card)).isEqualTo(1);
        gd.playerBattlefields.get(player1.getId()).clear();
        assertThat(gqs.getEffectiveCardPower(gd, card)).isZero();
    }

    private void castAndResolve(Card discardedCard, Card drawnCard) {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new EsgarothGarrison(), discardedCard));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        if (gd.interaction.isAwaitingInput()) {
            harness.handleCardChosen(player1, 0);
            harness.passBothPriorities();
        }
    }
}
