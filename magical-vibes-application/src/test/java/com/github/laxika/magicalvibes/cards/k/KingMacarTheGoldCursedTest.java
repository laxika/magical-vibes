package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KingMacarTheGoldCursedTest extends BaseCardTest {

    @Test
    @DisplayName("Untapping King Macar may exile a target creature and create a Gold token")
    void untappingExilesCreatureAndCreatesGoldToken() {
        Permanent kingMacar = addTappedKingMacar();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToKingMacarTrigger();

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Gold")).hasSize(1);
        Permanent gold = findPermanent(player1, "Gold");
        assertThat(gold.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(gold.getCard().getActivatedAbilities()).hasSize(1);
    }

    @Test
    @DisplayName("Declining King Macar's inspired ability leaves the target and creates no Gold")
    void decliningInspiredAbilityDoesNothing() {
        addTappedKingMacar();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToKingMacarTrigger();

        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Gold")).isEmpty();
    }

    @Test
    @DisplayName("King Macar can target creatures but not lands")
    void targetChoiceOnlyOffersCreatures() {
        addTappedKingMacar();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToKingMacarTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).contains(bears.getId()).doesNotContain(forest.getId());
    }

    private Permanent addTappedKingMacar() {
        Permanent kingMacar = harness.addToBattlefieldAndReturn(player1, new KingMacarTheGoldCursed());
        kingMacar.setSummoningSick(false);
        kingMacar.tap();
        return kingMacar;
    }

    private void advanceToKingMacarTrigger() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }
}
