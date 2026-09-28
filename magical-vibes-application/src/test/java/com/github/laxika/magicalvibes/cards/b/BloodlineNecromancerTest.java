package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodlineNecromancer.class, BloodthroneVampire.class, FugitiveWizard.class, GrizzlyBears.class})
class BloodlineNecromancerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB targets a Vampire or Wizard creature card and returns it to the battlefield")
    void returnsTargetVampireOrWizardToBattlefield() {
        Card vampire = new BloodthroneVampire();
        Card wizard = new FugitiveWizard();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(vampire, wizard, bears));

        castBloodlineNecromancer();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(vampire.getId(), wizard.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card");

        harness.handleMultipleCardsChosen(player1, List.of(wizard.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Fugitive Wizard");
        harness.assertInGraveyard(player1, "Bloodthrone Vampire");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the optional ETB return leaves the target in the graveyard")
    void decliningReturnLeavesTargetInGraveyard() {
        Card vampire = new BloodthroneVampire();
        harness.setGraveyard(player1, List.of(vampire));

        castBloodlineNecromancer();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(vampire.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Bloodline Necromancer");
        harness.assertInGraveyard(player1, "Bloodthrone Vampire");
    }

    @Test
    @DisplayName("ETB does not target a non-Vampire or non-Wizard creature")
    void doesNotTargetOtherCreature() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        castBloodlineNecromancer();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void castBloodlineNecromancer() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BloodlineNecromancer()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
    }
}
