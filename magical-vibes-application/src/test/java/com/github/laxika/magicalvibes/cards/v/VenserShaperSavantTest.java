package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Foresee;
import com.github.laxika.magicalvibes.cards.h.HorizonCanopy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VenserShaperSavant.class, HorizonCanopy.class, Foresee.class})
class VenserShaperSavantTest extends BaseCardTest {

    @Test
    void returnsTargetPermanentToItsOwnersHand() {
        HorizonCanopy targetCard = new HorizonCanopy();
        targetCard.setOwnerId(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);
        harness.setHand(player1, List.of(new VenserShaperSavant()));
        addVenserMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Horizon Canopy");
        harness.assertNotInHand(player2, "Horizon Canopy");
        harness.assertNotOnBattlefield(player2, "Horizon Canopy");
    }

    @Test
    void returnsTargetSpellToItsOwnersHand() {
        Foresee targetSpell = new Foresee();
        harness.setHand(player2, List.of(targetSpell));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new VenserShaperSavant()));
        addVenserMana();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castSorcery(player2, 0);
        UUID targetSpellId = targetSpell.getId();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(targetSpellId);
        harness.handlePermanentChosen(player1, targetSpellId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Venser, Shaper Savant");
        harness.assertInHand(player2, "Foresee");
        harness.assertNotInGraveyard(player2, "Foresee");
    }

    private void addVenserMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
