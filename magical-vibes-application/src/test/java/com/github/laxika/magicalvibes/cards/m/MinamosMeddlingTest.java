package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DesperateRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MinamosMeddlingTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the target spell and discards hand cards sharing a spliced card's name")
    void countersAndDiscardsSplicedNames() {
        Card arcaneShock = new Shock().createRuntimeCopy();
        arcaneShock.setSubtypes(List.of(CardSubtype.ARCANE));
        DesperateRitual splicedRitual = new DesperateRitual();
        DesperateRitual secondRitual = new DesperateRitual();
        harness.setHand(player1, List.of(arcaneShock, splicedRitual, secondRitual));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.setHand(player2, List.of(new MinamosMeddling()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castWithSplice(player1, 0, player2.getId(), List.of(1));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, arcaneShock.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Desperate Ritual"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Counters a spell with nothing spliced onto it and leaves the hand alone")
    void countersWithoutSpliceLeavesHandAlone() {
        GrizzlyBears bears = new GrizzlyBears();
        DesperateRitual ritual = new DesperateRitual();
        harness.setHand(player1, List.of(bears, ritual));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new MinamosMeddling()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ritual);
    }

    @Test
    @DisplayName("Discards only cards matching a spliced name, not other cards in hand")
    void leavesNonMatchingCardsInHand() {
        Card arcaneShock = new Shock().createRuntimeCopy();
        arcaneShock.setSubtypes(List.of(CardSubtype.ARCANE));
        DesperateRitual splicedRitual = new DesperateRitual();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(arcaneShock, splicedRitual, bears));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.setHand(player2, List.of(new MinamosMeddling()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castWithSplice(player1, 0, player2.getId(), List.of(1));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, arcaneShock.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        harness.assertInGraveyard(player1, "Desperate Ritual");
    }
}
