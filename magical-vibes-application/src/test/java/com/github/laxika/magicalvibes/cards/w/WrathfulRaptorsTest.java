package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BlasphemousAct;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LoxodonWarhammer;
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
        harness.castAndResolveInstant(player2, 0, raptorsId);

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
        harness.castAndResolveInstant(player2, 0, bearsId);

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
        harness.castAndResolveInstant(player2, 0, raptorsId);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds())
                .contains(player2.getId(), bearsId)
                .doesNotContain(raptorsId, hatchlingId, mountainId);
    }

    @Test
    @CardUsed({LoxodonWarhammer.class})
    @DisplayName("A lethally damaged Dinosaur deals reflected damage using its last-known lifelink")
    void lethallyDamagedDinosaurUsesItsOwnLifelink() {
        harness.addToBattlefield(player1, new LoxodonWarhammer());
        harness.addToBattlefield(player1, new WrathfulRaptors());
        harness.addToBattlefield(player1, new RaptorHatchling());
        UUID hatchlingId = harness.getPermanentId(player1, "Raptor Hatchling");
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, hatchlingId);
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, hatchlingId);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Raptor Hatchling");
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Damage to an opposing Dinosaur does not trigger Wrathful Raptors")
    void doesNotTriggerForOpposingDinosaur() {
        harness.addToBattlefield(player1, new WrathfulRaptors());
        harness.addToBattlefield(player2, new RaptorHatchling());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID hatchlingId = harness.getPermanentId(player2, "Raptor Hatchling");

        harness.castAndResolveInstant(player1, 0, hatchlingId);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Reflected damage can destroy a non-Dinosaur creature")
    void reflectedDamageDestroysNonDinosaurCreature() {
        harness.addToBattlefield(player1, new WrathfulRaptors());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID raptorsId = harness.getPermanentId(player1, "Wrathful Raptors");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.castAndResolveInstant(player2, 0, raptorsId);
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Wrathful Raptors");
    }

    @Test
    @DisplayName("Reflected damage may target the ability controller")
    void reflectedDamageCanTargetController() {
        harness.addToBattlefield(player1, new WrathfulRaptors());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID raptorsId = harness.getPermanentId(player1, "Wrathful Raptors");

        harness.castAndResolveInstant(player2, 0, raptorsId);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed({BlasphemousAct.class})
    @DisplayName("Simultaneous lethal damage triggers for Wrathful Raptors and another Dinosaur")
    void simultaneousLethalDamageTriggersForEveryDinosaur() {
        harness.addToBattlefield(player1, new WrathfulRaptors());
        harness.addToBattlefield(player1, new RaptorHatchling());
        harness.setLife(player2, 40);
        harness.setHand(player1, List.of(new BlasphemousAct()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, (UUID) null);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Wrathful Raptors");
        harness.assertInGraveyard(player1, "Raptor Hatchling");
        harness.assertLife(player2, 14);
    }
}
