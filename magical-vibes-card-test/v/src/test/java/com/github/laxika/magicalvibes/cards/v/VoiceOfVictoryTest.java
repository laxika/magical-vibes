package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Voice of Victory")
@CardUsed({VoiceOfVictory.class, ProdigalSorcerer.class, Shock.class})
class VoiceOfVictoryTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates two tapped and attacking red Warrior tokens")
    void attackingCreatesWarriorTokens() {
        addCreatureReady(player1, new VoiceOfVictory());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.WARRIOR);
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttackedThisTurn()).isTrue();
        });
    }

    @Test
    @DisplayName("Attack tokens are sacrificed at the beginning of the next end step")
    void attackTokensAreSacrificedAtNextEndStep() {
        addCreatureReady(player1, new VoiceOfVictory());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isEqualTo(2);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList()).isEmpty();
    }

    @Test
    @DisplayName("Opponents cannot cast spells during the controller's turn")
    void opponentsCannotCastDuringControllerTurn() {
        Permanent voice = addCreatureReady(player1, new VoiceOfVictory());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, voice.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("The restriction does not block activated abilities")
    void restrictionDoesNotBlockActivatedAbilities() {
        addCreatureReady(player1, new VoiceOfVictory());
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passPriority(player1);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(sorcerer),
                null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Opponents can cast spells during their own turn")
    void opponentsCanCastDuringTheirTurn() {
        addCreatureReady(player1, new VoiceOfVictory());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("The controller can cast spells during their own turn")
    void controllerCanCastDuringTheirTurn() {
        addCreatureReady(player1, new VoiceOfVictory());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Mobilize resolves and sacrifices its tokens even if Voice leaves before resolution")
    void mobilizeSurvivesSourceRemoval() {
        Permanent voice = addCreatureReady(player1, new VoiceOfVictory());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        harness.castInstant(player1, 0, voice.getId());
        harness.castInstant(player1, 0, voice.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Voice of Victory");
        assertThat(countPermanents(player1, "Warrior")).isEqualTo(2);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Warrior")).isZero();
    }
}
