package com.github.laxika.magicalvibes.cards.n;

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

@CardUsed({NewsHelicopter.class})
class NewsHelicopterTest extends BaseCardTest {

    @Test
    @DisplayName("When News Helicopter enters, it creates a green and white Human Citizen token")
    void enteringCreatesHumanCitizenToken() {
        harness.setHand(player1, List.of(new NewsHelicopter()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Human Citizen");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getColors())
                .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.CITIZEN);
    }

    @Test
    @DisplayName("The Citizen is created only when the enter trigger resolves")
    void tokenWaitsForEnterTriggerToResolve() {
        harness.setHand(player1, List.of(new NewsHelicopter()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        assertThat(countPermanents(player1, "Human Citizen")).isZero();

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "News Helicopter");
        assertThat(countPermanents(player1, "Human Citizen")).isZero();

        resolveAllTriggers();

        assertThat(countPermanents(player1, "Human Citizen")).isEqualTo(1);
        assertThat(countPermanents(player2, "Human Citizen")).isZero();
        assertThat(findPermanent(player1, "Human Citizen").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Entering without being cast creates a Citizen for the entering creature's controller")
    void enteringWithoutCastingCreatesTokenForController() {
        harness.enterBattlefieldAndReturn(player2, new NewsHelicopter());

        resolveAllTriggers();

        assertThat(countPermanents(player2, "Human Citizen")).isEqualTo(1);
        assertThat(countPermanents(player1, "Human Citizen")).isZero();
        harness.assertOnBattlefield(player2, "News Helicopter");
    }
}
