package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarVanguard;
import com.github.laxika.magicalvibes.cards.r.Recoup;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SaprolingSymbiosis.class, LlanowarVanguard.class, Forest.class, Recoup.class})
class SaprolingSymbiosisTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one 1/1 green Saproling token for each creature controlled")
    void createsTokenPerCreatureControlled() {
        harness.addToBattlefield(player1, new LlanowarVanguard());
        harness.addToBattlefield(player1, new LlanowarVanguard());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new LlanowarVanguard());
        harness.setHand(player1, List.of(new SaprolingSymbiosis()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        List<Permanent> tokens = findPermanents(player1, "Saproling");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getName()).isEqualTo("Saproling");
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
        });
    }

    @Test
    @DisplayName("Creates no tokens when no creatures are controlled")
    void createsNoTokensWithoutCreatures() {
        harness.setHand(player1, List.of(new SaprolingSymbiosis()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(findPermanents(player1, "Saproling")).isEmpty();
    }

    @Test
    @DisplayName("Can be cast at instant speed by paying two more")
    void canBeCastAtInstantSpeedForTwoMore() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SaprolingSymbiosis()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castWithAlternateCost(player1, 0, (UUID) null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Saproling Symbiosis");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot be cast at instant speed without the surcharge")
    void cannotBeCastAtInstantSpeedWithoutSurcharge() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SaprolingSymbiosis()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counts creatures present at resolution rather than casting")
    void countsCreaturesAtResolution() {
        harness.addToBattlefield(player1, new LlanowarVanguard());
        harness.setHand(player1, List.of(new SaprolingSymbiosis()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.addToBattlefield(player1, new LlanowarVanguard());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).hasSize(2);
    }

    @Test
    @DisplayName("Existing creature tokens count without counting newly created tokens again")
    void countsExistingTokens() {
        harness.addToBattlefield(player1, new LlanowarVanguard());
        harness.setHand(player1, List.of(new SaprolingSymbiosis(), new SaprolingSymbiosis()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(findPermanents(player1, "Saproling")).hasSize(3);
    }

    @Test
    @DisplayName("Flash surcharge can be paid alongside flashback granted by Recoup")
    @CardUsed({SaprolingSymbiosis.class, LlanowarVanguard.class, Recoup.class})
    void canPayFlashSurchargeWithGrantedFlashback() {
        SaprolingSymbiosis symbiosis = new SaprolingSymbiosis();
        harness.addToBattlefield(player1, new LlanowarVanguard());
        harness.setGraveyard(player1, List.of(symbiosis));
        harness.setHand(player1, List.of(new Recoup()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, symbiosis.getId());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castFlashback(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).hasSize(1);
        harness.assertNotInGraveyard(player1, "Saproling Symbiosis");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(symbiosis.getId()));
    }
}
