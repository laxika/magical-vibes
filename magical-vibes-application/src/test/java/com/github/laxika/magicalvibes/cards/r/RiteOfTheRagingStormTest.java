package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DarettiScrapSavant;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Rite of the Raging Storm")
@CardUsed({RiteOfTheRagingStorm.class, DarettiScrapSavant.class})
class RiteOfTheRagingStormTest extends BaseCardTest {

    @Test
    @DisplayName("Each player's upkeep creates a Lightning Rager for that player")
    void createsLightningRagerForActivePlayer() {
        harness.addToBattlefield(player1, new RiteOfTheRagingStorm());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        Permanent rager = findPermanent(player2, "Lightning Rager");
        assertThat(rager.getCard().getPower()).isEqualTo(5);
        assertThat(rager.getCard().getToughness()).isEqualTo(1);
        assertThat(rager.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(rager.getCard().getSubtypes()).containsExactly(CardSubtype.ELEMENTAL);
        assertThat(rager.getCard().getKeywords()).containsExactlyInAnyOrder(Keyword.TRAMPLE, Keyword.HASTE);
    }

    @Test
    @DisplayName("Lightning Ragers can't attack the enchantment's controller")
    void lightningRagerCannotAttackController() {
        harness.addToBattlefield(player1, new RiteOfTheRagingStorm());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        Permanent rager = findPermanent(player2, "Lightning Rager");

        assertThatThrownBy(() -> declareAttackers(player2, List.of(
                gd.playerBattlefields.get(player2.getId()).indexOf(rager))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Lightning Ragers can't attack the enchantment controller's planeswalker")
    void lightningRagerCannotAttackControllerPlaneswalker() {
        harness.addToBattlefield(player1, new RiteOfTheRagingStorm());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new DarettiScrapSavant());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        Permanent rager = findPermanent(player2, "Lightning Rager");

        int ragerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(rager);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(ragerIndex), Map.of(0, planeswalker.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("The Lightning Rager is sacrificed at the beginning of the next end step")
    void lightningRagerIsSacrificedAtNextEndStep() {
        harness.addToBattlefield(player1, new RiteOfTheRagingStorm());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Lightning Rager");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Lightning Rager");
    }

    @Test
    @DisplayName("The controller's token can attack immediately and deal five damage")
    void controllerTokenCanAttackOpponentImmediately() {
        harness.addToBattlefield(player1, new RiteOfTheRagingStorm());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Lightning Rager")).isEqualTo(1);
        assertThat(countPermanents(player2, "Lightning Rager")).isZero();
        Permanent rager = findPermanent(player1, "Lightning Rager");
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(rager)));
        resolveCombat(player1);

        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Each copy creates a separate token for the active player")
    void multipleCopiesCreateMultipleTokens() {
        harness.addToBattlefield(player1, new RiteOfTheRagingStorm());
        harness.addToBattlefield(player2, new RiteOfTheRagingStorm());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Lightning Rager")).isEqualTo(2);
        assertThat(countPermanents(player1, "Lightning Rager")).isZero();
    }

    @Test
    @DisplayName("An upkeep trigger still creates its token after the enchantment leaves")
    void pendingTriggerSurvivesSourceLeaving() {
        Permanent rite = harness.addToBattlefieldAndReturn(player1, new RiteOfTheRagingStorm());

        advanceToUpkeep(player2);
        gd.playerBattlefields.get(player1.getId()).remove(rite);
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Lightning Rager")).isEqualTo(1);
        assertThat(countPermanents(player1, "Lightning Rager")).isZero();
    }

    @Test
    @DisplayName("A token can attack after Rite leaves but still sacrifices on either player's end step")
    void tokenAbilitiesPersistWithoutEnchantment() {
        Permanent rite = harness.addToBattlefieldAndReturn(player1, new RiteOfTheRagingStorm());
        advanceToUpkeep(player2);
        resolveAllTriggers();
        Permanent rager = findPermanent(player2, "Lightning Rager");
        gd.playerBattlefields.get(player1.getId()).remove(rite);

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(rager)));
        resolveCombat(player2);
        harness.assertLife(player1, 15);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Lightning Rager");
    }
}
