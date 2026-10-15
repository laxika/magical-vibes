package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LightfootTechnique;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SageOfTheSkies.class, Shock.class, LightfootTechnique.class})
class SageOfTheSkiesTest extends BaseCardTest {

    @Test
    @DisplayName("Does not create a token copy when no other spell was cast this turn")
    void noTokenCopyWithoutAnotherSpell() {
        prepareMainPhase();
        harness.castFromHand(player1, new SageOfTheSkies(), "{2}{W}");
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

        harness.castFromHand(player1, new SageOfTheSkies(), "{2}{W}");
        resolveAllTriggers();

        assertThat(tokenCount()).isEqualTo(1);
        assertThat(findPermanents(player1, "Sage of the Skies")).hasSize(2);
    }

    @Test
    @DisplayName("A spell cast in response cannot enable the copy ability")
    void laterSpellDoesNotEnableCopy() {
        prepareMainPhase();
        var target = harness.addToBattlefieldAndReturn(player1, new SageOfTheSkies());
        harness.castFromHand(player1, new SageOfTheSkies(), "{2}{W}");

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new LightfootTechnique()));
        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(tokenCount()).isZero();
        assertThat(findPermanents(player1, "Sage of the Skies")).hasSize(2);
    }

    @Test
    @DisplayName("A previously cast creature spell enables exactly one copy")
    void priorCreatureSpellEnablesCopy() {
        prepareMainPhase();
        harness.castFromHand(player1, new SageOfTheSkies(), "{2}{W}");
        resolveAllTriggers();
        harness.castFromHand(player1, new SageOfTheSkies(), "{2}{W}");
        resolveAllTriggers();

        assertThat(tokenCount()).isEqualTo(1);
        assertThat(findPermanents(player1, "Sage of the Skies")).hasSize(3);
    }

    @Test
    @DisplayName("An opponent's earlier spell does not enable the copy ability")
    void opponentSpellDoesNotEnableCopy() {
        prepareMainPhase();
        var target = harness.addToBattlefieldAndReturn(player2, new SageOfTheSkies());
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new LightfootTechnique()));
        harness.castInstant(player2, 0, target.getId());
        resolveAllTriggers();

        harness.castFromHand(player1, new SageOfTheSkies(), "{2}{W}");
        resolveAllTriggers();

        assertThat(tokenCount()).isZero();
        assertThat(findPermanents(player1, "Sage of the Skies")).hasSize(1);
    }

    @Test
    @DisplayName("The resolved token copy gains life when it deals combat damage")
    void tokenCopyHasFunctionalLifelink() {
        prepareMainPhase();
        harness.castFromHand(player1, new SageOfTheSkies(), "{2}{W}");
        resolveAllTriggers();
        harness.castFromHand(player1, new SageOfTheSkies(), "{2}{W}");
        resolveAllTriggers();

        var battlefield = gd.playerBattlefields.get(player1.getId());
        var token = battlefield.stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        token.setSummoningSick(false);
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        declareAttackers(List.of(battlefield.indexOf(token)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 18);
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
