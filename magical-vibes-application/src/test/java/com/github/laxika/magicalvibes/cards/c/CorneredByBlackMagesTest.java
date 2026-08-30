package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CorneredByBlackMages.class, GoblinPiker.class, GrizzlyBears.class, Shock.class})
class CorneredByBlackMagesTest extends BaseCardTest {

    @Test
    void opponentChoosesCreatureToSacrificeAndWizardIsCreated() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GoblinPiker());
        harness.setHand(player1, List.of(new CorneredByBlackMages()));
        addCorneredByBlackMagesMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, chosen.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Goblin Piker");
        assertThat(findPermanents(player1, "Wizard")).hasSize(1);
        assertThat(findPermanents(player2, "Wizard")).isEmpty();
    }

    @Test
    void WizardDealsDamageToEachOpponentWhenControllerCastsNoncreatureSpell() {
        harness.setHand(player1, List.of(new CorneredByBlackMages(), new Shock()));
        addCorneredByBlackMagesMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    void WizardDoesNotTriggerForCreatureSpell() {
        harness.setHand(player1, List.of(new CorneredByBlackMages()));
        addCorneredByBlackMagesMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        harness.assertLife(player2, 20);
    }

    private void addCorneredByBlackMagesMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
