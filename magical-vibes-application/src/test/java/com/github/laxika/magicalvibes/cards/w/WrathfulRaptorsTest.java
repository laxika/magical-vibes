package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.RaptorHatchling;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WrathfulRaptors.class, GrizzlyBears.class, Mountain.class, RaptorHatchling.class, Shock.class})
class WrathfulRaptorsTest extends BaseCardTest {

    @Test
    @DisplayName("Reflects damage dealt to a Dinosaur at a non-Dinosaur target")
    void reflectsDamageDealtToDinosaur() {
        harness.addToBattlefield(player1, new WrathfulRaptors());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID raptorsId = harness.getPermanentId(player1, "Wrathful Raptors");
        harness.castInstant(player2, 0, raptorsId);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not trigger when a non-Dinosaur permanent is dealt damage")
    void doesNotTriggerForNonDinosaurPermanent() {
        harness.addToBattlefield(player1, new WrathfulRaptors());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player2, 0, bearsId);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not offer Dinosaurs as reflected-damage targets")
    void excludesDinosaursFromReflectedDamageTargets() {
        harness.addToBattlefield(player1, new WrathfulRaptors());
        harness.addToBattlefield(player1, new RaptorHatchling());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID raptorsId = harness.getPermanentId(player1, "Wrathful Raptors");
        UUID hatchlingId = harness.getPermanentId(player1, "Raptor Hatchling");
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID mountainId = harness.getPermanentId(player1, "Mountain");
        harness.castInstant(player2, 0, raptorsId);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds())
                .contains(player2.getId(), bearsId)
                .doesNotContain(raptorsId, hatchlingId, mountainId);
    }
}
