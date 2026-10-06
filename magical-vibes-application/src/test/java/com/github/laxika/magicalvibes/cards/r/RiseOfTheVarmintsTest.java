package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.p.PatientNaturalist;
import com.github.laxika.magicalvibes.cards.t.TakeUpTheShield;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiseOfTheVarmints.class, PatientNaturalist.class, TakeUpTheShield.class})
class RiseOfTheVarmintsTest extends BaseCardTest {

    @Test
    void createsOneVarmintPerCreatureCardInControllerGraveyard() {
        harness.setGraveyard(player1, List.of(new PatientNaturalist(), new PatientNaturalist(), new TakeUpTheShield()));
        harness.setGraveyard(player2, List.of(new PatientNaturalist()));
        harness.castFromHand(player1, new RiseOfTheVarmints(), "{3}{G}");
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).filteredOn(p -> p.getCard().isToken())
                .hasSize(2)
                .allSatisfy(token -> {
                    assertThat(token.getCard().getName()).isEqualTo("Varmint");
                    assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.VARMINT);
                    assertThat(token.getCard().getPower()).isEqualTo(2);
                    assertThat(token.getCard().getToughness()).isEqualTo(1);
                    assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
                    assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
                });
    }

    @Test
    void createsNoTokensWithNoCreatureCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new TakeUpTheShield()));
        harness.castFromHand(player1, new RiseOfTheVarmints(), "{3}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .isEmpty();
    }

    @Test
    void countsCreatureCardsAtResolutionRatherThanWhenCast() {
        harness.setGraveyard(player1, List.of(new PatientNaturalist()));
        harness.castFromHand(player1, new RiseOfTheVarmints(), "{3}{G}");
        harness.setGraveyard(player1, List.of(new PatientNaturalist(), new PatientNaturalist()));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken()).hasSize(2);
    }

    @Test
    void plotsAndCastsWithoutManaOnALaterTurn() {
        RiseOfTheVarmints rise = new RiseOfTheVarmints();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(rise));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(rise);
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.castFromExile(player1, rise.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new PatientNaturalist()));
        harness.castFromExile(player1, rise.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(rise);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(rise);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken()).hasSize(1);
    }
}
