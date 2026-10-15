package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.PhyrexianAtlas;
import com.github.laxika.magicalvibes.cards.p.PlanarDisruption;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SerumSnare.class, GrizzlyBears.class, HillGiant.class, Island.class,
        PhyrexianAtlas.class, PlanarDisruption.class})
class SerumSnareTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a low mana value nonland permanent and proliferates")
    void returnsLowManaValuePermanentAndProliferates() {
        Permanent creatureWithCounter = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creatureWithCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        castSerumSnare(targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(creatureWithCounter.getId()));

        assertThat(creatureWithCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Returns a high mana value nonland permanent without proliferating")
    void returnsHighManaValuePermanentWithoutProliferating() {
        Permanent creatureWithCounter = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creatureWithCounter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addToBattlefield(player2, new HillGiant());
        UUID targetId = harness.getPermanentId(player2, "Hill Giant");

        castSerumSnare(targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInHand(player2, "Hill Giant");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
        assertThat(creatureWithCounter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Island());
        UUID targetId = harness.getPermanentId(player2, "Island");

        assertThatThrownBy(() -> castSerumSnare(targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    @Test
    void manaValueThreeArtifactAllowsProliferatingPlayersAndEveryCounterKind() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PhyrexianAtlas());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new PhyrexianAtlas());
        other.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        other.setCounterCount(CounterType.CHARGE, 3);
        gd.playerPoisonCounters.put(player2.getId(), 1);

        castSerumSnare(artifact.getId());
        harness.passBothPriorities();
        harness.assertInHand(player2, "Phyrexian Atlas");
        harness.handleMultiplePermanentsChosen(player1, List.of(other.getId(), player2.getId()));

        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(other.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
    }

    @Test
    void canReturnOwnPermanentAndChooseNothingToProliferate() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PhyrexianAtlas());
        gd.playerPoisonCounters.put(player2.getId(), 1);

        castSerumSnare(target.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Phyrexian Atlas");
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void returnedPermanentIsNoLongerEligibleForProliferation() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PhyrexianAtlas());
        target.setCounterCount(CounterType.CHARGE, 1);

        castSerumSnare(target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Phyrexian Atlas");
        harness.assertNotOnBattlefield(player2, "Phyrexian Atlas");
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Serum Snare");
    }

    @Test
    void doesNotProliferateWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PhyrexianAtlas());
        gd.playerPoisonCounters.put(player2.getId(), 1);
        castSerumSnare(target.getId());
        harness.getPermanentRemovalService().removePermanentToHand(gd, target);

        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Serum Snare");
    }

    @Test
    void unattachedAuraRemainsAvailableForProliferationUntilResolutionFinishes() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PhyrexianAtlas());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new PlanarDisruption());
        aura.setAttachedTo(target.getId());
        aura.setCounterCount(CounterType.CHARGE, 1);

        castSerumSnare(target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Phyrexian Atlas");
        harness.assertOnBattlefield(player2, "Planar Disruption");
        harness.handleMultiplePermanentsChosen(player1, List.of(aura.getId()));

        assertThat(aura.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        harness.assertInGraveyard(player2, "Planar Disruption");
    }

    private void castSerumSnare(UUID targetId) {
        harness.setHand(player1, List.of(new SerumSnare()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, targetId);
    }
}
