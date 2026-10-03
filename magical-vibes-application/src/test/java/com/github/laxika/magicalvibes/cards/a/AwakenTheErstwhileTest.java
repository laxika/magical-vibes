package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FeralMaaka;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AwakenTheErstwhile.class, FeralMaaka.class, Forest.class, Mountain.class})
class AwakenTheErstwhileTest extends BaseCardTest {

    @Test
    @DisplayName("Each player discards their hand and creates Zombies equal to their own discard count")
    void discardsHandsAndCreatesPerPlayerZombieCounts() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new AwakenTheErstwhile(), new FeralMaaka(), new Mountain()));
        harness.setHand(player2, List.of(new Forest(), new FeralMaaka(), new Mountain(), new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, (UUID) null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(2);
        assertThat(countPermanents(player2, "Zombie")).isEqualTo(4);
        assertThat(findPermanents(player1, "Zombie"))
                .allMatch(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.ZOMBIE)
                        && permanent.getEffectivePower() == 2
                        && permanent.getEffectiveToughness() == 2);
        assertThat(findPermanents(player2, "Zombie"))
                .allMatch(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.ZOMBIE)
                        && permanent.getEffectivePower() == 2
                        && permanent.getEffectiveToughness() == 2);
    }

    @Test
    @DisplayName("An empty opposing hand creates no Zombies")
    void emptyOpponentHandCreatesNoTokens() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new AwakenTheErstwhile(), new Forest()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, (UUID) null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
        assertThat(countPermanents(player2, "Zombie")).isZero();
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Awaken the Erstwhile");
    }

    @Test
    @DisplayName("Casting the last card in hand does not count the spell as discarded")
    void emptyCasterHandStillDiscardsOpponentHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new AwakenTheErstwhile()));
        harness.setHand(player2, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, (UUID) null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(countPermanents(player1, "Zombie")).isZero();
        assertThat(countPermanents(player2, "Zombie")).isEqualTo(1);
        harness.assertInGraveyard(player2, "Mountain");
    }

    @Test
    @DisplayName("No tokens are created when both hands are empty on resolution")
    void bothHandsEmptyCreatesNoTokens() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new AwakenTheErstwhile()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, (UUID) null);

        assertThat(countPermanents(player1, "Zombie")).isZero();
        assertThat(countPermanents(player2, "Zombie")).isZero();
        harness.assertInGraveyard(player1, "Awaken the Erstwhile");
    }
}
