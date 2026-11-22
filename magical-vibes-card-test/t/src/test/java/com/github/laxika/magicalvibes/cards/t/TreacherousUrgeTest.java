package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.Extirpate;
import com.github.laxika.magicalvibes.cards.g.GossamerPhantasm;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.UUID;
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

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(handCard(player2, 1));

        harness.handleMultipleCardsChosen(player1, List.of(handCard(player2, 1)));

        Permanent phantasm = findPermanent(player1, "Gossamer Phantasm");
        assertThat(gqs.hasKeyword(gd, phantasm, Keyword.HASTE)).isTrue();
        assertThat(als.canAttack(gd, phantasm, gd.findControllerOf(phantasm))).isTrue();
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Extirpate");
        assertThat(gd.temporaryGlobalTriggeredAbilities).singleElement()
                .matches(delayed -> phantasm.getId().equals(delayed.watchedPermanentId()));
    }

    @Test
    @DisplayName("Sacrifices the chosen creature at the next end step")
    void sacrificesChosenCreatureAtNextEndStep() {
        Card opponentCreature = new GossamerPhantasm();
        opponentCreature.setOwnerId(player2.getId());
        harness.setHand(player2, List.of(opponentCreature));
        castTreacherousUrge();
        harness.handleMultipleCardsChosen(player1, List.of(handCard(player2, 0)));

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

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Gossamer Phantasm");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.temporaryGlobalTriggeredAbilities).isEmpty();
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
        harness.withAutoStop(TurnStep.END_STEP,
                () -> harness.handleMultipleCardsChosen(player1, List.of(handCard(player2, 0))));

        Permanent phantasm = findPermanent(player1, "Gossamer Phantasm");
        assertThat(gqs.hasKeyword(gd, phantasm, Keyword.HASTE)).isTrue();
        assertThat(als.canAttack(gd, phantasm, gd.findControllerOf(phantasm))).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, phantasm, Keyword.HASTE)).isTrue();
        assertThat(als.canAttack(gd, phantasm, gd.findControllerOf(phantasm))).isTrue();
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
        harness.handleMultipleCardsChosen(player1, List.of(handCard(player2, 0)));

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
        harness.handleMultipleCardsChosen(player1, List.of(handCard(player2, 0)));

        Permanent creature = findPermanent(player1, "Gossamer Phantasm");
        setOwnerAndController(creature, player2, player2);

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
        harness.handleMultipleCardsChosen(player1, List.of(handCard(player2, 0)));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        Permanent creature = findPermanent(player1, "Gossamer Phantasm");
        setOwnerAndController(creature, player2, player2);
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

    /** The id of the card at {@code index} of {@code player}'s hand. */
    private UUID handCard(Player player, int index) {
        return gd.playerHands.get(player.getId()).get(index).getId();
    }
}
