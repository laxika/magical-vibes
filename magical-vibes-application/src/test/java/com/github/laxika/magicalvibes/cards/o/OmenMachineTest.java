package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.a.Artillerize;
import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.d.Dismember;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.cards.s.SurgicalExtraction;
import com.github.laxika.magicalvibes.cards.t.TrollAscetic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GitaxianProbe;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OmenMachine.class, GrizzlyBears.class, Plains.class, Pyroclasm.class, Shock.class,
        Dismember.class, SurgicalExtraction.class, TrollAscetic.class, RuleOfLaw.class,
        AuraOfSilence.class, Artillerize.class, GitaxianProbe.class})
class OmenMachineTest extends BaseCardTest {

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid first-turn draw skip
        harness.forceStep(TurnStep.UPKEEP);
        harness.passUntil(activePlayer, TurnStep.DRAW);
    }

    @Test
    @DisplayName("Normal draw step draw is prevented")
    void normalDrawIsPrevented() {
        harness.addToBattlefield(player1, new OmenMachine());
        // Put a known card on top so we can track it
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);

        advanceToDraw(player1);

        // The normal draw should not have added any cards to hand
        // (Omen Machine trigger may have changed the deck, but hand should not grow from draw)
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Opponent's draw step draw is also prevented")
    void opponentDrawIsPrevented() {
        harness.addToBattlefield(player1, new OmenMachine());

        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player2.getId()).addFirst(topCard);

        advanceToDraw(player2);

        // Opponent should not draw the card into hand
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Land on top of library is put onto the battlefield")
    void landOnTopGoesToBattlefield() {
        harness.addToBattlefield(player1, new OmenMachine());

        Card plains = new Plains();
        gd.playerDecks.get(player1.getId()).addFirst(plains);

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve Omen Machine trigger

        // Plains should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == plains);

        // Not in exile
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(plains);

        // Not in library
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(plains);
    }

    @Test
    @DisplayName("Non-targeted sorcery on top is cast without paying mana cost")
    void nonTargetedSorceryIsCastForFree() {
        harness.addToBattlefield(player1, new OmenMachine());
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2

        Card pyroclasm = new Pyroclasm();
        gd.playerDecks.get(player1.getId()).addFirst(pyroclasm);

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve Omen Machine trigger — Pyroclasm goes on stack

        // Pyroclasm should be on the stack
        assertThat(gd.stack).anyMatch(se -> se.getCard() == pyroclasm);

        // Resolve Pyroclasm
        harness.passBothPriorities();

        // Grizzly Bears (2/2) should be dead from 2 damage
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        // Pyroclasm goes to graveyard
        harness.assertInGraveyard(player1, "Pyroclasm");

        // No mana was spent
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Targeted instant on top prompts for target and is cast without paying")
    void targetedInstantPrompsForTarget() {
        harness.addToBattlefield(player1, new OmenMachine());
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2

        Card shock = new Shock();
        gd.playerDecks.get(player1.getId()).addFirst(shock);

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve Omen Machine trigger — prompts for target

        // Should be prompting for a target
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Choose Grizzly Bears as target
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        // Shock should be on the stack
        assertThat(gd.stack).anyMatch(se -> se.getCard() == shock);

        // Resolve Shock
        harness.passBothPriorities();

        // Grizzly Bears should be dead
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Creature on top is cast without paying (put on stack as creature spell)")
    void creatureOnTopIsCast() {
        harness.addToBattlefield(player1, new OmenMachine());

        Card bears = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(bears);

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve Omen Machine trigger — creature goes on stack

        // Grizzly Bears should be on the stack
        assertThat(gd.stack).anyMatch(se -> se.getCard() == bears);

        // Resolve creature spell
        harness.passBothPriorities();

        // Grizzly Bears should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == bears);
    }

    @Test
    @DisplayName("Trigger fires on opponent's draw step as well")
    void triggerFiresOnOpponentDrawStep() {
        harness.addToBattlefield(player1, new OmenMachine());

        Card plains = new Plains();
        gd.playerDecks.get(player2.getId()).addFirst(plains);

        advanceToDraw(player2);
        harness.passBothPriorities(); // resolve Omen Machine trigger

        // Plains should be on opponent's battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard() == plains);
    }

    @Test
    @DisplayName("Empty library — trigger does nothing")
    void emptyLibraryDoesNothing() {
        harness.addToBattlefield(player1, new OmenMachine());
        gd.playerDecks.get(player1.getId()).clear();

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve Omen Machine trigger — nothing happens

        // No crash, game continues normally
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Creature-only spell with no legal targets remains in exile")
    void targetedSpellNoValidTargetsStaysInExile() {
        harness.addToBattlefield(player1, new OmenMachine());
        Card dismember = new Dismember();
        harness.setLibrary(player1, List.of(dismember));

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(dismember);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == dismember);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Casting from Omen Machine counts as spell cast")
    void castCountsAsSpellCast() {
        harness.addToBattlefield(player1, new OmenMachine());
        Card pyroclasm = new Pyroclasm();
        gd.playerDecks.get(player1.getId()).addFirst(pyroclasm);

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve trigger — Pyroclasm goes on stack

        assertThat(gd.getSpellsCastThisTurnCount(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Card exiled by Omen Machine never enters the hand")
    void cardNeverEntersHand() {
        harness.addToBattlefield(player1, new OmenMachine());

        Card bears = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(bears);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToDraw(player1);
        harness.passBothPriorities(); // resolve Omen Machine trigger

        // Bears should not be in hand — it was cast from exile directly to stack
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("Graveyard-targeting spell must be cast when a legal target exists")
    void graveyardTargetIsAvailable() {
        harness.addToBattlefield(player1, new OmenMachine());
        harness.setGraveyard(player2, List.of(new Dismember()));
        Card surgicalExtraction = new SurgicalExtraction();
        harness.setLibrary(player1, List.of(surgicalExtraction));

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.getSpellsCastThisTurnCount(player1.getId())).isZero();
    }

    @Test
    @DisplayName("An opponent's hexproof creature is not a legal target")
    void hexproofCreatureDoesNotAllowCasting() {
        harness.addToBattlefield(player1, new OmenMachine());
        harness.addToBattlefield(player2, new TrollAscetic());
        Card dismember = new Dismember();
        harness.setLibrary(player1, List.of(dismember));

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(dismember);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == dismember);
    }

    @Test
    @DisplayName("Rule of Law prevents casting a second spell through Omen Machine")
    void castingLimitLeavesCardInExile() {
        harness.addToBattlefield(player1, new OmenMachine());
        harness.addToBattlefield(player2, new RuleOfLaw());
        Card pyroclasm = new Pyroclasm();
        harness.setLibrary(player1, List.of(pyroclasm));
        harness.setHand(player1, List.of(new Shock()));

        advanceToDraw(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(pyroclasm);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == pyroclasm);
        assertThat(gd.getSpellsCastThisTurnCount(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Cost increases still apply to spells cast without paying their mana cost")
    void unpaidCostIncreaseLeavesCardInExile() {
        harness.addToBattlefield(player1, new OmenMachine());
        harness.addToBattlefield(player2, new AuraOfSilence());
        Card artifact = new OmenMachine();
        harness.setLibrary(player1, List.of(artifact));

        advanceToDraw(player1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(artifact);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == artifact);
        assertThat(gd.getSpellsCastThisTurnCount(player1.getId())).isZero();
    }

    @Test
    @DisplayName("Artillerize requires an artifact or creature sacrifice before it is cast")
    void requiredSacrificeIsPaidBeforeCasting() {
        harness.addToBattlefield(player1, new OmenMachine());
        Card artillerize = new Artillerize();
        harness.setLibrary(player1, List.of(artillerize));

        advanceToDraw(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == artillerize);
        assertThat(gd.getSpellsCastThisTurnCount(player1.getId())).isZero();
    }

    @Test
    @DisplayName("Draws from spells are prevented as well as the draw step draw")
    void spellDrawIsPrevented() {
        harness.addToBattlefield(player1, new OmenMachine());
        Card topCard = new Plains();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new GitaxianProbe()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertInGraveyard(player1, "Gitaxian Probe");
    }

    @Test
    @DisplayName("Each Omen Machine independently processes the next library card")
    void multipleMachinesProcessSuccessiveCards() {
        harness.addToBattlefield(player1, new OmenMachine());
        harness.addToBattlefield(player2, new OmenMachine());
        Card firstLand = new Plains();
        Card secondLand = new Plains();
        harness.setLibrary(player1, List.of(firstLand, secondLand));

        advanceToDraw(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == firstLand)
                .anyMatch(permanent -> permanent.getCard() == secondLand);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(firstLand, secondLand);
    }
}
