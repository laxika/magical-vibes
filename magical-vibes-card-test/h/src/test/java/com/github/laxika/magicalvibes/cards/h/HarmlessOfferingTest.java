package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.t.Terrarion;
import com.github.laxika.magicalvibes.cards.u.Unsubstantiate;
import com.github.laxika.magicalvibes.cards.w.WoodlandPatrol;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HarmlessOffering.class, WoodlandPatrol.class, Terrarion.class, Unsubstantiate.class})
class HarmlessOfferingTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent gains control of target permanent you control")
    void targetOpponentGainsControlOfTargetPermanent() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new WoodlandPatrol()).getId();
        harness.setHand(player1, List.of(new HarmlessOffering()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId(), targetId));

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(permanent -> permanent.getId().equals(targetId));
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(permanent -> permanent.getId().equals(targetId));
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetYourself() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new WoodlandPatrol()).getId();
        harness.setHand(player1, List.of(new HarmlessOffering()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(player1.getId(), targetId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Cannot target a permanent controlled by another player")
    void cannotTargetPermanentControlledByAnotherPlayer() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new WoodlandPatrol()).getId();
        harness.setHand(player1, List.of(new HarmlessOffering()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(player2.getId(), targetId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a permanent you control");
    }

    @Test
    void canGiveANoncreaturePermanentWithoutUntappingIt() {
        var artifact = harness.enterBattlefieldAndReturn(player1, new Terrarion());
        assertThat(artifact.isTapped()).isTrue();
        harness.setHand(player1, List.of(new HarmlessOffering()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId(), artifact.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
        assertThat(artifact.isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Terrarion");
    }

    @Test
    void controlChangePersistsIntoTheOpponentsTurn() {
        var creature = harness.addToBattlefieldAndReturn(player1, new WoodlandPatrol());
        creature.setSummoningSick(false);
        harness.setHand(player1, List.of(new HarmlessOffering()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId(), creature.getId()));

        assertThat(creature.isSummoningSick()).isTrue();
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    void donatedCreatureReturnsToItsOwnersHand() {
        var creature = harness.addToBattlefieldAndReturn(player1, new WoodlandPatrol());
        harness.setHand(player1, List.of(new HarmlessOffering()));
        harness.setHand(player2, List.of(new Unsubstantiate()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId(), creature.getId()));

        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.assertInHand(player1, "Woodland Patrol");
        harness.assertNotInHand(player2, "Woodland Patrol");
        harness.assertNotOnBattlefield(player2, "Woodland Patrol");
    }

    @Test
    void permanentThatLeavesBeforeResolutionIsNotTransferred() {
        var creature = harness.addToBattlefieldAndReturn(player1, new WoodlandPatrol());
        harness.setHand(player1, List.of(new HarmlessOffering()));
        harness.setHand(player2, List.of(new Unsubstantiate()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, List.of(player2.getId(), creature.getId()));

        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Woodland Patrol");
        harness.assertNotOnBattlefield(player1, "Woodland Patrol");
        harness.assertNotOnBattlefield(player2, "Woodland Patrol");
        harness.assertInGraveyard(player1, "Harmless Offering");
    }
}
