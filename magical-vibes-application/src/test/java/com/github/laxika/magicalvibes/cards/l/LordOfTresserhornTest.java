package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LordOfTresserhornTest extends BaseCardTest {

    private void castLord() {
        harness.setHand(player1, new ArrayList<>(List.of(new LordOfTresserhorn())));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.getGameService().playCard(gd, player1, 0, 0, player2.getId(), null);
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger
        harness.passBothPriorities(); // resolve ETB trigger
    }

    @Test
    @DisplayName("ETB loses 2 life, sacrifices two creatures and makes target opponent draw two")
    void etbFullEffect() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setLibrary(player2, new ArrayList<>(List.of(new GrizzlyBears(), new HillGiant())));
        harness.setHand(player2, new ArrayList<>());

        castLord();

        harness.assertLife(player1, 18);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent giant = findPermanent(player1, "Hill Giant");
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId(), giant.getId()));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player1, "Lord of Tresserhorn");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("With no other creatures the Lord sacrifices itself")
    void sacrificesItselfWhenAlone() {
        harness.setLibrary(player2, new ArrayList<>(List.of(new GrizzlyBears(), new HillGiant())));
        harness.setHand(player2, new ArrayList<>());

        castLord();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Lord of Tresserhorn");
        harness.assertInGraveyard(player1, "Lord of Tresserhorn");
        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("{B} grants a regeneration shield")
    void regenerationAbility() {
        harness.addToBattlefield(player1, new LordOfTresserhorn());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent lord = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(lord.getRegenerationShield()).isEqualTo(1);
    }
}
