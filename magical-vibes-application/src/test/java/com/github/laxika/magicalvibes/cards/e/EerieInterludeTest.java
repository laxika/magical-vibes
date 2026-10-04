package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LayClaim;
import com.github.laxika.magicalvibes.cards.v.VesselOfEphemera;
import com.github.laxika.magicalvibes.cards.u.UninvitedGeist;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EerieInterlude.class, GrizzlyBears.class, Forest.class, LayClaim.class,
        DevilthornFox.class, VesselOfEphemera.class, UninvitedGeist.class})
class EerieInterludeTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles all selected creatures and returns them at the next end step")
    void exilesSelectedCreaturesAndReturnsThemAtNextEndStep() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new com.github.laxika.magicalvibes.cards.f.Forest());
        harness.setHand(player1, List.of(new EerieInterlude()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, List.of(first.getId(), second.getId()));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Grizzly Bears", "Grizzly Bears");

        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Allows choosing no creatures")
    void allowsChoosingNoCreatures() {
        harness.setHand(player1, List.of(new EerieInterlude()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, List.of());

        harness.assertNotOnBattlefield(player1, "Eerie Interlude");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target only creatures you control")
    void canTargetOnlyCreaturesYouControl() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EerieInterlude()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Returns selected creatures under their owners' control")
    void returnsSelectedCreaturesUnderOwnersControl() {
        Permanent stolenCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.l.LayClaim()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castEnchantment(player1, 0, stolenCreature.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        UUID controlledId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new EerieInterlude()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, controlledId);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");

        advanceToEndStep();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void canExileMoreThanNinetyNineCreatures() {
        List<UUID> targets = IntStream.range(0, 100)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player1, new DevilthornFox()).getId())
                .toList();
        harness.setHand(player1, List.of(new EerieInterlude()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, targets);

        assertThat(countPermanents(player1, "Devilthorn Fox")).isZero();
        advanceToEndStep();
        assertThat(countPermanents(player1, "Devilthorn Fox")).isEqualTo(100);
    }

    @Test
    void differentOwnersReturnTogetherFromOneTriggerControlledByTheCaster() {
        Permanent stolen = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
        Permanent owned = harness.addToBattlefieldAndReturn(player1, new DevilthornFox());
        harness.setHand(player1, List.of(new LayClaim()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castEnchantment(player1, 0, stolen.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new EerieInterlude()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, List.of(owned.getId(), stolen.getId()));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(EerieInterlude.class);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Devilthorn Fox")).isEqualTo(1);
        assertThat(countPermanents(player2, "Devilthorn Fox")).isEqualTo(1);
    }

    @Test
    void returnsAsANewUntappedPermanentWithoutCounters() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DevilthornFox());
        creature.tap();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new EerieInterlude()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        advanceToEndStep();

        Permanent returned = findPermanent(player1, "Devilthorn Fox");
        assertThat(returned.getId()).isNotEqualTo(creature.getId());
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(returned.isSummoningSick()).isTrue();
    }

    @Test
    void exiledCreatureTokensDoNotReturn() {
        harness.addToBattlefield(player1, new VesselOfEphemera());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(2);
        List<UUID> targets = findPermanents(player1, "Spirit").stream().map(Permanent::getId).toList();
        harness.setHand(player1, List.of(new EerieInterlude()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, targets);
        advanceToEndStep();

        assertThat(countPermanents(player1, "Spirit")).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void castDuringEndStepWaitsForTheFollowingTurnsEndStep() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DevilthornFox());
        harness.setLibrary(player2, List.of(new Forest()));
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new EerieInterlude()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        assertThat(countPermanents(player1, "Devilthorn Fox")).isZero();
        harness.passUntil(player2, TurnStep.POSTCOMBAT_MAIN);
        assertThat(countPermanents(player1, "Devilthorn Fox")).isZero();
        harness.passUntil(player2, TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Devilthorn Fox")).isEqualTo(1);
    }

    @Test
    void transformedCreatureReturnsWithItsFrontFaceUp() {
        Permanent geist = addCreatureReady(player1, new UninvitedGeist());
        geist.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();
        assertThat(geist.isTransformed()).isTrue();
        harness.setHand(player1, List.of(new EerieInterlude()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, geist.getId());
        advanceToEndStep();

        Permanent returned = findPermanent(player1, "Uninvited Geist");
        assertThat(returned.isTransformed()).isFalse();
        assertThat(returned.getId()).isNotEqualTo(geist.getId());
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
