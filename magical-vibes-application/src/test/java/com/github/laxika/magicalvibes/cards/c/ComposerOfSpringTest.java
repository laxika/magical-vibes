package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ComposerOfSpring.class, Forest.class, GloriousAnthem.class, GrizzlyBears.class})
class ComposerOfSpringTest extends BaseCardTest {

    @Test
    @DisplayName("Below six enchantments, Composer of Spring only puts a tapped land from hand")
    void belowSixEnchantmentsOnlyPutsLand() {
        harness.addToBattlefield(player1, new ComposerOfSpring());
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, new GloriousAnthem());
        castTriggeringEnchantment();
        harness.setHand(player1, List.of(forest, bears));

        resolveTrigger(true);
        harness.handleCardChosen(player1, 0);

        Permanent enteredLand = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == forest)
                .findFirst()
                .orElseThrow();
        assertThat(enteredLand.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
    }

    @Test
    @DisplayName("With six enchantments, Composer of Spring can put a creature from hand")
    void sixEnchantmentsAlsoPutsCreature() {
        harness.addToBattlefield(player1, new ComposerOfSpring());
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new GloriousAnthem());
        }
        GrizzlyBears bears = new GrizzlyBears();
        castTriggeringEnchantment();
        harness.setHand(player1, List.of(bears));

        resolveTrigger(true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard() == bears && permanent.isTapped());
    }

    @Test
    @DisplayName("Declining Composer of Spring's trigger leaves the hand unchanged")
    void decliningLeavesHandUnchanged() {
        harness.addToBattlefield(player1, new ComposerOfSpring());
        Forest forest = new Forest();
        castTriggeringEnchantment();
        harness.setHand(player1, List.of(forest));

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == forest);
    }

    @Test
    void sixEnchantmentsStillAllowsLandAndOnlyOneCard() {
        harness.addToBattlefield(player1, new ComposerOfSpring());
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new GloriousAnthem());
        }
        castTriggeringEnchantment();
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(forest, bears));

        resolveTrigger(true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard() == forest && permanent.isTapped());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void reachingSixAfterTriggeringAllowsCreature() {
        harness.addToBattlefield(player1, new ComposerOfSpring());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new GloriousAnthem());
        }
        castTriggeringEnchantment();
        harness.addToBattlefield(player1, new GloriousAnthem());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));

        resolveTrigger(true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard() == bears && permanent.isTapped());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void fallingBelowSixBeforeResolutionOnlyAllowsLand() {
        harness.addToBattlefield(player1, new ComposerOfSpring());
        Permanent removedEnchantment = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new GloriousAnthem());
        }
        castTriggeringEnchantment();
        gd.playerBattlefields.get(player1.getId()).remove(removedEnchantment);
        gd.playerGraveyards.get(player1.getId()).add(removedEnchantment.getCard());
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(forest, bears));

        resolveTrigger(true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard() == forest && permanent.isTapped());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsEnchantmentDoesNotTrigger() {
        harness.addToBattlefield(player1, new ComposerOfSpring());
        harness.enterBattlefieldAndReturn(player2, new GloriousAnthem());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void decliningAtSixDoesNotOfferAnotherLandChoice() {
        harness.addToBattlefield(player1, new ComposerOfSpring());
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new GloriousAnthem());
        }
        castTriggeringEnchantment();
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(forest, bears));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest, bears);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void triggerStillResolvesAfterComposerLeavesBattlefield() {
        Permanent composer = harness.addToBattlefieldAndReturn(player1, new ComposerOfSpring());
        castTriggeringEnchantment();
        gd.playerBattlefields.get(player1.getId()).remove(composer);
        gd.playerGraveyards.get(player1.getId()).add(composer.getCard());
        Forest forest = new Forest();
        harness.setHand(player1, List.of(forest));

        resolveTrigger(true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard() == forest && permanent.isTapped());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void opponentsEnchantmentsDoNotEnableCreatureChoice() {
        harness.addToBattlefield(player1, new ComposerOfSpring());
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player2, new GloriousAnthem());
        }
        castTriggeringEnchantment();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));

        resolveTrigger(true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void resolveTrigger(boolean accept) {
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, accept);
        harness.passBothPriorities();
    }

    private void castTriggeringEnchantment() {
        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();
    }
}
