package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DuneDrifter.class, AvatarOfMight.class, GrizzlyBears.class, Spellbook.class, Plains.class})
class DuneDrifterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a target artifact or creature with mana value X or less")
    void etbReturnsEligibleArtifactOrCreature() {
        Card artifact = new Spellbook();
        Card creature = new GrizzlyBears();
        Card tooExpensive = new AvatarOfMight();
        harness.setGraveyard(player1, List.of(artifact, creature, tooExpensive));
        harness.setHand(player1, List.of(new DuneDrifter()));
        addDuneDrifterMana(2);

        harness.castAndResolveSorcery(player1, 0, 2);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(artifact.getId(), creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Avatar of Might");
    }

    @Test
    @DisplayName("Crew 2 turns Dune Drifter into a creature until end of turn")
    void crewsDuneDrifter() {
        Permanent drifter = harness.addToBattlefieldAndReturn(player1, new DuneDrifter());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        crew.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, drifter)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("ETB does not target a card with mana value greater than X")
    void etbDoesNotTargetTooExpensiveCard() {
        Card tooExpensive = new AvatarOfMight();
        harness.setGraveyard(player1, List.of(tooExpensive));
        harness.setHand(player1, List.of(new DuneDrifter()));
        addDuneDrifterMana(2);

        harness.castAndResolveSorcery(player1, 0, 2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Avatar of Might");
    }

    @Test
    @DisplayName("ETB returns a creature at the X boundary untapped")
    void returnsCreatureAtManaValueBoundary() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DuneDrifter()));
        addDuneDrifterMana(2);

        harness.castAndResolveSorcery(player1, 0, 2);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        Permanent returned = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Grizzly Bears"));
        assertThat(returned.isTapped()).isFalse();
    }

    @Test
    @DisplayName("X zero targets only zero-mana artifacts or creatures in your graveyard")
    void zeroXRestrictsTypeManaValueAndGraveyard() {
        Card artifact = new Spellbook();
        Card creature = new GrizzlyBears();
        Card land = new Plains();
        Card opposingArtifact = new Spellbook();
        harness.setGraveyard(player1, List.of(artifact, creature, land));
        harness.setGraveyard(player2, List.of(opposingArtifact));
        harness.setHand(player1, List.of(new DuneDrifter()));
        addDuneDrifterMana(0);

        harness.castAndResolveSorcery(player1, 0, 0);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(artifact.getId());
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Plains");
        harness.assertInGraveyard(player2, "Spellbook");
    }

    @Test
    @DisplayName("Entering without being cast uses X zero")
    void enteringWithoutCastingUsesZeroX() {
        Card artifact = new Spellbook();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(artifact, creature));

        harness.enterBattlefieldAndReturn(player1, new DuneDrifter());
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(artifact.getId());
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB does not choose a replacement when its target leaves the graveyard")
    void missingTargetDoesNotReturnAnotherCard() {
        Card target = new GrizzlyBears();
        Card other = new Spellbook();
        harness.setGraveyard(player1, List.of(target, other));
        harness.setHand(player1, List.of(new DuneDrifter()));
        addDuneDrifterMana(2);

        harness.castAndResolveSorcery(player1, 0, 2);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(other));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("Summoning-sick creatures can crew and animation expires at end of turn")
    void summoningSickCrewAndAnimationDuration() {
        Permanent drifter = harness.addToBattlefieldAndReturn(player1, new DuneDrifter());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        crew.setSummoningSick(true);
        assertThat(gqs.isCreature(gd, drifter)).isFalse();

        harness.activateAbility(player1, 0, null, null);
        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, drifter)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, drifter)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.isCreature(gd, drifter)).isFalse();
    }

    private void addDuneDrifterMana(int xValue) {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
    }
}
