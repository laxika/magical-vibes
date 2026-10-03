package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AnotherChance;
import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MaskwoodNexus;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CorpsesOfTheLost.class, AnotherChance.class, DrudgeSkeletons.class, Forest.class,
        GrizzlyBears.class, MaskwoodNexus.class, Opalescence.class, ZuranOrb.class})
class CorpsesOfTheLostTest extends BaseCardTest {

    @Test
    @DisplayName("Skeletons you control get +1/+0 and haste")
    void buffsOwnSkeletons() {
        harness.addToBattlefield(player1, new CorpsesOfTheLost());
        Permanent skeleton = harness.addToBattlefieldAndReturn(player1, new DrudgeSkeletons());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingSkeleton = harness.addToBattlefieldAndReturn(player2, new DrudgeSkeletons());

        assertThat(gqs.getEffectivePower(gd, skeleton)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, skeleton)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, skeleton, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opposingSkeleton)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, opposingSkeleton, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Entering creates a 2/2 black Skeleton Pirate token")
    void enteringCreatesSkeletonPirateToken() {
        castCorpsesOfTheLost();

        Permanent token = findPermanent(player1, "Skeleton Pirate");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.SKELETON, CardSubtype.PIRATE);
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("After descending, paying 1 life returns Corpses of the Lost to hand")
    void descendedEndStepMayReturnToHand() {
        Permanent corpses = castCorpsesOfTheLost();
        descendThisTurn();

        advanceToEndStep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        int lifeBefore = gd.getLife(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(corpses.getCard().getId()));
        harness.assertNotOnBattlefield(player1, "Corpses of the Lost");
    }

    @Test
    @DisplayName("Declining the descended end-step payment keeps Corpses of the Lost on the battlefield")
    void decliningEndStepPaymentKeepsEnchantment() {
        Permanent corpses = castCorpsesOfTheLost();
        descendThisTurn();

        advanceToEndStep(player1);
        harness.passBothPriorities();
        int lifeBefore = gd.getLife(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(corpses);
        harness.assertNotInHand(player1, "Corpses of the Lost");
    }

    @Test
    @DisplayName("The end-step ability does not trigger without descending")
    void doesNotTriggerWithoutDescending() {
        castCorpsesOfTheLost();

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Corpses of the Lost");
    }

    @Test
    @DisplayName("Returning the enchantment leaves its token behind without the boost or haste")
    void returningEnchantmentRemovesBuffButKeepsToken() {
        castCorpsesOfTheLost();
        Permanent token = findPermanent(player1, "Skeleton Pirate");
        descendThisTurn();

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Multiple copies stack their power bonuses")
    void multipleCopiesStackPowerBonuses() {
        castCorpsesOfTheLost();
        Permanent token = findPermanent(player1, "Skeleton Pirate");
        harness.addToBattlefield(player1, new CorpsesOfTheLost());

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Descending does not trigger the ability during an opponent's end step")
    void doesNotTriggerOnOpponentsEndStep() {
        castCorpsesOfTheLost();
        harness.forceActivePlayer(player2);
        descendThisTurn();

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Corpses of the Lost");
    }

    @Test
    @DisplayName("An opponent descending does not satisfy your end-step condition")
    void opponentsDescentDoesNotEnableReturn() {
        castCorpsesOfTheLost();
        descendThisTurn(player2);

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Corpses of the Lost");
    }

    @Test
    @DisplayName("Milling a permanent card enables the end-step return")
    void millingPermanentEnablesReturn() {
        castCorpsesOfTheLost();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        castAnotherChanceAndMill();

        advanceToEndStep(player1);
        harness.passBothPriorities();
        int lifeBefore = gd.getLife(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        harness.assertInHand(player1, "Corpses of the Lost");
        harness.assertNotOnBattlefield(player1, "Corpses of the Lost");
    }

    @Test
    @DisplayName("Milling only instants does not count as descending")
    void millingNonpermanentsDoesNotEnableReturn() {
        castCorpsesOfTheLost();
        harness.setLibrary(player1, List.of(new AnotherChance(), new AnotherChance()));
        castAnotherChanceAndMill();

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Corpses of the Lost");
    }

    @Test
    @CardUsed({CorpsesOfTheLost.class, MaskwoodNexus.class, Opalescence.class})
    @DisplayName("An animated Corpses of the Lost that is a Skeleton receives its own bonus and haste")
    void animatedSkeletonEnchantmentBuffsItself() {
        harness.addToBattlefield(player1, new Opalescence());
        harness.addToBattlefield(player1, new MaskwoodNexus());
        Permanent corpses = harness.addToBattlefieldAndReturn(player1, new CorpsesOfTheLost());

        assertThat(gqs.getEffectivePower(gd, corpses)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, corpses)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, corpses, Keyword.HASTE)).isTrue();
    }

    private void castAnotherChanceAndMill() {
        harness.setHand(player1, List.of(new AnotherChance()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent castCorpsesOfTheLost() {
        harness.setHand(player1, List.of(new CorpsesOfTheLost()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Corpses of the Lost");
    }

    private void descendThisTurn() {
        descendThisTurn(player1);
    }

    private void descendThisTurn(Player player) {
        harness.addToBattlefield(player, new ZuranOrb());
        harness.addToBattlefield(player, new Forest());
        int orbIndex = gd.playerBattlefields.get(player.getId()).indexOf(findPermanent(player, "Zuran Orb"));
        harness.activateAbility(player, orbIndex, null, null);
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
