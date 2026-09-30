package com.github.laxika.magicalvibes.cards.h;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.laxika.magicalvibes.cards.a.AssaultZeppelid;
import com.github.laxika.magicalvibes.cards.a.AzoriusFirstWing;
import com.github.laxika.magicalvibes.cards.b.BloodCrypt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({HellholeRats.class, AzoriusFirstWing.class, AssaultZeppelid.class, BloodCrypt.class})
class HellholeRatsTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, target player discards a card and takes damage equal to its mana value")
    void discardsAndDealsDiscardedManaValueDamage() {
        harness.setHand(player2, new ArrayList<>(List.of(new AzoriusFirstWing(), new AssaultZeppelid())));
        harness.setHand(player1, List.of(new HellholeRats()));
        addMana(player1);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        harness.assertInGraveyard(player2, "Assault Zeppelid");
    }

    @Test
    @DisplayName("An empty hand results in no damage")
    void emptyHandDealsNoDamage() {
        harness.setHand(player2, new ArrayList<>());
        harness.setHand(player1, List.of(new HellholeRats()));
        addMana(player1);
        int lifeBefore = gd.getLife(player2.getId());

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("It can target its controller, and discarding a land deals no damage")
    void canTargetControllerAndLandDealsNoDamage() {
        harness.setHand(player1, List.of(new HellholeRats(), new BloodCrypt()));
        addMana(player1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castCreature(player1, 0, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        harness.assertInGraveyard(player1, "Blood Crypt");
    }

    @Test
    @DisplayName("The ETB ability cannot target a permanent")
    void cannotTargetPermanent() {
        var permanent = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        harness.setHand(player1, List.of(new HellholeRats()));
        addMana(player1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
