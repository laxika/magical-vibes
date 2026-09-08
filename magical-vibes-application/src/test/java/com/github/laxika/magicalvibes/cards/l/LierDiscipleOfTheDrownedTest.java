package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LierDiscipleOfTheDrowned.class, Counterspell.class, GrizzlyBears.class, Shock.class})
class LierDiscipleOfTheDrownedTest extends BaseCardTest {

    @Test
    @DisplayName("Spells cannot be countered while Lier is on the battlefield")
    void spellsCannotBeCountered() {
        harness.addToBattlefield(player1, new LierDiscipleOfTheDrowned());
        GrizzlyBears bears = new GrizzlyBears();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(bears));
        harness.setHand(player1, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Counterspell");
    }

    @Test
    @DisplayName("Instant and sorcery cards in your graveyard have flashback")
    void grantsFlashbackToInstantAndSorceryCards() {
        harness.addToBattlefield(player1, new LierDiscipleOfTheDrowned());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Shock shock = new Shock();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setGraveyard(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFlashback(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Shock"));
    }

    @Test
    @DisplayName("Lier does not grant flashback to creature cards")
    void doesNotGrantFlashbackToCreatureCards() {
        harness.addToBattlefield(player1, new LierDiscipleOfTheDrowned());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
