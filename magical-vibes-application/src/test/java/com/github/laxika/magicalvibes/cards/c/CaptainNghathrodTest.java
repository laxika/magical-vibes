package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.p.PhyrexianRager;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaptainNghathrod.class, Forest.class, GrizzlyBears.class, MindStone.class, PhyrexianRager.class})
class CaptainNghathrodTest extends BaseCardTest {

    @org.junit.jupiter.api.BeforeEach
    void keepTurnStepsAvailableForAssertions() {
        for (var player : java.util.List.of(player1, player2)) {
            gd.playerAutoStopSteps.put(player.getId(), java.util.EnumSet.of(
                    com.github.laxika.magicalvibes.model.TurnStep.UPKEEP,
                    com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN,
                    com.github.laxika.magicalvibes.model.TurnStep.COMBAT_DAMAGE,
                    com.github.laxika.magicalvibes.model.TurnStep.POSTCOMBAT_MAIN));
        }
    }


    @Test
    void givesHorrorsYouControlMenace() {
        Permanent captain = addCreatureReady(player1, new CaptainNghathrod());

        assertThat(gqs.hasKeyword(gd, captain, Keyword.MENACE)).isTrue();
    }

    @Test
    void doesNotGiveNonHorrorsMenace() {
        addCreatureReady(player1, new CaptainNghathrod());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bear, Keyword.MENACE)).isFalse();
    }

    @Test
    void givesOtherFriendlyHorrorsButNotOpposingHorrorsMenace() {
        addCreatureReady(player1, new CaptainNghathrod());
        Permanent friendlyHorror = addCreatureReady(player1, new PhyrexianRager());
        Permanent opposingHorror = addCreatureReady(player2, new PhyrexianRager());

        assertThat(gqs.hasKeyword(gd, friendlyHorror, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingHorror, Keyword.MENACE)).isFalse();
    }

    @Test
    void anotherHorrorsCombatDamageMillsItsDamageAmount() {
        addCreatureReady(player1, new CaptainNghathrod());
        Permanent horror = addCreatureReady(player1, new PhyrexianRager());
        horror.setAttacking(true);
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        harness.assertLife(player2, 18);
    }

    @Test
    void nonHorrorCombatDamageDoesNotMill() {
        addCreatureReady(player1, new CaptainNghathrod());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.setAttacking(true);
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        harness.assertLife(player2, 18);
    }

    @Test
    void horrorCombatDamageMillsThatManyCards() {
        Permanent captain = addCreatureReady(player1, new CaptainNghathrod());
        captain.setAttacking(true);
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new MindStone(), new Forest()));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    void endStepReturnsOnlyAQualifyingCardPutIntoGraveyardFromLibrary() {
        Card eligible = new GrizzlyBears();
        Card ineligible = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(ineligible, eligible));
        gd.cardsPutIntoGraveyardFromLibraryThisTurn
                .computeIfAbsent(player2.getId(), ignored -> new HashSet<>())
                .add(eligible.getId());
        harness.addToBattlefield(player1, new CaptainNghathrod());

        advanceToEndStep(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(eligible.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(ineligible);
    }

    @Test
    void endStepCanReturnAnArtifactMilledByCombatDamageButNotALand() {
        Permanent captain = addCreatureReady(player1, new CaptainNghathrod());
        captain.setAttacking(true);
        Card artifact = new MindStone();
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        harness.setLibrary(player2, List.of(artifact, creature, land));

        resolveCombat();
        harness.passBothPriorities();
        advanceToEndStep(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(artifact.getId(), creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mind Stone");
        harness.assertNotOnBattlefield(player2, "Mind Stone");
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrder(creature, land);
    }

    @Test
    void doesNotReanimateFromItsControllersGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        gd.cardsPutIntoGraveyardFromLibraryThisTurn
                .computeIfAbsent(player1.getId(), ignored -> new HashSet<>())
                .add(creature.getId());
        harness.addToBattlefield(player1, new CaptainNghathrod());

        advanceToEndStep(player1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void doesNotTriggerAtAnOpponentsEndStep() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        gd.cardsPutIntoGraveyardFromLibraryThisTurn
                .computeIfAbsent(player2.getId(), ignored -> new HashSet<>())
                .add(creature.getId());
        harness.addToBattlefield(player1, new CaptainNghathrod());

        advanceToEndStep(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
