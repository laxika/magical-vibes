package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ParallelLives;
import com.github.laxika.magicalvibes.cards.q.QueenAllenalOfRuadach;
import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LucyMacLeanPositivelyArmed.class, RaiseTheAlarm.class, GrizzlyBears.class})
class LucyMacLeanPositivelyArmedTest extends BaseCardTest {

    @Test
    @DisplayName("A token entering under your control may be copied for an opponent and draws a card")
    void copiesOwnTokenForOpponentAndDraws() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addLucy(player1);

        createTokens(player1);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPlayerIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(countTokens(player2)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's token may be copied for you without drawing")
    void copiesOpponentsTokenForControllerWithoutDrawing() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addLucy(player1);

        createTokens(player2);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPlayerIds()).containsExactly(player1.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(countTokens(player1)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The trigger fires only once each turn")
    void triggersOnlyOnceEachTurn() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        addLucy(player1);

        createTokens(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        createTokens(player1);
        harness.passBothPriorities();

        assertThat(countTokens(player2)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Simultaneous tokens each offer a copy until one is accepted")
    void canDeclineFirstSimultaneousTokenAndCopySecond() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addLucy(player1);

        createTokens(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validPlayerIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(countTokens(player2)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @CardUsed(Shock.class)
    @DisplayName("A token removed before resolution is copied using last known information")
    void copiesTokenThatLeftBattlefieldBeforeResolution() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addLucy(player1);

        createTokens(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getId().equals(gd.stack.getLast().getTriggeringPermanentId()))
                .findFirst().orElseThrow();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, token.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(countTokens(player2)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @CardUsed({QueenAllenalOfRuadach.class, ParallelLives.class})
    @DisplayName("The opponent creating the copy orders their replacements, and the draw waits for the copy")
    void opponentOrdersReplacementsBeforeTheDraw() {
        acceptCopyForOpponentUnderQueenAndParallelLives();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.context()).isInstanceOf(ChoiceContext.ReplacementEffectOrder.class);
        assertThat(countTokens(player2)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @CardUsed({QueenAllenalOfRuadach.class, ParallelLives.class})
    @DisplayName("Applying Queen Allenal before Parallel Lives doubles the copy and the added Soldier, then draws")
    void queenFirstDoublesCopyAndSoldierThenDraws() {
        acceptCopyForOpponentUnderQueenAndParallelLives();

        chooseReplacement("Queen Allenal of Ruadach");
        resolveAllTriggers();

        assertThat(countTokens(player2)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @CardUsed({QueenAllenalOfRuadach.class, ParallelLives.class})
    @DisplayName("Applying Parallel Lives before Queen Allenal adds one undoubled Soldier, then draws")
    void parallelLivesFirstAddsOneSoldierThenDraws() {
        acceptCopyForOpponentUnderQueenAndParallelLives();

        chooseReplacement("Parallel Lives");
        resolveAllTriggers();

        assertThat(countTokens(player2)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void acceptCopyForOpponentUnderQueenAndParallelLives() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addLucy(player1);
        harness.addToBattlefield(player2, new QueenAllenalOfRuadach());
        harness.addToBattlefield(player2, new ParallelLives());

        createTokens(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
    }

    private void chooseReplacement(String optionPrefix) {
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player2, choice.options().stream()
                .filter(option -> option.startsWith(optionPrefix)).findFirst().orElseThrow());
    }

    private void addLucy(Player player) {
        harness.addToBattlefield(player, new LucyMacLeanPositivelyArmed());
    }

    private void createTokens(Player player) {
        List<Card> hand = new ArrayList<>(gd.playerHands.get(player.getId()));
        hand.add(new RaiseTheAlarm());
        harness.setHand(player, hand);
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player, hand.size() - 1);
    }

    private long countTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count();
    }
}
