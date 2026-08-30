package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VenserShaperSavant.class, GrizzlyBears.class, Shock.class})
class VenserShaperSavantTest extends BaseCardTest {

    @Test
    void returnsTargetPermanentToItsOwnersHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VenserShaperSavant()));
        addVenserMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void returnsTargetSpellToItsOwnersHand() {
        harness.setHand(player1, List.of(new Shock(), new VenserShaperSavant()));
        harness.addMana(player1, ManaColor.RED, 1);
        addVenserMana();

        harness.castInstant(player1, 0, player2.getId());
        UUID shockId = gd.stack.getFirst().getCard().getId();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(shockId);
        harness.handlePermanentChosen(player1, shockId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Venser, Shaper Savant");
        harness.assertInHand(player1, "Shock");
        harness.assertNotInGraveyard(player1, "Shock");
    }

    private void addVenserMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
