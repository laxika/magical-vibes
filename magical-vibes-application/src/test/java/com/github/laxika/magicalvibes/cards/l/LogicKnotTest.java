package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LogicKnot.class, BlindPhantasm.class})
class LogicKnotTest extends BaseCardTest {

    @Test
    @DisplayName("Delve exiles a graveyard card and Logic Knot counters a spell when its controller cannot pay X")
    void delvesAndCountersWhenTargetControllerCannotPayX() {
        BlindPhantasm phantasm = new BlindPhantasm();
        BlindPhantasm graveyardCard = new BlindPhantasm();
        harness.setHand(player1, List.of(phantasm));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.setHand(player2, List.of(new LogicKnot()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        gs.playCard(gd, player2, 0, 1, phantasm.getId(), null,
                List.of(), List.of(), false, null, null, null, null, List.of(0));

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(graveyardCard);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Blind Phantasm");
        harness.assertNotOnBattlefield(player1, "Blind Phantasm");
    }

    @Test
    @DisplayName("Declining to pay the announced X counters the target spell")
    void decliningXCountersTargetSpell() {
        BlindPhantasm phantasm = new BlindPhantasm();
        harness.setHand(player1, List.of(phantasm));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.setHand(player2, List.of(new LogicKnot()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, phantasm.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Blind Phantasm");
        harness.assertNotOnBattlefield(player1, "Blind Phantasm");
    }

    @Test
    @DisplayName("Paying the announced X keeps the target spell on the stack")
    void payingXKeepsTargetSpell() {
        BlindPhantasm phantasm = new BlindPhantasm();
        harness.setHand(player1, List.of(phantasm));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.setHand(player2, List.of(new LogicKnot()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 1, phantasm.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertNotInGraveyard(player1, "Blind Phantasm");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Blind Phantasm");
    }
}
