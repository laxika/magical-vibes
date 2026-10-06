package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.ArtificialEvolution;
import com.github.laxika.magicalvibes.cards.f.FavorOfTheMighty;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OneEyedScarecrow;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReaperKing.class, OneEyedScarecrow.class, GrizzlyBears.class,
        RattleblazeScarecrow.class, Forest.class, ArtificialEvolution.class, FavorOfTheMighty.class})
class ReaperKingTest extends BaseCardTest {

    @Test
    @DisplayName("Other Scarecrows you control get +1/+1, but Reaper King itself does not")
    void boostsOtherScarecrows() {
        Permanent reaperKing = harness.addToBattlefieldAndReturn(player1, new ReaperKing());
        Permanent scarecrow = harness.addToBattlefieldAndReturn(player1, new OneEyedScarecrow());

        // One-Eyed Scarecrow is a 2/3 → 3/4 with Reaper King's anthem.
        assertThat(gqs.getEffectivePower(gd, scarecrow)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, scarecrow)).isEqualTo(4);

        // Reaper King is a 6/6 and does not boost itself ("Other Scarecrow creatures").
        assertThat(gqs.getEffectivePower(gd, reaperKing)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, reaperKing)).isEqualTo(6);
    }

    @Test
    @DisplayName("A Scarecrow entering queues the destroy trigger for target selection")
    void scarecrowEnterQueuesTargetSelection() {
        harness.addToBattlefieldAndReturn(player1, new ReaperKing());
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castScarecrow(player1);
        harness.passBothPriorities(); // resolve the creature spell (Scarecrow enters, trigger fires)

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);
    }

    @Test
    @DisplayName("Resolving the trigger destroys the chosen permanent")
    void scarecrowEnterDestroysChosenPermanent() {
        harness.addToBattlefieldAndReturn(player1, new ReaperKing());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castScarecrow(player1);
        harness.passBothPriorities(); // resolve the creature spell → trigger awaits target

        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities(); // resolve the destroy ability

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(victim.getId()));
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A non-Scarecrow entering does not trigger the destroy ability")
    void nonScarecrowEnterDoesNotTrigger() {
        harness.addToBattlefieldAndReturn(player1, new ReaperKing());
        Permanent bystander = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        // Cast a Bear (not a Scarecrow) under player1's control.
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities(); // resolve the creature spell

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(bystander.getId()));
    }

    @Test
    @DisplayName("The trigger cannot target a player (destroys a permanent only)")
    void triggerCannotTargetPlayer() {
        harness.addToBattlefieldAndReturn(player1, new ReaperKing());
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castScarecrow(player1);
        harness.passBothPriorities(); // resolve the creature spell → trigger awaits target

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castScarecrow(com.github.laxika.magicalvibes.model.Player player) {
        harness.castFromHand(player, new OneEyedScarecrow(), "{3}");
    }

    @Test
    void doesNotBoostOpposingScarecrowsOrOtherCreatureTypes() {
        harness.addToBattlefield(player1, new ReaperKing());
        Permanent opposingScarecrow = harness.addToBattlefieldAndReturn(player2, new RattleblazeScarecrow());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, opposingScarecrow)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, opposingScarecrow)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    void ownEntryDoesNotTriggerDestruction() {
        harness.enterBattlefieldAndReturn(player1, new ReaperKing());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opposingScarecrowEntryDoesNotTriggerDestruction() {
        harness.addToBattlefield(player1, new ReaperKing());
        harness.enterBattlefieldAndReturn(player2, new RattleblazeScarecrow());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggerCanDestroyALand() {
        harness.addToBattlefield(player1, new ReaperKing());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.castFromHand(player1, new RattleblazeScarecrow(), "{6}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void triggerCanDestroyReaperKingAndRemoveItsAnthem() {
        Permanent king = harness.addToBattlefieldAndReturn(player1, new ReaperKing());
        harness.castFromHand(player1, new RattleblazeScarecrow(), "{6}");
        harness.passBothPriorities();
        Permanent scarecrow = findPermanent(player1, "Rattleblaze Scarecrow");
        assertThat(gqs.getEffectivePower(gd, scarecrow)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, scarecrow)).isEqualTo(4);

        harness.handlePermanentChosen(player1, king.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Reaper King");
        assertThat(gqs.getEffectivePower(gd, scarecrow)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, scarecrow)).isEqualTo(3);
    }

    @Test
    void noncreatureScarecrowEntryTriggersDestruction() {
        harness.addToBattlefield(player1, new ReaperKing());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.castFromHand(player1, new FavorOfTheMighty(), "{1}{W}");
        UUID spellId = gd.stack.getFirst().getCard().getId();
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, spellId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GIANT");
        harness.handleListChoice(player1, "SCARECROW");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Forest");
    }
}
