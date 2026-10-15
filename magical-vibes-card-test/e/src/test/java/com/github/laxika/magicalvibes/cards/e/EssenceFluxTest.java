package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BattlegroundGeist;
import com.github.laxika.magicalvibes.cards.g.GarruksPackleader;
import com.github.laxika.magicalvibes.cards.m.MaskwoodNexus;
import com.github.laxika.magicalvibes.cards.m.MentorOfTheMeek;
import com.github.laxika.magicalvibes.cards.s.SpectralShepherd;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EssenceFlux.class, BattlegroundGeist.class, MentorOfTheMeek.class,
        SpectralShepherd.class, MaskwoodNexus.class, GarruksPackleader.class})
class EssenceFluxTest extends BaseCardTest {

    @Test
    @DisplayName("Flickers a non-Spirit creature without a +1/+1 counter")
    void flickerNonSpiritNoCounter() {
        harness.addToBattlefield(player1, new MentorOfTheMeek());
        harness.setHand(player1, List.of(new EssenceFlux()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID creatureId = harness.getPermanentId(player1, "Mentor of the Meek");
        harness.castAndResolveInstant(player1, 0, creatureId);

        Permanent returned = findPermanent(player1, "Mentor of the Meek");
        assertThat(returned.getId()).isNotEqualTo(creatureId);
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Mentor of the Meek"));
    }

    @Test
    @DisplayName("Flickers a Spirit and returns it with a +1/+1 counter")
    void flickerSpiritGetsCounter() {
        harness.addToBattlefield(player1, new BattlegroundGeist());
        harness.setHand(player1, List.of(new EssenceFlux()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID geistId = harness.getPermanentId(player1, "Battleground Geist");
        harness.castAndResolveInstant(player1, 0, geistId);

        Permanent returned = findPermanent(player1, "Battleground Geist");
        assertThat(returned.getId()).isNotEqualTo(geistId);
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target opponent's creature")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new MentorOfTheMeek());
        harness.setHand(player1, List.of(new EssenceFlux()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID opponentCreatureId = harness.getPermanentId(player2, "Mentor of the Meek");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returned creature goes to its owner (not necessarily the controller)")
    void returnsUnderOwnersControl() {
        UUID creatureId = harness.addToBattlefieldAndReturn(player1, new MentorOfTheMeek()).getId();
        gd.stolenCreatures.put(creatureId, player2.getId());

        harness.setHand(player1, List.of(new EssenceFlux()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, creatureId);

        harness.assertOnBattlefield(player2, "Mentor of the Meek");
        harness.assertNotOnBattlefield(player1, "Mentor of the Meek");
    }

    @Test
    @DisplayName("Checks Spirit status on the returned permanent, including continuous effects")
    void returnedCreatureWithAllCreatureTypesGetsCounter() {
        harness.addToBattlefield(player1, new MaskwoodNexus());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new MentorOfTheMeek());
        harness.setHand(player1, List.of(new EssenceFlux()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        Permanent returned = findPermanent(player1, "Mentor of the Meek");
        assertThat(returned.getId()).isNotEqualTo(creature.getId());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The Spirit enters before receiving its counter, so power-three entry does not trigger")
    void spiritCounterIsPutOnAfterEntering() {
        harness.addToBattlefield(player1, new GarruksPackleader());
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new SpectralShepherd());
        harness.setHand(player1, List.of(new EssenceFlux()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, spirit.getId());

        assertThat(findPermanent(player1, "Spectral Shepherd")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Flickering removes old counters and returns the Spirit untapped with one new counter")
    void oldCountersAndTappedStateDoNotSurvive() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new SpectralShepherd());
        spirit.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        spirit.tap();
        harness.setHand(player1, List.of(new EssenceFlux()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, spirit.getId());

        Permanent returned = findPermanent(player1, "Spectral Shepherd");
        assertThat(returned.getId()).isNotEqualTo(spirit.getId());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(returned.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A creature token is exiled and cannot return")
    void tokenDoesNotReturn() {
        harness.addToBattlefield(player1, new MaskwoodNexus());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        UUID tokenId = harness.getPermanentId(player1, "Shapeshifter");
        harness.setHand(player1, List.of(new EssenceFlux()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, tokenId);

        harness.assertNotOnBattlefield(player1, "Shapeshifter");
        harness.assertNotOnBattlefield(player2, "Shapeshifter");
        harness.assertOnBattlefield(player1, "Maskwood Nexus");
    }

    @Test
    @DisplayName("A creature that leaves before resolution is not returned from its new zone")
    void missingTargetIsNotReturned() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new SpectralShepherd());
        harness.setHand(player1, List.of(new EssenceFlux()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, spirit.getId());
        harness.getPermanentRemovalService().removePermanentToHand(gd, spirit);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spectral Shepherd");
        harness.assertInHand(player1, "Spectral Shepherd");
        harness.assertInGraveyard(player1, "Essence Flux");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent you control")
    void cannotTargetNoncreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MaskwoodNexus());
        harness.setHand(player1, List.of(new EssenceFlux()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
