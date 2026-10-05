package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RagavanNimblePilferer.class, GrizzlyBears.class, Forest.class})
class RagavanNimblePilfererTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage creates a Treasure and grants cast permission for the damaged player's top card")
    void combatDamageCreatesTreasureAndGrantsCastPermission() {
        addAttackingRagavan();
        Card topCard = new GrizzlyBears();
        topCard.setOwnerId(player2.getId());
        harness.setLibrary(player2, List.of(topCard, new Forest()));

        resolveCombatAndTrigger();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
        assertThat(gd.exilePlayAnyManaType).doesNotContain(topCard.getId());
    }

    @Test
    @DisplayName("The exiled nonland card can be cast until end of turn")
    void castsExiledNonlandCard() {
        addAttackingRagavan();
        Card topCard = new GrizzlyBears();
        topCard.setOwnerId(player2.getId());
        harness.setLibrary(player2, List.of(topCard, new Forest()));

        resolveCombatAndTrigger();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("A land exiled by the trigger does not receive cast or land-play permission")
    void doesNotGrantLandPermission() {
        addAttackingRagavan();
        Card topCard = new Forest();
        topCard.setOwnerId(player2.getId());
        harness.setLibrary(player2, List.of(topCard));

        resolveCombatAndTrigger();

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Dash grants haste and returns Ragavan to its owner's hand at end step")
    void dashGrantsHasteAndReturnsToHand() {
        harness.setHand(player1, List.of(new RagavanNimblePilferer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        Permanent ragavan = findPermanent(player1, "Ragavan, Nimble Pilferer");
        assertThat(ragavan.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertInHand(player1, "Ragavan, Nimble Pilferer");
        harness.assertNotOnBattlefield(player1, "Ragavan, Nimble Pilferer");
    }

    @Test
    @DisplayName("Combat damage still creates a Treasure when the damaged player's library is empty")
    void emptyLibraryStillCreatesTreasure() {
        addAttackingRagavan();
        harness.setLibrary(player2, List.of());

        resolveCombatAndTrigger();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gd.exilePlayPermissions).isEmpty();
    }

    @Test
    @DisplayName("Casting Ragavan normally does not grant dash haste or return it at end step")
    void normalCastDoesNotApplyDash() {
        harness.setHand(player1, List.of(new RagavanNimblePilferer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent ragavan = findPermanent(player1, "Ragavan, Nimble Pilferer");
        assertThat(ragavan.hasKeyword(Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Ragavan, Nimble Pilferer");
        harness.assertNotInHand(player1, "Ragavan, Nimble Pilferer");
    }

    @Test
    @DisplayName("Dash does not create an enters-the-battlefield triggered ability")
    void dashDoesNotCreateEtbTrigger() {
        harness.setHand(player1, List.of(new RagavanNimblePilferer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.castWithAlternateCost(player1, 0, List.of());
            harness.passBothPriorities();
        });

        harness.assertOnBattlefield(player1, "Ragavan, Nimble Pilferer");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Dash creates exactly one return trigger at the beginning of the next end step")
    void dashCreatesOnlyOneEndStepReturnTrigger() {
        harness.setHand(player1, List.of(new RagavanNimblePilferer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Ragavan, Nimble Pilferer");
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertInHand(player1, "Ragavan, Nimble Pilferer");
    }

    @Test
    @DisplayName("The exiled card stays in exile after its casting permission expires")
    void castingPermissionExpiresAtEndOfTurn() {
        addAttackingRagavan();
        Card topCard = new GrizzlyBears();
        topCard.setOwnerId(player2.getId());
        harness.setLibrary(player2, List.of(topCard, new Forest()));
        resolveCombatAndTrigger();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(topCard.getId());
    }
    private Permanent addAttackingRagavan() {
        Permanent ragavan = addCreatureReady(player1, new RagavanNimblePilferer());
        ragavan.setAttacking(true);
        ragavan.setAttackTarget(player2.getId());
        return ragavan;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
