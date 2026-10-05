package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({Miscast.class, Shock.class, Divination.class, GrizzlyBears.class})
class MiscastTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an instant spell when its controller cannot pay {3}")
    void countersInstantWhenControllerCannotPay() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.setHand(player2, List.of(new Miscast()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, shock.getId());

        harness.assertInGraveyard(player1, "Shock");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("A sorcery spell resolves when its controller pays {3}")
    void sorceryResolvesWhenControllerPays() {
        Divination divination = new Divination();
        harness.setHand(player1, List.of(divination));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.setHand(player2, List.of(new Miscast()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, divination.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Divination");
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Miscast()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters an instant when its controller declines an affordable payment")
    void countersInstantWhenControllerDeclinesPayment() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setHand(player2, List.of(new Miscast()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, shock.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player2, "Miscast");
        harness.assertLife(player2, 20);
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Two available mana cannot satisfy the three-mana payment")
    void countersInstantWhenControllerHasOnlyTwoMana() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new Miscast()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, shock.getId());

        harness.assertInGraveyard(player1, "Shock");
        harness.assertLife(player2, 20);
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(harness.getGameData().playerManaPools.get(player2.getId()).getTotal()).isEqualTo(3);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("An instant resolves after its controller pays three mana of different colors")
    void instantResolvesWhenControllerPaysMixedColors() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setHand(player2, List.of(new Miscast()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, shock.getId());
        harness.handleMayAbilityChosen(player1, true);
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player2, "Miscast");
        assertThat(harness.getGameData().stack).isEmpty();
    }
}
