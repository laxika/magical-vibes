package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CullingDais;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SavraQueenOfTheGolgari.class, CullingDais.class, ScatheZombies.class, GrizzlyBears.class})
class SavraQueenOfTheGolgariTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a black creature and paying 2 life makes each opponent sacrifice a creature")
    void blackCreatureSacrificeTriggersOpponentSacrifice() {
        harness.setLife(player1, 20);
        Permanent opponentCreature = addCreature(player2, new GrizzlyBears());
        addSavraAndDais(player1, new ScatheZombies());

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Scathe Zombies");
        harness.assertNotOnBattlefield(player2, opponentCreature.getCard().getName());
        harness.assertInGraveyard(player2, opponentCreature.getCard().getName());
    }

    @Test
    @DisplayName("Declining Savra's black-creature payment leaves opponents' creatures alone")
    void decliningBlackCreaturePaymentDoesNothing() {
        harness.setLife(player1, 20);
        Permanent opponentCreature = addCreature(player2, new GrizzlyBears());
        addSavraAndDais(player1, new ScatheZombies());

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        harness.assertOnBattlefield(player2, opponentCreature.getCard().getName());
    }

    @Test
    @DisplayName("Sacrificing a green creature and accepting gains 2 life")
    void greenCreatureSacrificeGainsLife() {
        harness.setLife(player1, 20);
        addSavraAndDais(player1, new GrizzlyBears());

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void addSavraAndDais(Player player, Card sacrificedCard) {
        harness.addToBattlefield(player, new SavraQueenOfTheGolgari());
        harness.addToBattlefield(player, new CullingDais());
        harness.addToBattlefield(player, sacrificedCard);
    }

    private Permanent addCreature(Player player, Card card) {
        return harness.addToBattlefieldAndReturn(player, card);
    }
}
