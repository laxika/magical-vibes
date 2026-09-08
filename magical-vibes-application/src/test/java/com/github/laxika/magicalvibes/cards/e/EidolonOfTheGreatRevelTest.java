package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.Concentrate;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EidolonOfTheGreatRevelTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to its controller when they cast a spell with mana value 3 or less")
    void damagesControllerCastingLowManaValueSpell() {
        harness.addToBattlefield(player1, new EidolonOfTheGreatRevel());
        harness.setHand(player1, List.of(new Fog()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals 2 damage to the player who casts a spell with mana value 3 or less")
    void damagesOpponentCastingLowManaValueSpell() {
        harness.addToBattlefield(player1, new EidolonOfTheGreatRevel());
        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not trigger for a spell with mana value 4 or greater")
    void doesNotTriggerForHighManaValueSpell() {
        harness.addToBattlefield(player1, new EidolonOfTheGreatRevel());
        harness.setHand(player2, List.of(new Concentrate()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player2);

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Uses the chosen X value when checking a spell's mana value")
    void usesChosenXValueForManaValueCheck() {
        harness.addToBattlefield(player1, new EidolonOfTheGreatRevel());
        harness.setHand(player2, List.of(new Fireball()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.forceActivePlayer(player2);

        harness.castSorcery(player2, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
