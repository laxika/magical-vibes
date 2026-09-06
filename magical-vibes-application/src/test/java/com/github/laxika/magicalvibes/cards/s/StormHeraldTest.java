package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AbundantGrowth;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
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
        HolyStrength.class, Pacifism.class})
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

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
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
        harness.castInstant(player1, 0, aura.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(holyStrength.getId()));
        harness.assertNotOnBattlefield(player1, "Holy Strength");
        harness.assertNotInHand(player1, "Holy Strength");
        harness.assertNotInGraveyard(player1, "Holy Strength");
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
