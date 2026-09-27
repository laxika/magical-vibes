package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NetherHorror;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AncientCellarspawn.class, NetherHorror.class, GrizzlyBears.class})
class AncientCellarspawnTest extends BaseCardTest {

    @Test
    @DisplayName("Demon, Horror, and Nightmare spells cost {1} less")
    void matchingCreatureSpellCostsOneLess() {
        harness.addToBattlefield(player1, new AncientCellarspawn());
        harness.setHand(player1, List.of(new NetherHorror()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Nether Horror"));
    }

    @Test
    @DisplayName("A discounted spell makes a target opponent lose the mana-value difference")
    void discountedSpellLosesDifference() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new AncientCellarspawn());
        harness.setHand(player1, List.of(new NetherHorror()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The trigger does not fire when the spell was not discounted")
    void fullCostSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new AncientCellarspawn());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The cast trigger cannot target its controller")
    void triggerCannotTargetController() {
        harness.addToBattlefield(player1, new AncientCellarspawn());
        harness.setHand(player1, List.of(new NetherHorror()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
