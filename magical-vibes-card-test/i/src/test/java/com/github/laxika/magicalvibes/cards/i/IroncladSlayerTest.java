package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.CatharsShield;
import com.github.laxika.magicalvibes.cards.l.LunarchMantle;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IroncladSlayer.class, LunarchMantle.class, CatharsShield.class})
class IroncladSlayerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns a targeted Aura card from the graveyard to hand")
    void returnsAuraFromGraveyardToHand() {
        LunarchMantle mantle = new LunarchMantle();
        harness.setGraveyard(player1, List.of(mantle));

        castIroncladSlayer();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(mantle.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Lunarch Mantle");
        harness.assertNotInGraveyard(player1, "Lunarch Mantle");
    }

    @Test
    @DisplayName("ETB returns a targeted Equipment card from the graveyard to hand")
    void returnsEquipmentFromGraveyardToHand() {
        CatharsShield shield = new CatharsShield();
        harness.setGraveyard(player1, List.of(shield));

        castIroncladSlayer();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(shield.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Cathar's Shield");
        harness.assertNotInGraveyard(player1, "Cathar's Shield");
    }

    @Test
    @DisplayName("ETB cannot target a non-Aura, non-Equipment card")
    void doesNotTargetOtherCards() {
        harness.setGraveyard(player1, List.of(new IroncladSlayer()));

        castIroncladSlayer();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Ironclad Slayer");
    }

    @Test
    @DisplayName("ETB may decline to return a card")
    void mayDeclineToReturnCard() {
        LunarchMantle mantle = new LunarchMantle();
        harness.setGraveyard(player1, List.of(mantle));

        castIroncladSlayer();

        harness.handleMultipleCardsChosen(player1, List.of(mantle.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Lunarch Mantle");
        harness.assertNotInHand(player1, "Lunarch Mantle");
    }

    @Test
    @DisplayName("A legal target is required even when the return may be declined")
    void requiresTargetBeforeResolution() {
        harness.setGraveyard(player1, List.of(new LunarchMantle()));

        castIroncladSlayer();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB cannot return an Aura from an opponent's graveyard")
    void doesNotTargetOpponentsGraveyard() {
        harness.setGraveyard(player2, List.of(new LunarchMantle()));

        castIroncladSlayer();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Lunarch Mantle");
        harness.assertNotInHand(player1, "Lunarch Mantle");
    }

    @Test
    @DisplayName("ETB does not substitute another card when its target leaves the graveyard")
    void doesNotReturnAnotherCardWhenTargetLeaves() {
        LunarchMantle mantle = new LunarchMantle();
        CatharsShield shield = new CatharsShield();
        harness.setGraveyard(player1, List.of(mantle, shield));

        castIroncladSlayer();
        harness.handleMultipleCardsChosen(player1, List.of(mantle.getId()));
        harness.setGraveyard(player1, List.of(shield));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Cathar's Shield");
        harness.assertNotInHand(player1, "Lunarch Mantle");
        harness.assertNotInHand(player1, "Cathar's Shield");
    }

    private void castIroncladSlayer() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new IroncladSlayer(), "{2}{W}");
        harness.passBothPriorities();
    }
}
