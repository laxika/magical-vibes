package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.r.ReachThroughMists;
import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.cards.y.YamabushisFlame;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({BlessedBreath.class, WanderingOnes.class, ReachThroughMists.class, YamabushisFlame.class})
class BlessedBreathTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature you control gains protection from the chosen color")
    void grantsProtectionFromChosenColor() {
        harness.addToBattlefield(player1, new WanderingOnes());
        harness.setHand(player1, List.of(new BlessedBreath()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Wandering Ones"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();

        harness.handleListChoice(player1, "RED");

        Permanent wanderingOnes = findPermanent(player1, "Wandering Ones");
        assertThat(wanderingOnes.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new WanderingOnes());
        harness.setHand(player1, List.of(new BlessedBreath()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Wandering Ones")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Splices onto an Arcane spell and leaves Blessed Breath in hand")
    void splicesOntoArcaneSpell() {
        harness.addToBattlefield(player1, new WanderingOnes());
        harness.setHand(player1, List.of(new ReachThroughMists(), new BlessedBreath()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        var targetId = harness.getPermanentId(player1, "Wandering Ones");
        harness.castWithSplice(player1, 0, targetId, List.of(1));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        Permanent wanderingOnes = findPermanent(player1, "Wandering Ones");
        assertThat(wanderingOnes.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLUE);
        harness.assertInHand(player1, "Blessed Breath");
    }

    @Test
    @DisplayName("Protection from red prevents a red spell from targeting the creature")
    void protectionPreventsRedSpellFromTargeting() {
        harness.addToBattlefield(player1, new WanderingOnes());
        harness.setHand(player1, List.of(new BlessedBreath()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        var targetId = harness.getPermanentId(player1, "Wandering Ones");
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.handleListChoice(player1, "RED");

        harness.setHand(player2, List.of(new YamabushisFlame()));
        harness.addMana(player2, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Protection lasts through the end step and expires during cleanup")
    void protectionExpiresDuringCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WanderingOnes());
        harness.setHand(player1, List.of(new BlessedBreath()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleListChoice(player1, "WHITE");

        harness.forceStep(TurnStep.END_STEP);
        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.WHITE);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }

    @Test
    @DisplayName("Protection makes an already cast red damage spell lose its target")
    void protectionInvalidatesPendingDamageSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WanderingOnes());
        harness.setHand(player2, List.of(new YamabushisFlame()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castInstant(player2, 0, target.getId());

        harness.setHand(player1, List.of(new BlessedBreath()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handleListChoice(player1, "RED");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wandering Ones");
        harness.assertInGraveyard(player2, "Yamabushi's Flame");
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Spliced protection still requires a creature you control")
    void spliceCannotTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WanderingOnes());
        harness.setHand(player1, List.of(new ReachThroughMists(), new BlessedBreath()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, target.getId(), List.of(1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Splice requires its additional white mana payment")
    void spliceRequiresWhiteMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WanderingOnes());
        harness.setHand(player1, List.of(new ReachThroughMists(), new BlessedBreath()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, target.getId(), List.of(1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot splice Blessed Breath onto a non-Arcane spell")
    void cannotSpliceOntoNonArcaneSpell() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WanderingOnes());
        harness.setHand(player1, List.of(new YamabushisFlame(), new BlessedBreath()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, target.getId(), List.of(1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The host spell does not draw when its only spliced target becomes illegal")
    void illegalSplicedTargetStopsHostSpellFromResolving() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WanderingOnes());
        WanderingOnes libraryCard = new WanderingOnes();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new ReachThroughMists(), new BlessedBreath()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castWithSplice(player1, 0, target.getId(), List.of(1));

        harness.setHand(player2, List.of(new YamabushisFlame()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wandering Ones");
        harness.assertInGraveyard(player1, "Reach Through Mists");
        harness.assertInHand(player1, "Blessed Breath");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
