package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TangletroveKelp.class, GrizzlyBears.class, Boomerang.class})
class TangletroveKelpTest extends BaseCardTest {

    @Test
    @DisplayName("Animates other Clues you control at the beginning of each combat")
    void animatesOtherCluesAtBeginningOfCombat() {
        harness.addToBattlefield(player1, new TangletroveKelp());
        Permanent ownClue = addClueToken(player1);
        Permanent opposingClue = addClueToken(player2);

        advanceToCombatAndResolve(player1);

        assertThat(gqs.isCreature(gd, ownClue)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownClue)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, ownClue)).isEqualTo(6);
        assertThat(gqs.hasEffectiveSubtype(gd, ownClue, CardSubtype.PLANT)).isTrue();
        assertThat(gqs.isCreature(gd, opposingClue)).isFalse();
    }

    @Test
    @DisplayName("Clue animation wears off at the end of the turn")
    void clueAnimationWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new TangletroveKelp());
        Permanent clue = addClueToken(player1);

        advanceToCombatAndResolve(player1);
        assertThat(gqs.isCreature(gd, clue)).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.CLEANUP);
        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        assertThat(gqs.isCreature(gd, clue)).isFalse();
    }

    @Test
    @DisplayName("Sacrifices to draw a card")
    void sacrificesToDrawACard() {
        Permanent kelp = harness.addToBattlefieldAndReturn(player1, new TangletroveKelp());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int kelpIndex = gd.playerBattlefields.get(player1.getId()).indexOf(kelp);
        harness.activateAbility(player1, kelpIndex, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(kelp);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Animates your Clues during an opponent's combat too")
    void animatesCluesDuringOpponentsCombat() {
        harness.addToBattlefield(player1, new TangletroveKelp());
        Permanent clue = addClueToken(player1);
        Permanent nonClue = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombatAndResolve(player2);

        assertThat(gqs.isCreature(gd, clue)).isTrue();
        assertThat(gqs.getEffectivePower(gd, clue)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, clue)).isEqualTo(6);
        assertThat(gqs.isArtifact(gd, clue)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, clue, CardSubtype.CLUE)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, clue, CardSubtype.PLANT)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, nonClue, CardSubtype.PLANT)).isFalse();
    }

    @Test
    @DisplayName("Clues entering after the trigger resolves remain noncreatures")
    void doesNotAnimateCluesEnteringAfterResolution() {
        harness.addToBattlefield(player1, new TangletroveKelp());
        Permanent earlyClue = addClueToken(player1);

        advanceToCombatAndResolve(player1);
        Permanent lateClue = addClueToken(player1);

        assertThat(gqs.isCreature(gd, earlyClue)).isTrue();
        assertThat(gqs.isCreature(gd, lateClue)).isFalse();
    }

    @Test
    @DisplayName("Clues entering in response to the trigger are animated")
    void includesCluesPresentAtResolution() {
        harness.addToBattlefield(player1, new TangletroveKelp());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).hasSize(1);
        Permanent clue = addClueToken(player1);

        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, clue)).isTrue();
        assertThat(gqs.getEffectivePower(gd, clue)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, clue)).isEqualTo(6);
    }

    @Test
    @DisplayName("Sacrificing Kelp in response does not stop its combat trigger")
    void combatTriggerResolvesAfterSourceIsSacrificed() {
        Permanent kelp = harness.addToBattlefieldAndReturn(player1, new TangletroveKelp());
        Permanent clue = addClueToken(player1);
        harness.setHand(player1, List.of());
        TangletroveKelp drawn = new TangletroveKelp();
        harness.setLibrary(player1, List.of(drawn));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(kelp);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gqs.isCreature(gd, clue)).isFalse();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gqs.isCreature(gd, clue)).isTrue();
        assertThat(gqs.getEffectivePower(gd, clue)).isEqualTo(6);
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when they decline to pay")
    void wardCountersOpponentsSpell() {
        Permanent kelp = harness.addToBattlefieldAndReturn(player1, new TangletroveKelp());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, kelp.getId());
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kelp);
        harness.assertInGraveyard(player2, "Boomerang");
        harness.assertNotInHand(player1, "Tangletrove Kelp");
    }

    @Test
    @DisplayName("Paying ward lets the opponent's spell resolve")
    void payingWardLetsSpellResolve() {
        Permanent kelp = harness.addToBattlefieldAndReturn(player1, new TangletroveKelp());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, kelp.getId());
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(kelp);
        harness.assertInHand(player1, "Tangletrove Kelp");
        harness.assertInGraveyard(player2, "Boomerang");
    }

    @Test
    @DisplayName("Ward does not trigger for its controller's spell")
    void ownSpellDoesNotTriggerWard() {
        Permanent kelp = harness.addToBattlefieldAndReturn(player1, new TangletroveKelp());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, kelp.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(kelp);
        harness.assertInHand(player1, "Tangletrove Kelp");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void advanceToCombatAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();
    }

    private Permanent addClueToken(Player player) {
        Card clueCard = new Card();
        clueCard.setName("Clue");
        clueCard.setType(CardType.ARTIFACT);
        clueCard.setToken(true);
        clueCard.setSubtypes(List.of(CardSubtype.CLUE));
        Permanent clue = new Permanent(clueCard);
        clue.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(clue);
        return clue;
    }
}
