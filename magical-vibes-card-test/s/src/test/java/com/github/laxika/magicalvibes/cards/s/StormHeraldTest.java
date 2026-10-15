package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AbundantGrowth;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.f.FlickerOfFate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
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

@CardUsed({StormHerald.class, AbundantGrowth.class, Boomerang.class, GrizzlyBears.class,
        HolyStrength.class, Pacifism.class, FlickerOfFate.class, SpectraWard.class})
class StormHeraldTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns selected Auras attached to creatures I control")
    void returnsSelectedAurasAttachedToControlledCreatures() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        HolyStrength holyStrength = new HolyStrength();
        Pacifism pacifism = new Pacifism();

        castStormHerald(List.of(holyStrength, pacifism));

        PendingInteraction.ReturnAurasFromGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ReturnAurasFromGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(holyStrength.getId(), pacifism.getId());

        harness.handleMultipleCardsChosen(player1, List.of(holyStrength.getId(), pacifism.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();

        harness.handlePermanentChosen(player1, bears.getId());
        Permanent herald = findPermanent(player1, "Storm Herald");
        harness.handlePermanentChosen(player1, herald.getId());

        assertThat(findPermanent(player1, "Holy Strength").getAttachedTo()).isEqualTo(bears.getId());
        assertThat(findPermanent(player1, "Pacifism").getAttachedTo()).isEqualTo(herald.getId());
        harness.assertNotInGraveyard(player1, "Holy Strength");
        harness.assertNotInGraveyard(player1, "Pacifism");
    }

    @Test
    @DisplayName("Only Auras that can enchant a controlled creature are offered")
    void offersOnlyAurasWithAControlledCreatureTarget() {
        AbundantGrowth abundantGrowth = new AbundantGrowth();
        HolyStrength holyStrength = new HolyStrength();

        castStormHerald(List.of(abundantGrowth, holyStrength));

        PendingInteraction.ReturnAurasFromGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ReturnAurasFromGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(holyStrength.getId());

        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertInGraveyard(player1, "Abundant Growth");
        harness.assertInGraveyard(player1, "Holy Strength");
        harness.assertNotOnBattlefield(player1, "Abundant Growth");
        harness.assertNotOnBattlefield(player1, "Holy Strength");
    }

    @Test
    @DisplayName("Returned Auras are exiled at the next end step")
    void returnedAurasAreExiledAtNextEndStep() {
        HolyStrength holyStrength = new HolyStrength();
        Pacifism pacifism = new Pacifism();

        castStormHerald(List.of(holyStrength, pacifism));
        harness.handleMultipleCardsChosen(player1, List.of(holyStrength.getId()));

        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(holyStrength.getId()));
        harness.assertInGraveyard(player1, "Pacifism");
    }

    @Test
    @DisplayName("Returned Auras are exiled instead of being bounced")
    void returnedAurasAreExiledInsteadOfBounced() {
        HolyStrength holyStrength = new HolyStrength();
        castStormHerald(List.of(holyStrength));
        harness.handleMultipleCardsChosen(player1, List.of(holyStrength.getId()));

        Permanent aura = findPermanent(player1, "Holy Strength");
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, aura.getId());

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(holyStrength.getId()));
        harness.assertNotOnBattlefield(player1, "Holy Strength");
        harness.assertNotInHand(player1, "Holy Strength");
        harness.assertNotInGraveyard(player1, "Holy Strength");
    }

    @Test
    @DisplayName("Auras returned during an opponent's turn wait for my next end step")
    void aurasReturnedOnOpponentsTurnWaitForMyEndStep() {
        castStormHerald(List.of());
        HolyStrength holyStrength = new HolyStrength();
        harness.setGraveyard(player1, List.of(holyStrength));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new FlickerOfFate()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, findPermanent(player1, "Storm Herald").getId());
        resolveAllTriggers();
        harness.handleMultipleCardsChosen(player1, List.of(holyStrength.getId()));

        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Holy Strength");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getId().equals(holyStrength.getId()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Holy Strength");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(holyStrength.getId()));
    }

    @Test
    @DisplayName("Selected Auras enter together before their protection effects apply")
    void selectedAurasEnterSimultaneously() {
        SpectraWard spectraWard = new SpectraWard();
        HolyStrength holyStrength = new HolyStrength();

        castStormHerald(List.of(spectraWard, holyStrength));
        harness.handleMultipleCardsChosen(player1, List.of(spectraWard.getId(), holyStrength.getId()));

        Permanent herald = findPermanent(player1, "Storm Herald");
        assertThat(findPermanent(player1, "Spectra Ward").getAttachedTo()).isEqualTo(herald.getId());
        assertThat(findPermanent(player1, "Holy Strength").getAttachedTo()).isEqualTo(herald.getId());
        harness.assertNotInGraveyard(player1, "Holy Strength");
    }

    @Test
    @DisplayName("One delayed trigger exiles all Auras returned by the same ability")
    void oneDelayedTriggerExilesAllReturnedAuras() {
        HolyStrength holyStrength = new HolyStrength();
        Pacifism pacifism = new Pacifism();
        castStormHerald(List.of(holyStrength, pacifism));
        harness.handleMultipleCardsChosen(player1, List.of(holyStrength.getId(), pacifism.getId()));

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Holy Strength");
        harness.assertNotOnBattlefield(player1, "Pacifism");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(holyStrength.getId()))
                .anyMatch(card -> card.getId().equals(pacifism.getId()));
    }

    @Test
    @DisplayName("A returned Aura is exiled when its enchanted creature leaves")
    void returnedAuraIsExiledWhenItsCreatureLeaves() {
        HolyStrength holyStrength = new HolyStrength();
        castStormHerald(List.of(holyStrength));
        harness.handleMultipleCardsChosen(player1, List.of(holyStrength.getId()));
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, findPermanent(player1, "Storm Herald").getId());

        harness.assertInHand(player1, "Storm Herald");
        harness.assertNotOnBattlefield(player1, "Holy Strength");
        harness.assertNotInGraveyard(player1, "Holy Strength");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(holyStrength.getId()));
    }

    private void castStormHerald(List<com.github.laxika.magicalvibes.model.Card> graveyard) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new StormHerald()));
        harness.setGraveyard(player1, graveyard);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
