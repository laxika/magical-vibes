package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Daydream.class, GrizzlyBears.class, Island.class})
class DaydreamTest extends BaseCardTest {

    @Test
    @DisplayName("Flickers target creature and returns it with a +1/+1 counter")
    void flickerReturnsWithCounter() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Daydream()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, List.of(bearId));

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(bear.getId()).isNotEqualTo(bearId);
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target opponent's creature")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Daydream()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID opponentBearId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(opponentBearId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returned creature goes to its owner, not necessarily the controller")
    void returnsUnderOwnersControl() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);
        UUID bearPermId = harness.getPermanentId(player1, "Grizzly Bears");
        gd.stolenCreatures.put(bearPermId, player2.getId());

        harness.setHand(player1, List.of(new Daydream()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(bearPermId));

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Flashback flickers target creature with a +1/+1 counter")
    void flashbackFlickersWithCounter() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Daydream()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveFlashback(player1, 0, bearId);

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Flashback exiles Daydream after resolving")
    void flashbackExilesAfterResolving() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Daydream()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveFlashback(player1, 0, bearId);

        harness.assertNotInGraveyard(player1, "Daydream");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Daydream"));
    }

    @Test
    void cannotTargetNoncreature() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new Daydream()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returningCreatureLosesOldCountersAndReturnsUntapped() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        original.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        original.tap();
        harness.setHand(player1, List.of(new Daydream()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(original.getId()));

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(original.getId());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(returned.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Daydream");
    }

    @Test
    void flashbackExilesSpellWhenTargetChangesController() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Daydream()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castFlashback(player1, 0, List.of(bear.getId()));

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(CreatureControlService.class)
                .applyControlEffect(gd, player2.getId(), bear,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT), EffectDuration.PERMANENT,
                        null, "Test setup"));
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Grizzly Bears").getId()).isEqualTo(bear.getId());
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotInGraveyard(player1, "Daydream");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Daydream"));
    }

    @Test
    @DisplayName("Flashback puts sorcery spell on stack")
    void flashbackPutsOnStackAsSorcery() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Daydream()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castFlashback(player1, 0, List.of(bearId));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Daydream");
        assertThat(gd.stack.getFirst().isCastWithFlashback()).isTrue();
    }
}
