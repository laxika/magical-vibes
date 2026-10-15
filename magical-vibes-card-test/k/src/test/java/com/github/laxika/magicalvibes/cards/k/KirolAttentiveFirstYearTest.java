package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TrialOfZeal;
import com.github.laxika.magicalvibes.cards.v.VerdurousGearhulk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KirolAttentiveFirstYear.class, GrizzlyBears.class, TrialOfZeal.class, VerdurousGearhulk.class})
class KirolAttentiveFirstYearTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a triggered ability after tapping two creatures")
    void copiesTriggeredAbilityAfterTappingTwoCreatures() {
        harness.setLife(player2, 20);
        Permanent kirol = addCreatureReady(player1, new KirolAttentiveFirstYear());
        Permanent firstCost = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCost = addCreatureReady(player1, new GrizzlyBears());
        UUID triggerId = createTrialOfZealTrigger();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(kirol), null, triggerId);
        harness.handlePermanentChosen(player1, firstCost.getId());
        harness.handlePermanentChosen(player1, secondCost.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(firstCost.isTapped()).isTrue();
        assertThat(secondCost.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Can activate only once each turn")
    void canActivateOnlyOnceEachTurn() {
        Permanent kirol = addCreatureReady(player1, new KirolAttentiveFirstYear());
        Permanent firstCost = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCost = addCreatureReady(player1, new GrizzlyBears());
        UUID triggerId = createTrialOfZealTrigger();
        int kirolIndex = gd.playerBattlefields.get(player1.getId()).indexOf(kirol);

        harness.activateAbility(player1, kirolIndex, null, triggerId);
        harness.handlePermanentChosen(player1, firstCost.getId());
        harness.handlePermanentChosen(player1, secondCost.getId());

        addCreatureReady(player1, new GrizzlyBears());
        assertThatThrownBy(() -> harness.activateAbility(player1, kirolIndex, null, triggerId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires two untapped creatures to pay the activation cost")
    void requiresTwoUntappedCreatures() {
        Permanent kirol = addCreatureReady(player1, new KirolAttentiveFirstYear());
        UUID triggerId = createTrialOfZealTrigger();

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(kirol),
                null,
                triggerId
        )).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can change the copy's target without changing the original")
    void canChooseNewTargetForCopy() {
        Permanent kirol = addCreatureReady(player1, new KirolAttentiveFirstYear());
        Permanent cost = addCreatureReady(player1, new GrizzlyBears());
        Permanent newTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        UUID triggerId = createTrialOfZealTrigger();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(kirol), null, triggerId);
        harness.handlePermanentChosen(player1, kirol.getId());
        harness.handlePermanentChosen(player1, cost.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, newTarget.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player2, 17);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Kirol and another summoning-sick creature can pay the tap cost")
    void canTapSummoningSickCreaturesIncludingKirol() {
        Permanent kirol = harness.addToBattlefieldAndReturn(player1, new KirolAttentiveFirstYear());
        Permanent cost = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        kirol.setSummoningSick(true);
        cost.setSummoningSick(true);
        UUID triggerId = createTrialOfZealTrigger();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(kirol), null, triggerId);
        harness.handlePermanentChosen(player1, kirol.getId());
        harness.handlePermanentChosen(player1, cost.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(kirol.isTapped()).isTrue();
        assertThat(cost.isTapped()).isTrue();
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Cannot copy an opponent's triggered ability")
    void cannotCopyOpponentTriggeredAbility() {
        Permanent kirol = addCreatureReady(player1, new KirolAttentiveFirstYear());
        addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new TrialOfZeal()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castEnchantment(player2, 0, player1.getId());
        harness.passBothPriorities();
        UUID triggerId = gd.stack.getLast().getTargetableId();

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(kirol), null, triggerId))
                .isInstanceOf(IllegalStateException.class);
        assertThat(kirol.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot copy a spell instead of a triggered ability")
    void cannotCopySpell() {
        Permanent kirol = addCreatureReady(player1, new KirolAttentiveFirstYear());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TrialOfZeal()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0, player2.getId());
        UUID spellId = gd.stack.getLast().getTargetableId();

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(kirol), null, spellId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Offers new targets when copying a triggered ability with multiple targets")
    void canChooseNewTargetsForMultiTargetTrigger() {
        Permanent kirol = addCreatureReady(player1, new KirolAttentiveFirstYear());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        gd.pendingETBDamageAssignments = Map.of(first.getId(), 1, second.getId(), 3);
        harness.setHand(player1, List.of(new VerdurousGearhulk()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        UUID triggerId = gd.stack.getLast().getTargetableId();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(kirol), null, triggerId);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
    }

    private UUID createTrialOfZealTrigger() {
        harness.setHand(player1, List.of(new TrialOfZeal()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        return gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .findFirst()
                .orElseThrow()
                .getCard()
                .getId();
    }
}
