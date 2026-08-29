package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FormOfTheDinosaurTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield sets its controller's life total to 15")
    void enteringSetsLifeTotalToFifteen() {
        harness.setLife(player1, 4);
        harness.setHand(player1, List.of(new FormOfTheDinosaur()));
        addManaForForm(player1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("Upkeep trigger damages the target creature and then its controller")
    void upkeepDamagesTargetAndController() {
        harness.addToBattlefield(player1, new FormOfTheDinosaur());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gqs.findPermanentById(gd, target.getId())).isNull();
    }

    @Test
    @DisplayName("Upkeep trigger only offers creatures controlled by an opponent")
    void upkeepTargetIsOpponentCreature() {
        harness.addToBattlefield(player1, new FormOfTheDinosaur());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(opponentCreature.getId())
                .doesNotContain(ownCreature.getId());
    }

    @Test
    @DisplayName("A target that leaves before resolution deals no damage to the controller")
    void targetLeavingBeforeResolutionDealsNoDamage() {
        harness.addToBattlefield(player1, new FormOfTheDinosaur());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, target));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
    }

    private void addManaForForm(Player player) {
        harness.addMana(player, com.github.laxika.magicalvibes.model.ManaColor.RED, 2);
        harness.addMana(player, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 4);
    }
}
