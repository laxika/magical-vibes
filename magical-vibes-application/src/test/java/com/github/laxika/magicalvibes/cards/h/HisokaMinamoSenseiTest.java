package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HisokaMinamoSenseiTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the target spell when the discarded card has the same mana value")
    void countersSpellWithMatchingManaValue() {
        harness.addToBattlefield(player1, new HisokaMinamoSensei());
        harness.setHand(player1, List.of(new Shock())); // MV 1, same as the targeted Shock
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.activateAbility(player1, 0, null, shock.getId(), Zone.STACK);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertInGraveyard(player1, "Shock");
        harness.assertLife(player1, lifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Spell resolves when the discarded card has a different mana value")
    void doesNotCounterOnManaValueMismatch() {
        harness.addToBattlefield(player1, new HisokaMinamoSensei());
        harness.setHand(player1, List.of(new GrizzlyBears())); // MV 2, Shock is MV 1
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.activateAbility(player1, 0, null, shock.getId(), Zone.STACK);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Shock");
        harness.assertLife(player1, lifeBefore - 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate with an empty hand")
    void cannotActivateWithoutACardToDiscard() {
        harness.addToBattlefield(player1, new HisokaMinamoSensei());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        UUID shockId = shock.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, shockId, Zone.STACK))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player1, new HisokaMinamoSensei());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId, Zone.STACK))
                .isInstanceOf(IllegalStateException.class);
    }
}
