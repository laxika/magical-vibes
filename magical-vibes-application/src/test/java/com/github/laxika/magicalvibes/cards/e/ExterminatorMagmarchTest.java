package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BeastWithin;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExterminatorMagmarch.class, BeastWithin.class, Forest.class, GrizzlyBears.class, Shock.class})
class ExterminatorMagmarchTest extends BaseCardTest {

    private Player player3;

    @Test
    @DisplayName("Copies a single-target spell onto a nonland permanent controlled by another opponent")
    void copiesSpellOntoAnotherOpponentsPermanent() {
        addThirdPlayer();
        harness.addToBattlefield(player1, new ExterminatorMagmarch());
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent otherOpponentTarget = harness.addToBattlefieldAndReturn(player3, new GrizzlyBears());
        Permanent secondOtherOpponentTarget = harness.addToBattlefieldAndReturn(player3, new GrizzlyBears());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, originalTarget.getId());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                otherOpponentTarget.getId(), secondOtherOpponentTarget.getId());

        harness.handlePermanentChosen(player1, otherOpponentTarget.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().isCopy()).isTrue();
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(otherOpponentTarget.getId());
    }

    @Test
    @DisplayName("Does not trigger in a two-player game")
    void doesNotTriggerWithoutAnotherOpponent() {
        harness.addToBattlefield(player1, new ExterminatorMagmarch());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new BeastWithin()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Does not trigger when another opponent controls only lands")
    void doesNotTriggerWithoutAnotherOpponentNonlandPermanent() {
        addThirdPlayer();
        harness.addToBattlefield(player1, new ExterminatorMagmarch());
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player3, new Forest());

        harness.setHand(player1, List.of(new BeastWithin()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, originalTarget.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("The regeneration ability grants a regeneration shield")
    void regenerationAbilityGrantsShield() {
        Permanent magmarch = addReadyMagmach();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(magmarch.getRegenerationShield()).isEqualTo(1);
    }

    private Permanent addReadyMagmach() {
        return harness.addToBattlefieldAndReturn(player1, new ExterminatorMagmarch());
    }

    private void addThirdPlayer() {
        UUID thirdPlayerId = UUID.randomUUID();
        player3 = new Player(thirdPlayerId, "Charlie");
        gd.playerIds.add(thirdPlayerId);
        gd.orderedPlayerIds.add(thirdPlayerId);
        gd.playerNames.add("Charlie");
        gd.playerIdToName.put(thirdPlayerId, "Charlie");
        gd.playerDecks.put(thirdPlayerId, new ArrayList<>());
        gd.playerHands.put(thirdPlayerId, new ArrayList<>());
        gd.playerBattlefields.put(thirdPlayerId, new ArrayList<>());
        gd.playerGraveyards.put(thirdPlayerId, new ArrayList<>());
        gd.playerCommandZones.put(thirdPlayerId, new ArrayList<>());
        gd.playerManaPools.put(thirdPlayerId, new ManaPool());
        gd.playerLifeTotals.put(thirdPlayerId, 20);
        harness.getSessionManager().registerPlayer(new FakeConnection("conn-3"), thirdPlayerId, "Charlie");
    }
}
