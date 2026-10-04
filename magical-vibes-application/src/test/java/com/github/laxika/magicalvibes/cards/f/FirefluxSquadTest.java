package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FirefluxSquad.class, GrizzlyBears.class, FountainOfYouth.class})
class FirefluxSquadTest extends BaseCardTest {

    @Test
    @DisplayName("The attack trigger only targets another attacking creature you control")
    void attackTriggerRestrictsTargets() {
        Permanent fireflux = addCreatureReady(player1, new FirefluxSquad());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0, 1));

        var validIds = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds();
        assertThat(validIds).contains(otherAttacker.getId());
        assertThat(validIds).doesNotContain(fireflux.getId(), opponentCreature.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPlayerIds())
                .doesNotContain(player1.getId());
    }

    @Test
    @DisplayName("Exiles the chosen attacker and puts the first revealed creature onto the battlefield tapped and attacking")
    void exilesAttackerAndRevealsCreature() {
        addCreatureReady(player1, new FirefluxSquad());
        Permanent exiledAttacker = addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears revealedCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new FountainOfYouth(), revealedCreature));
        gd.playerAutoStopSteps.put(player2.getId(), java.util.Set.of(TurnStep.DECLARE_BLOCKERS));

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, exiledAttacker.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice attackTarget =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(attackTarget.validPlayerIds()).containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.findExiledCard(exiledAttacker.getOriginalCard().getId())).isNotNull();
        Permanent enteredCreature = findPermanent(player1, "Grizzly Bears");
        assertThat(enteredCreature.isTapped()).isTrue();
        assertThat(enteredCreature.isAttacking()).isTrue();
        assertThat(enteredCreature.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Fountain of Youth");
    }

    @Test
    @DisplayName("The optional trigger does nothing when no other creature is attacking")
    void noOtherAttackerDoesNothing() {
        addCreatureReady(player1, new FirefluxSquad());
        FountainOfYouth topCard = new FountainOfYouth();
        harness.setLibrary(player1, List.of(topCard, new GrizzlyBears()));

        declareAttackers(List.of(0));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Fountain of Youth", "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("The controller may decline exile when the targeted trigger resolves")
    void mayDeclineExileAtResolution() {
        addCreatureReady(player1, new FirefluxSquad());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        FountainOfYouth topCard = new FountainOfYouth();
        GrizzlyBears creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, creature));

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.findExiledCard(attacker.getOriginalCard().getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, creature);
    }

    @Test
    @DisplayName("A target that stops attacking prevents exile and revealing")
    void targetNoLongerAttackingDoesNothing() {
        addCreatureReady(player1, new FirefluxSquad());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        FountainOfYouth topCard = new FountainOfYouth();
        GrizzlyBears creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, creature));

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        attacker.setAttacking(false);
        resolveAllTriggers();

        assertThat(gd.findExiledCard(attacker.getOriginalCard().getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, creature);
    }

    @Test
    @DisplayName("With no creature in the library, exile still happens and all revealed cards return")
    void noCreatureInLibraryReturnsAllRevealedCards() {
        addCreatureReady(player1, new FirefluxSquad());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        FountainOfYouth first = new FountainOfYouth();
        FountainOfYouth second = new FountainOfYouth();
        harness.setLibrary(player1, List.of(first, second));

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.findExiledCard(attacker.getOriginalCard().getId())).isNotNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("An animated artifact is a legal attacking creature to exile")
    void exilesAnimatedArtifactCreature() {
        addCreatureReady(player1, new FirefluxSquad());
        Permanent attacker = addCreatureReady(player1, new FountainOfYouth());
        attacker.setAnimatedUntilEndOfTurn(true);
        attacker.setAnimatedPower(2);
        attacker.setAnimatedToughness(2);
        GrizzlyBears creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));

        declareAttackers(List.of(0, 1));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(attacker.getId());
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.findExiledCard(attacker.getOriginalCard().getId())).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
    }
}
