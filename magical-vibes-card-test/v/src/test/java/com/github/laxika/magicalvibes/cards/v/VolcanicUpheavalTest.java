package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VolcanicUpheaval.class, Mountain.class, GrizzlyBears.class, EvolvingWilds.class})
class VolcanicUpheavalTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Volcanic Upheaval destroys target land")
    void destroysTargetLand() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new VolcanicUpheaval()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Mountain"));

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new VolcanicUpheaval()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castAndResolveInstant(
                player1, 0, harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Volcanic Upheaval can destroy its controller's nonbasic land")
    void destroysOwnNonbasicLand() {
        harness.addToBattlefield(player1, new EvolvingWilds());
        harness.setHand(player1, List.of(new VolcanicUpheaval()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Evolving Wilds"));

        harness.assertNotOnBattlefield(player1, "Evolving Wilds");
        harness.assertInGraveyard(player1, "Evolving Wilds");
        harness.assertInGraveyard(player1, "Volcanic Upheaval");
    }

    @Test
    @DisplayName("A land destroyed in response leaves Volcanic Upheaval with no legal target")
    void targetDestroyedInResponse() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new EvolvingWilds());
        harness.setHand(player1, List.of(new VolcanicUpheaval(), new VolcanicUpheaval()));
        harness.addMana(player1, ManaColor.RED, 8);
        UUID targetId = harness.getPermanentId(player2, "Mountain");

        harness.castInstant(player1, 0, targetId);
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
        harness.assertOnBattlefield(player2, "Evolving Wilds");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Volcanic Upheaval"))
                .hasSize(2);
    }
}
