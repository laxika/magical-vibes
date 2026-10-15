package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MishrasFactory;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Suspend.class, GrizzlyBears.class, Forest.class, MishrasFactory.class})
class SuspendTest extends BaseCardTest {

    @Test
    void exilesTargetCreatureWithTwoTimeCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSuspend(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
        assertThat(gd.exiledCardTimeCounters).containsEntry(target.getCard().getId(), 2);
    }

    @Test
    void suspendedCreatureLosesCountersAndMayBeCastForFree() {
        GrizzlyBears targetCard = new GrizzlyBears();
        Permanent target = harness.addToBattlefieldAndReturn(player1, targetCard);

        castSuspend(target);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.exiledCardTimeCounters).containsEntry(targetCard.getId(), 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(targetCard.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == targetCard);

        declareAttackers(player1, List.of(0));
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isAttacking()).isTrue();
    }

    @Test
    void opponentCreatureUsesItsOwnersUpkeepAndReturnsToItsOwner() {
        GrizzlyBears targetCard = new GrizzlyBears();
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);
        castSuspend(target);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.exiledCardTimeCounters).containsEntry(targetCard.getId(), 2);

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(gd.exiledCardTimeCounters).containsEntry(targetCard.getId(), 1);
        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void decliningFreeCastLeavesCreatureExiledWithoutFurtherUpkeepTriggers() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castSuspend(target);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(target.getCard().getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(target.getCard());
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void suspendedAnimatedLandIsPlayedAsALandWhenItsLastCounterIsRemoved() {
        Permanent factory = harness.addToBattlefieldAndReturn(player1, new MishrasFactory());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        castSuspend(factory);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mishra's Factory");
        harness.assertNotInGraveyard(player1, "Mishra's Factory");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void onlyTargetsCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new Suspend()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void castSuspend(Permanent target) {
        harness.setHand(player1, List.of(new Suspend()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
