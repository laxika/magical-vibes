package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.Extirpate;
import com.github.laxika.magicalvibes.cards.g.GossamerPhantasm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TreacherousUrge.class, GossamerPhantasm.class, Extirpate.class})
class TreacherousUrgeTest extends BaseCardTest {

    @Test
    @DisplayName("Chooses a creature from the target opponent's hand and puts it under the caster's control")
    void choosesCreatureFromTargetOpponentsHand() {
        harness.setHand(player2, List.of(new Extirpate(), new GossamerPhantasm()));
        castTreacherousUrge();

        PendingInteraction.TargetedHandBattlefieldChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.TargetedHandBattlefieldChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(1);

        harness.handleCardChosen(player1, 1);

        Permanent phantasm = findPermanent(player1, "Gossamer Phantasm");
        assertThat(phantasm.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Extirpate");
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(phantasm.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP));
    }

    @Test
    @DisplayName("Sacrifices the chosen creature at the next end step")
    void sacrificesChosenCreatureAtNextEndStep() {
        Card opponentCreature = new GossamerPhantasm();
        opponentCreature.setOwnerId(player2.getId());
        harness.setHand(player2, List.of(opponentCreature));
        castTreacherousUrge();
        harness.handleCardChosen(player1, 0);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gossamer Phantasm");
        harness.assertInGraveyard(player2, "Gossamer Phantasm");
    }

    @Test
    @DisplayName("Declining leaves the target hand unchanged")
    void decliningLeavesTargetHandUnchanged() {
        harness.setHand(player2, List.of(new GossamerPhantasm()));
        castTreacherousUrge();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Gossamer Phantasm");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class)).isEmpty();
    }

    @Test
    @DisplayName("Does not prompt when the target hand contains no creatures")
    void noCreaturesMeansNoChoice() {
        harness.setHand(player2, List.of(new Extirpate()));
        castTreacherousUrge();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Extirpate");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Haste lasts through cleanup until the next end step")
    void hasteLastsUntilNextEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GossamerPhantasm()));
        castTreacherousUrge();
        harness.withAutoStop(TurnStep.END_STEP, () -> harness.handleCardChosen(player1, 0));

        Permanent phantasm = findPermanent(player1, "Gossamer Phantasm");
        assertThat(gqs.hasKeyword(gd, phantasm, Keyword.HASTE)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, phantasm, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Can target only an opponent")
    void canTargetOnlyOpponent() {
        harness.setHand(player1, List.of(new TreacherousUrge()));
        addManaForSpell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The delayed sacrifice uses the stack before removing the creature")
    void delayedSacrificeAllowsResponses() {
        harness.setHand(player2, List.of(new GossamerPhantasm()));
        castTreacherousUrge();
        harness.handleCardChosen(player1, 0);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Gossamer Phantasm");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gossamer Phantasm");
        harness.assertInGraveyard(player2, "Gossamer Phantasm");
    }

    @Test
    @DisplayName("The caster cannot sacrifice the creature after losing control before the end step")
    void cannotSacrificeCreatureControlledByOpponent() {
        harness.setHand(player2, List.of(new GossamerPhantasm()));
        castTreacherousUrge();
        harness.handleCardChosen(player1, 0);

        Permanent creature = findPermanent(player1, "Gossamer Phantasm");
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Gossamer Phantasm");
        harness.assertNotInGraveyard(player2, "Gossamer Phantasm");
    }

    @Test
    @DisplayName("Losing control in response to the delayed trigger prevents the caster from sacrificing it")
    void cannotSacrificeAfterControlChangesInResponse() {
        harness.setHand(player2, List.of(new GossamerPhantasm()));
        castTreacherousUrge();
        harness.handleCardChosen(player1, 0);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        Permanent creature = findPermanent(player1, "Gossamer Phantasm");
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Gossamer Phantasm");
        harness.assertNotInGraveyard(player2, "Gossamer Phantasm");
    }

    private void castTreacherousUrge() {
        harness.setHand(player1, List.of(new TreacherousUrge()));
        addManaForSpell();
        harness.castAndResolveInstant(player1, 0, player2.getId());
    }

    private void addManaForSpell() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
