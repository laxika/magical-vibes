package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AncientKavu;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NomadicElf;
import com.github.laxika.magicalvibes.cards.s.SterlingGrove;
import com.github.laxika.magicalvibes.cards.t.TsabosWeb;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Void.class, AncientKavu.class, Forest.class, NomadicElf.class, SterlingGrove.class, TsabosWeb.class})
class VoidTest extends BaseCardTest {

    @Test
    @DisplayName("Chooses a mana value, destroys matching artifacts and creatures, and discards matching nonlands")
    void resolvesChosenManaValue() {
        harness.addToBattlefield(player1, new NomadicElf());
        harness.addToBattlefield(player2, new NomadicElf());
        harness.addToBattlefield(player2, new TsabosWeb());
        harness.addToBattlefield(player2, new AncientKavu());
        harness.addToBattlefield(player2, new SterlingGrove());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player2, List.of(
                new NomadicElf(), new TsabosWeb(), new SterlingGrove(), new AncientKavu(), new Forest()));

        castVoid(player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "2");

        harness.assertNotOnBattlefield(player1, "Nomadic Elf");
        harness.assertNotOnBattlefield(player2, "Nomadic Elf");
        harness.assertNotOnBattlefield(player2, "Tsabo's Web");
        harness.assertOnBattlefield(player2, "Ancient Kavu");
        harness.assertOnBattlefield(player2, "Sterling Grove");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Ancient Kavu", "Forest");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .contains("Nomadic Elf", "Tsabo's Web", "Sterling Grove");
    }

    @Test
    @DisplayName("Discards matching cards only from the targeted player's hand")
    void discardsOnlyFromTargetPlayersHand() {
        harness.setHand(player1, List.of(new Void(), new NomadicElf()));
        harness.setHand(player2, List.of(new NomadicElf(), new Forest()));
        addVoidMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "2");

        harness.assertInHand(player1, "Nomadic Elf");
        harness.assertNotInHand(player2, "Nomadic Elf");
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("Rejects a non-player target")
    void rejectsNonPlayerTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NomadicElf());
        harness.setHand(player1, List.of(new Void()));
        addVoidMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castVoid(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new Void()));
        addVoidMana();
        harness.castSorcery(player1, 0, targetPlayerId);
        harness.passBothPriorities();
    }

    private void addVoidMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
