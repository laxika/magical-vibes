package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Shunt;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Twinferno.class, GrizzlyBears.class, Shock.class, Shunt.class, LavaAxe.class})
class TwinfernoTest extends BaseCardTest {

    @Test
    @DisplayName("Copy mode copies the next instant or sorcery spell this turn")
    void copyModeCopiesNextInstantOrSorcery() {
        harness.setHand(player1, List.of(new Twinferno()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount)
                .containsEntry(player1.getId(), 1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount)
                .doesNotContainKey(player1.getId());
    }

    @Test
    @DisplayName("Double-strike mode grants double strike to a creature you control")
    void doubleStrikeModeGrantsDoubleStrike() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Twinferno()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castModalInstant(player1, 0, 1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Double-strike mode cannot target an opponent's creature")
    void doubleStrikeModeCannotTargetOpponentCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Twinferno()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("control");
    }

    @Test
    void copyDealsDamageAndOnlyTheNextSpellIsCopied() {
        resolveCopyMode();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 16);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 14);
    }

    @Test
    void copyCanChooseANewTargetWithoutChangingTheOriginal() {
        resolveCopyMode();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    void opponentsSpellDoesNotConsumeTheDelayedCopy() {
        resolveCopyMode();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 18);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 16);
    }

    @Test
    void creatureSpellDoesNotConsumeTheDelayedCopy() {
        resolveCopyMode();
        harness.setHand(player1, List.of(new GrizzlyBears(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 16);
    }

    @Test
    void unusedCopyExpiresAtEndOfTurn() {
        resolveCopyMode();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyUsesOriginalSpellsTargetAtTriggerResolution() {
        resolveCopyMode();
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.setHand(player2, List.of(new Shunt()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, shock.getId());
        harness.handlePermanentChosen(player2, player1.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }

    @Test
    void copyModeCopiesASorcery() {
        resolveCopyMode();
        harness.setHand(player1, List.of(new LavaAxe()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 10);
    }

    @Test
    void modalCopyKeepsDoubleStrikeModeAndCanRetarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveCopyMode();
        harness.setHand(player1, List.of(new Twinferno()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castModalInstant(player1, 0, 1, List.of(first.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, first, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void copyModeDoesNotCopyASpellAlreadyOnTheStack() {
        harness.setHand(player1, List.of(new Shock(), new Twinferno()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, player2.getId());
        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 18);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 14);
    }

    @Test
    void doubleStrikeModeDoesNothingWhenItsTargetLeavesBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Twinferno()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castModalInstant(player1, 0, 1, List.of(creature.getId()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    private void resolveCopyMode() {
        harness.setHand(player1, List.of(new Twinferno()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();
    }
}
