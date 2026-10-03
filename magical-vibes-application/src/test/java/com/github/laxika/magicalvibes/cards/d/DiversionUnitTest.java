package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiversionUnit.class, Shock.class, GrizzlyBears.class, LavaAxe.class})
class DiversionUnitTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an instant when its controller cannot pay {3}")
    void countersInstantWhenControllerCannotPay() {
        harness.addToBattlefield(player1, new DiversionUnit());
        harness.addMana(player1, ManaColor.BLUE, 1);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, shock.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Diversion Unit");
    }

    @Test
    @DisplayName("Instant resolves when its controller pays {3}")
    void instantResolvesWhenControllerPays() {
        harness.addToBattlefield(player1, new DiversionUnit());
        harness.addMana(player1, ManaColor.BLUE, 1);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, shock.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertInGraveyard(player2, "Shock");
        harness.assertInGraveyard(player1, "Diversion Unit");
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        harness.addToBattlefield(player1, new DiversionUnit());
        harness.addMana(player1, ManaColor.BLUE, 1);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifice is paid immediately and the ability counters a sorcery")
    void countersSorceryAfterSacrificingAsCost() {
        harness.addToBattlefield(player1, new DiversionUnit());
        harness.addMana(player1, ManaColor.BLUE, 1);
        LavaAxe axe = new LavaAxe();
        harness.setHand(player2, List.of(axe));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, axe.getId());

        harness.assertNotOnBattlefield(player1, "Diversion Unit");
        harness.assertInGraveyard(player1, "Diversion Unit");
        harness.assertNotInGraveyard(player2, "Lava Axe");

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Lava Axe");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Counters an instant when its controller declines an affordable payment")
    void countersInstantWhenControllerDeclinesPayment() {
        harness.addToBattlefield(player1, new DiversionUnit());
        harness.addMana(player1, ManaColor.BLUE, 1);
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, shock.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Shock");
        harness.assertInGraveyard(player1, "Diversion Unit");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Two remaining mana is insufficient to prevent the counter")
    void countersInstantWhenControllerHasOnlyTwoMana() {
        harness.addToBattlefield(player1, new DiversionUnit());
        harness.addMana(player1, ManaColor.BLUE, 1);
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, shock.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertLife(player1, 20);
    }
}
