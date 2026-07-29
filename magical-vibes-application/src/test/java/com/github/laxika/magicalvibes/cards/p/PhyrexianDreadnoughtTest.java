package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhyrexianDreadnoughtTest extends BaseCardTest {

    private void castDreadnought() {
        harness.setHand(player1, List.of(new PhyrexianDreadnought()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB
    }

    @Test
    @DisplayName("Auto-sacrifices when the controller's other creatures cannot reach total power 12")
    void autoSacrificesWithoutEnoughPower() {
        harness.addToBattlefield(player1, new AvatarOfMight());
        harness.addToBattlefield(player1, new GrizzlyBears());
        castDreadnought();

        // 8 + 2 = 10 power available, so there is nothing to choose.
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Phyrexian Dreadnought");
        harness.assertOnBattlefield(player1, "Avatar of Might");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sacrificing creatures with total power 12 keeps Phyrexian Dreadnought")
    void sacrificingEnoughPowerKeepsDreadnought() {
        harness.addToBattlefield(player1, new AvatarOfMight());
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player1, new GrizzlyBears());
        castDreadnought();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(
                harness.getPermanentId(player1, "Avatar of Might"),
                harness.getPermanentId(player1, "Air Elemental")));

        harness.assertOnBattlefield(player1, "Phyrexian Dreadnought");
        harness.assertInGraveyard(player1, "Avatar of Might");
        harness.assertInGraveyard(player1, "Air Elemental");
        // Bears were not chosen, so they survive.
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Choosing no creatures sacrifices Phyrexian Dreadnought and nothing else")
    void decliningSacrificesDreadnought() {
        harness.addToBattlefield(player1, new AvatarOfMight());
        harness.addToBattlefield(player1, new AvatarOfMight());
        castDreadnought();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        harness.assertInGraveyard(player1, "Phyrexian Dreadnought");
        assertThat(countPermanents(player1, "Avatar of Might")).isEqualTo(2);
    }

    @Test
    @DisplayName("A selection below total power 12 is rejected and the prompt stands")
    void rejectsSelectionBelowThreshold() {
        harness.addToBattlefield(player1, new AvatarOfMight());
        harness.addToBattlefield(player1, new AvatarOfMight());
        castDreadnought();

        Permanent avatar = findPermanents(player1, "Avatar of Might").getFirst();
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of(avatar.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.assertOnBattlefield(player1, "Phyrexian Dreadnought");
        assertThat(countPermanents(player1, "Avatar of Might")).isEqualTo(2);
    }
}
