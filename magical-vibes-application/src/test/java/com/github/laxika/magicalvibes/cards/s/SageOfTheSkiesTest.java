package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SageOfTheSkies.class, Shock.class})
class SageOfTheSkiesTest extends BaseCardTest {

    @Test
    @DisplayName("Does not create a token copy when no other spell was cast this turn")
    void noTokenCopyWithoutAnotherSpell() {
        prepareMainPhase();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of(new SageOfTheSkies()));

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(tokenCount()).isZero();
        assertThat(findPermanents(player1, "Sage of the Skies")).hasSize(1);
    }

    @Test
    @DisplayName("Creates a token copy when another spell was cast this turn")
    void createsTokenCopyAfterAnotherSpell() {
        prepareMainPhase();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player1, List.of(new SageOfTheSkies()));
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(tokenCount()).isEqualTo(1);
        assertThat(findPermanents(player1, "Sage of the Skies")).hasSize(2);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    private long tokenCount() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count();
    }
}
