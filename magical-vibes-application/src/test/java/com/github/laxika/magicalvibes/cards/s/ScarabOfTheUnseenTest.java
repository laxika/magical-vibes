package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScarabOfTheUnseen.class, Forest.class, GrizzlyBears.class, HolyStrength.class})
class ScarabOfTheUnseenTest extends BaseCardTest {

    @Test
    @DisplayName("Returns every Aura attached to the target, including an opponent's, to its owner's hand")
    void returnsAllAttachedAuras() {
        harness.addToBattlefield(player1, new ScarabOfTheUnseen());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent ownAura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        ownAura.setAttachedTo(bears.getId());

        Permanent opponentAura = harness.addToBattlefieldAndReturn(player2, new HolyStrength());
        opponentAura.setAttachedTo(bears.getId());

        int p1HandBefore = gd.playerHands.get(player1.getId()).size();
        int p2HandBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownAura);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentAura);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1HandBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(p2HandBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
    }

    @Test
    @DisplayName("Sacrifices itself and schedules a draw at the next upkeep")
    void sacrificesItselfAndSchedulesDraw() {
        Permanent scarab = harness.addToBattlefieldAndReturn(player1, new ScarabOfTheUnseen());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(scarab);

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Draws a card at the beginning of the next turn's upkeep")
    void drawsAtNextTurnUpkeep() {
        harness.addToBattlefield(player1, new ScarabOfTheUnseen());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a permanent owned by an opponent")
    void cannotTargetOpponentPermanent() {
        harness.addToBattlefield(player1, new ScarabOfTheUnseen());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentLand.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a permanent you own");
    }

    @Test
    @DisplayName("A tapped Scarab cannot pay its activation cost")
    void cannotActivateWhileTapped() {
        Permanent scarab = harness.addToBattlefieldAndReturn(player1, new ScarabOfTheUnseen());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        scarab.setTapped(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(scarab);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A draw scheduled before upkeep waits until the next turn's upkeep")
    void doesNotDrawDuringSameTurnUpkeep() {
        harness.addToBattlefield(player1, new ScarabOfTheUnseen());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);
        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("Can target an owned permanent controlled by an opponent")
    void canTargetOwnedPermanentUnderOpponentControl() {
        harness.addToBattlefield(player1, new ScarabOfTheUnseen());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.stolenCreatures.put(bears.getId(), player1.getId());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new HolyStrength());
        aura.setAttachedTo(bears.getId());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Holy Strength");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears).doesNotContain(aura);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target an opponent-owned permanent even when controlling it")
    void cannotTargetControlledButUnownedPermanent() {
        harness.addToBattlefield(player1, new ScarabOfTheUnseen());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.stolenCreatures.put(bears.getId(), player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a permanent you own");
        harness.assertOnBattlefield(player1, "Scarab of the Unseen");
    }

    @Test
    @DisplayName("Targeting itself pays the sacrifice cost but does not schedule a draw")
    void selfTargetIsIllegalOnResolution() {
        Permanent scarab = harness.addToBattlefieldAndReturn(player1, new ScarabOfTheUnseen());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, scarab.getId());
        harness.assertInGraveyard(player1, "Scarab of the Unseen");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Returns a stolen Aura to its owner and leaves Auras on other permanents alone")
    void returnsStolenAuraToOwnerOnlyFromTarget() {
        harness.addToBattlefield(player1, new ScarabOfTheUnseen());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent stolenAura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        gd.stolenCreatures.put(stolenAura.getId(), player2.getId());
        stolenAura.setAttachedTo(target.getId());
        Permanent otherAura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        otherAura.setAttachedTo(other.getId());
        int ownHandBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Holy Strength");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(ownHandBefore);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(target, other, otherAura).doesNotContain(stolenAura);
        assertThat(otherAura.getAttachedTo()).isEqualTo(other.getId());
    }
}
