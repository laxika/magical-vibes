package com.github.laxika.magicalvibes.cards.h;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({HellholeRats.class, GrizzlyBears.class, WindDrake.class})
class HellholeRatsTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, target player discards a card and takes damage equal to its mana value")
    void discardsAndDealsDiscardedManaValueDamage() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new WindDrake())));
        harness.setHand(player1, List.of(new HellholeRats()));
        addMana(player1);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        harness.assertInGraveyard(player2, "Wind Drake");
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
    @DisplayName("The ETB ability cannot target a permanent")
    void cannotTargetPermanent() {
        var permanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
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
