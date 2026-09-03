package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoidRend.class, GrizzlyBears.class, Forest.class, Cancel.class})
class VoidRendTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target nonland permanent")
    void destroysTargetNonlandPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        castVoidRend(targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Void Rend");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        UUID targetId = harness.getPermanentId(player2, "Forest");

        harness.setHand(player1, List.of(new VoidRend()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    @DisplayName("Cannot be countered")
    void cannotBeCountered() {
        GrizzlyBears bears = new GrizzlyBears();
        VoidRend voidRend = new VoidRend();
        harness.addToBattlefield(player2, bears);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(voidRend));
        addMana();
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, voidRend.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Void Rend");
        harness.assertInGraveyard(player2, "Cancel");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private void castVoidRend(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new VoidRend()));
        addMana();
        harness.castInstant(player1, 0, targetId);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
