package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GatheringStone.class, GrizzlyBears.class, WalkingCorpse.class, NamelessInversion.class})
class GatheringStoneTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a creature type as Gathering Stone enters stores that type")
    void choosesCreatureTypeOnEntry() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new GatheringStone(), "{4}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");

        assertThat(findPermanent(player1, "Gathering Stone").getChosenSubtype())
                .isEqualTo(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("The entering ability can reveal a matching top card to hand")
    void matchingTopCardOnEntryGoesToHand() {
        GrizzlyBears bear = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bear));
        harness.castFromHand(player1, new GatheringStone(), "{4}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(bear);
    }

    @Test
    @DisplayName("Spells of the chosen type cost {1} less to cast")
    void reducesChosenTypeSpellCost() {
        gatheringStone(CardSubtype.BEAR);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Spells without the chosen type are not reduced")
    void doesNotReduceOtherTypeSpellCost() {
        gatheringStone(CardSubtype.BEAR);
        harness.setHand(player1, List.of(new WalkingCorpse()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The upkeep ability may reveal a matching top card to hand")
    void matchingTopCardGoesToHand() {
        gatheringStone(CardSubtype.BEAR);
        GrizzlyBears bear = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bear));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(bear);
    }

    @Test
    @DisplayName("A nonmatching top card stays on top when the graveyard option is declined")
    void nonmatchingTopCardStaysOnTopWhenDeclined() {
        gatheringStone(CardSubtype.BEAR);
        WalkingCorpse corpse = new WalkingCorpse();
        harness.setLibrary(player1, List.of(corpse));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(corpse);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(corpse);
    }

    @Test
    void enteringTriggerCanPutNonmatchingCardIntoGraveyard() {
        WalkingCorpse corpse = new WalkingCorpse();
        harness.setLibrary(player1, List.of(corpse));
        harness.castFromHand(player1, new GatheringStone(), "{4}");

        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(corpse);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(corpse);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(corpse);
    }

    @Test
    void nonmatchingTopCardCanGoToGraveyard() {
        gatheringStone(CardSubtype.BEAR);
        WalkingCorpse corpse = new WalkingCorpse();
        harness.setLibrary(player1, List.of(corpse));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(corpse);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(corpse);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(corpse);
    }

    @Test
    void declinedMatchingCardCanGoToGraveyard() {
        gatheringStone(CardSubtype.BEAR);
        GrizzlyBears bear = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bear));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bear);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(bear);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bear);
    }

    @Test
    void matchingCardCanStayOnTopWhenBothOptionsAreDeclined() {
        gatheringStone(CardSubtype.BEAR);
        GrizzlyBears bear = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bear));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bear);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bear);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bear);
    }

    @Test
    void matchingKindredCardCanBeRevealedToHand() {
        gatheringStone(CardSubtype.BEAR);
        NamelessInversion inversion = new NamelessInversion();
        harness.setLibrary(player1, List.of(inversion));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(inversion);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(inversion);
    }

    @Test
    void reducesMatchingKindredSpellCost() {
        gatheringStone(CardSubtype.BEAR);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new NamelessInversion()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Nameless Inversion"));
    }

    @Test
    void upkeepTriggerRemembersChosenTypeAfterStoneLeaves() {
        Permanent stone = gatheringStone(CardSubtype.BEAR);
        GrizzlyBears bear = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bear));

        advanceToUpkeep(player1);
        assertThat(gd.stack).isNotEmpty();
        gd.playerBattlefields.get(player1.getId()).remove(stone);
        gd.playerGraveyards.get(player1.getId()).add(stone.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(bear);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bear);
    }

    @Test
    void emptyLibraryCreatesNoChoice() {
        gatheringStone(CardSubtype.BEAR);
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsUpkeepDoesNotTriggerStone() {
        gatheringStone(CardSubtype.BEAR);
        GrizzlyBears bear = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bear));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bear);
    }

    @Test
    void doesNotReduceOpponentsSpells() {
        Permanent stone = harness.addToBattlefieldAndReturn(player2, new GatheringStone());
        stone.setChosenSubtype(CardSubtype.BEAR);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reductionDoesNotPayColoredMana() {
        gatheringStone(CardSubtype.BEAR);
        gatheringStone(CardSubtype.BEAR);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent gatheringStone(CardSubtype chosenSubtype) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new GatheringStone());
        permanent.setChosenSubtype(chosenSubtype);
        return permanent;
    }
}
