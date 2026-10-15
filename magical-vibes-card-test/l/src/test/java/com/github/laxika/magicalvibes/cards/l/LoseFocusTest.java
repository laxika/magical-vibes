package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SupremeVerdict;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoseFocus.class, YouthfulKnight.class, Island.class, SupremeVerdict.class})
class LoseFocusTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell when its controller cannot pay {2}")
    void countersWhenControllerCannotPay() {
        YouthfulKnight knight = castKnight();
        castLoseFocus(knight, List.of());

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Youthful Knight");
        harness.assertNotOnBattlefield(player1, "Youthful Knight");
    }

    @Test
    @DisplayName("The target spell resolves when its controller pays {2}")
    void resolvesWhenControllerPays() {
        YouthfulKnight knight = castKnight();
        harness.addMana(player1, ManaColor.BLUE, 2);
        castLoseFocus(knight, List.of());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Youthful Knight");
    }

    @Test
    @DisplayName("Replicate creates one copy for each additional {U} paid")
    void replicateCreatesCopies() {
        YouthfulKnight knight = castKnight();
        harness.setHand(player2, List.of(new LoseFocus()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.passPriority(player1);
        harness.castInstantWithRepeatedCosts(player2, 0, knight.getId(), List.of("{U}", "{U}"));

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
        assertThat(gd.pendingMayAbilities).hasSize(2);

        harness.handleMayAbilityChosen(player2, false);
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Youthful Knight");
    }

    @Test
    @DisplayName("The spell is countered when its controller declines an affordable payment")
    void countersWhenControllerDeclinesPayment() {
        YouthfulKnight knight = castKnight();
        harness.addMana(player1, ManaColor.BLUE, 2);
        castLoseFocus(knight, List.of());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Youthful Knight");
        harness.assertNotOnBattlefield(player1, "Youthful Knight");
    }

    @Test
    @DisplayName("Paying for a replicate copy does not pay for the original spell")
    void eachCounterEffectRequiresItsOwnPayment() {
        YouthfulKnight knight = castKnight();
        harness.addMana(player1, ManaColor.BLUE, 2);
        castLoseFocus(knight, List.of("{U}"));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Youthful Knight");
        harness.assertNotOnBattlefield(player1, "Youthful Knight");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The target resolves after paying separately for the original and its copy")
    void payingForBothCounterEffectsAllowsTargetToResolve() {
        YouthfulKnight knight = castKnight();
        harness.addMana(player1, ManaColor.BLUE, 4);
        castLoseFocus(knight, List.of("{U}"));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Youthful Knight");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Lose Focus")).hasSize(1);
    }

    @Test
    @DisplayName("A replicate copy may target and counter the original Lose Focus")
    void replicateCopyCanChooseNewTarget() {
        YouthfulKnight knight = castKnight();
        castLoseFocus(knight, List.of("{U}"));
        StackEntry original = gd.stack.stream()
                .filter(entry -> entry.getCard() instanceof LoseFocus && !entry.isCopy()
                        && entry.getTargetId() != null)
                .findFirst().orElseThrow();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, original.getTargetableId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Youthful Knight");
        harness.assertInGraveyard(player2, "Lose Focus");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller may generate mana during resolution to pay the tax")
    void offersPaymentWhenManaCanBeGeneratedDuringResolution() {
        YouthfulKnight knight = castKnight();
        Permanent firstIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent secondIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        castLoseFocus(knight, List.of());

        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Youthful Knight");
        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(firstIsland.isTapped()).isFalse();
        assertThat(secondIsland.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Replicate still creates a copy after the original spell is countered")
    void replicateSurvivesOriginalBeingCountered() {
        YouthfulKnight knight = castKnight();
        castLoseFocus(knight, List.of("{U}"));
        StackEntry original = gd.stack.stream()
                .filter(entry -> entry.getCard() instanceof LoseFocus && entry.getTargetId() != null)
                .findFirst().orElseThrow();
        harness.setHand(player1, List.of(new LoseFocus()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, original.getTargetableId());

        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Lose Focus");
        harness.passBothPriorities();
        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Youthful Knight");
        harness.assertNotOnBattlefield(player1, "Youthful Knight");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An uncounterable spell's controller may still choose to pay")
    void offersPaymentForUncounterableSpell() {
        SupremeVerdict verdict = new SupremeVerdict();
        harness.castFromHand(player1, verdict, "{1}{W}{W}{U}");
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(new LoseFocus()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, verdict.getId());

        harness.passBothPriorities();

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Supreme Verdict");
    }

    private YouthfulKnight castKnight() {
        YouthfulKnight knight = new YouthfulKnight();
        harness.castFromHand(player1, knight, "{1}{W}");
        return knight;
    }

    private void castLoseFocus(YouthfulKnight target, List<String> replicatePayments) {
        harness.setHand(player2, List.of(new LoseFocus()));
        harness.addMana(player2, ManaColor.BLUE, 2 + replicatePayments.size());
        harness.passPriority(player1);
        harness.castInstantWithRepeatedCosts(player2, 0, target.getId(), replicatePayments);
    }
}
