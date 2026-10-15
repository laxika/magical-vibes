package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;




@CardUsed({SettleBeyondReality.class, GrizzlyBears.class, Island.class})
class SettleBeyondRealityTest extends BaseCardTest {

    @Test
    void exileModeExilesAnOpponentsCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast(new int[]{0}, List.of(harness.getPermanentId(player2, "Grizzly Bears")));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    void flickerModeReturnsYourCreatureAsANewPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        java.util.UUID originalId = creature.getId();

        cast(new int[]{1}, List.of(originalId));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(originalId);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void bothModesResolveAgainstTheirIndependentTargets() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(new int[]{0, 1}, List.of(opponentCreature.getId(), ownCreature.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(ownCreature.getId());
    }

    @Test
    void exileModeRejectsACreatureYouControl() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> cast(new int[]{0}, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void flickerModeRejectsAnOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> cast(new int[]{1}, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothModesRejectNonCreatureTargets() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> cast(new int[]{0}, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void flickerModeRejectsALandYouControl() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());

        assertThatThrownBy(() -> cast(new int[]{1}, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void flickerReturnsStolenCreatureToItsOwner() {
        GrizzlyBears card = new GrizzlyBears();
        card.setOwnerId(player2.getId());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, card);
        gd.stolenCreatures.put(creature.getId(), player2.getId());

        cast(new int[]{1}, List.of(creature.getId()));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(harness.getPermanentId(player2, "Grizzly Bears")).isNotEqualTo(creature.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void flickerRemovesCountersAndReturnsUntapped() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.tap();
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        cast(new int[]{1}, List.of(creature.getId()));

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void legalFlickerStillResolvesWhenExileTargetChangesController() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SettleBeyondReality()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(opposingCreature.getId(), ownCreature.getId()), null);
        gd.playerBattlefields.get(player2.getId()).remove(opposingCreature);
        gd.playerBattlefields.get(player1.getId()).add(opposingCreature);
        gd.stolenCreatures.put(opposingCreature.getId(), player2.getId());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(opposingCreature.getId()))
                .noneMatch(permanent -> permanent.getId().equals(ownCreature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void legalExileStillResolvesWhenFlickerTargetChangesController() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SettleBeyondReality()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(opposingCreature.getId(), ownCreature.getId()), null);
        gd.playerBattlefields.get(player1.getId()).remove(ownCreature);
        gd.playerBattlefields.get(player2.getId()).add(ownCreature);
        gd.stolenCreatures.put(ownCreature.getId(), player1.getId());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId).containsExactly(ownCreature.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    private void cast(int[] modes, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new SettleBeyondReality()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castModalSorceryWithModes(player1, 0, 1, 2, modes, targetIds, null);
        harness.passBothPriorities();
    }
}

@CardUsed({SettleBeyondReality.class, GrizzlyBears.class})
class Mh1SettleBeyondRealityTest extends BaseCardTest {

    @Test
    void exileModeExilesCreatureYouDoNotControl() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(new int[]{0}, List.of(creature.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    void flickerModeReturnsCreatureYouControlAsNewPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(new int[]{1}, List.of(creature.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(harness.getPermanentId(player1, "Grizzly Bears"))
                .isNotEqualTo(creature.getId());
    }

    @Test
    void bothModesResolveWithIndependentTargets() {
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent controlledCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(new int[]{0, 1}, List.of(opposingCreature.getId(), controlledCreature.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(harness.getPermanentId(player1, "Grizzly Bears"))
                .isNotEqualTo(controlledCreature.getId());
    }

    @Test
    void modesRejectCreaturesWithTheWrongController() {
        Permanent controlledCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SettleBeyondReality()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0}, List.of(controlledCreature.getId()), null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{1}, List.of(opposingCreature.getId()), null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new SettleBeyondReality()));
        addMana();
        harness.castModalSorceryWithModes(player1, 0, 1, 2, modes, targetIds, null);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
