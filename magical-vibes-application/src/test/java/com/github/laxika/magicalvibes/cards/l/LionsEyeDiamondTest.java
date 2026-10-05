package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.m.MtendaLion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LionsEyeDiamond.class, MtendaLion.class})
class LionsEyeDiamondTest extends BaseCardTest {

    @Test
    @DisplayName("Activating discards the hand, sacrifices itself, and adds three mana of the chosen color")
    void activateDiscardsHandSacrificesAndAddsThreeMana() {
        harness.addToBattlefield(player1, new LionsEyeDiamond());
        harness.setHand(player1, List.of(new MtendaLion(), new MtendaLion()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Activating with an empty hand still produces three mana")
    void activateWithEmptyHandStillProducesMana() {
        harness.addToBattlefield(player1, new LionsEyeDiamond());
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The mana ability resolves immediately while a creature spell remains on the stack")
    void activatesInResponseWithoutUsingTheStack() {
        harness.addToBattlefield(player1, new LionsEyeDiamond());
        harness.setHand(player1, List.of(new MtendaLion(), new MtendaLion()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        var spell = gd.stack.getFirst();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.stack).containsExactly(spell);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Lion's Eye Diamond");
        harness.assertInGraveyard(player1, "Mtenda Lion");
    }

    @Test
    @DisplayName("Cannot activate while Mtenda Lion's trigger is resolving and asking for mana")
    void cannotActivateDuringResolvingManaPayment() {
        addCreatureReady(player1, new MtendaLion());
        harness.addToBattlefield(player2, new LionsEyeDiamond());
        harness.setHand(player2, List.of(new MtendaLion()));
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        var payment = gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(payment.playerId()).isEqualTo(player2.getId());
        assertThat(payment.manaCost()).isEqualTo("{U}");

        assertThatThrownBy(() -> gs.activateAbility(gd, player2, 0, 0, null, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Lion's Eye Diamond");
        harness.assertInHand(player2, "Mtenda Lion");
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
        harness.handleMayAbilityChosen(player2, false);
    }
}
