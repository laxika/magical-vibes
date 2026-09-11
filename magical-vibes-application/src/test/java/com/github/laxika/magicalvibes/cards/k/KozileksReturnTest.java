package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KozileksReturn.class, com.github.laxika.magicalvibes.cards.d.DeceiverOfForm.class, GrizzlyBears.class, SerraAngel.class})
class KozileksReturnTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to each creature without damaging players")
    void dealsTwoDamageToEachCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new SerraAngel());

        harness.setHand(player1, List.of(new KozileksReturn()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Serra Angel");
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("May exile from the graveyard for 5 damage when a large Eldrazi is cast")
    void mayExileFromGraveyardForFiveDamage() {
        KozileksReturn returnCard = new KozileksReturn();
        harness.setGraveyard(player1, List.of(returnCard));
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.d.DeceiverOfForm()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Kozilek's Return");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Kozilek's Return"));
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Deceiver of Form");
    }

    @Test
    @DisplayName("Declining the graveyard trigger leaves the card in the graveyard")
    void decliningGraveyardTriggerLeavesCard() {
        KozileksReturn returnCard = new KozileksReturn();
        harness.setGraveyard(player1, List.of(returnCard));
        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.d.DeceiverOfForm()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Kozilek's Return");
    }
}
