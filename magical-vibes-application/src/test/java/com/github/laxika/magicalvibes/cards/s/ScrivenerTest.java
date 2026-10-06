package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AboshanCephalidEmperor;
import com.github.laxika.magicalvibes.cards.a.AetherBurst;
import com.github.laxika.magicalvibes.cards.f.FerventDenial;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Scrivener.class, AetherBurst.class, FerventDenial.class, AboshanCephalidEmperor.class})
class ScrivenerTest extends BaseCardTest {

    private void castScrivener() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Scrivener(), "{4}{U}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns the chosen instant card from the graveyard")
    void returnsChosenInstantToHand() {
        AetherBurst aetherBurst = new AetherBurst();
        FerventDenial ferventDenial = new FerventDenial();
        AboshanCephalidEmperor aboshan = new AboshanCephalidEmperor();
        harness.setGraveyard(player1, List.of(aboshan, aetherBurst, ferventDenial));

        castScrivener();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(aetherBurst.getId(), ferventDenial.getId());

        harness.handleMultipleCardsChosen(player1, List.of(ferventDenial.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Fervent Denial");
        harness.assertInGraveyard(player1, "Aether Burst");
        harness.assertInGraveyard(player1, "Aboshan, Cephalid Emperor");
    }

    @Test
    @DisplayName("The optional return may be declined")
    void returnMayBeDeclined() {
        FerventDenial ferventDenial = new FerventDenial();
        harness.setGraveyard(player1, List.of(ferventDenial));

        castScrivener();

        harness.handleMultipleCardsChosen(player1, List.of(ferventDenial.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Fervent Denial");
        harness.assertNotInHand(player1, "Fervent Denial");
    }

    @Test
    @DisplayName("A non-instant card is not a legal target")
    void nonInstantIsNotTargetable() {
        harness.setGraveyard(player1, List.of(new AboshanCephalidEmperor()));

        castScrivener();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Aboshan, Cephalid Emperor");
    }

    @Test
    @DisplayName("An empty graveyard produces no target choice")
    void emptyGraveyardProducesNoChoice() {
        castScrivener();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("An instant in an opponent's graveyard is not a legal target")
    void opponentGraveyardIsNotTargetable() {
        harness.setGraveyard(player2, List.of(new FerventDenial()));

        castScrivener();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Fervent Denial");
    }

    @Test
    @DisplayName("A targeted instant that leaves the graveyard is not returned")
    void targetLeavingGraveyardIsNotReturned() {
        FerventDenial ferventDenial = new FerventDenial();
        harness.setGraveyard(player1, List.of(ferventDenial));

        castScrivener();

        harness.handleMultipleCardsChosen(player1, List.of(ferventDenial.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Fervent Denial");
    }

    @Test
    @DisplayName("The return resolves even if Scrivener leaves the battlefield")
    void returnsInstantAfterSourceLeavesBattlefield() {
        FerventDenial ferventDenial = new FerventDenial();
        harness.setGraveyard(player1, List.of(ferventDenial));

        castScrivener();
        harness.handleMultipleCardsChosen(player1, List.of(ferventDenial.getId()));

        harness.setHand(player1, List.of(new AetherBurst()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0,
                List.of(harness.getPermanentId(player1, "Scrivener")));
        harness.assertNotOnBattlefield(player1, "Scrivener");
        harness.assertInHand(player1, "Scrivener");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Fervent Denial");
        harness.assertNotInGraveyard(player1, "Fervent Denial");
        harness.assertInGraveyard(player1, "Aether Burst");
    }

    @Test
    @DisplayName("Entering without being cast returns an instant from the entering controller's graveyard")
    void enteringWithoutCastingUsesControllersGraveyard() {
        AetherBurst ownInstant = new AetherBurst();
        FerventDenial opponentInstant = new FerventDenial();
        harness.setGraveyard(player2, List.of(ownInstant));
        harness.setGraveyard(player1, List.of(opponentInstant));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.enterBattlefieldAndReturn(player2, new Scrivener());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownInstant.getId());
        harness.handleMultipleCardsChosen(player2, List.of(ownInstant.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInHand(player2, "Aether Burst");
        harness.assertNotInGraveyard(player2, "Aether Burst");
        harness.assertInGraveyard(player1, "Fervent Denial");
        harness.assertNotInHand(player1, "Fervent Denial");
    }
}
