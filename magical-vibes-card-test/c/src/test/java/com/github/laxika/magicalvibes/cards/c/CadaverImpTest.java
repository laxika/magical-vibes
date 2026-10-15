package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DaggerbackBasilisk;
import com.github.laxika.magicalvibes.cards.f.FlameSlash;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CadaverImp.class, DaggerbackBasilisk.class, FlameSlash.class})
class CadaverImpTest extends BaseCardTest {

    private void castCadaverImp() {
        harness.castFromHand(player1, new CadaverImp(), "{1}{B}{B}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns a chosen creature card from its controller's graveyard to hand")
    void returnsChosenCreatureFromOwnGraveyard() {
        Card card = new DaggerbackBasilisk();
        harness.setGraveyard(player1, List.of(card));

        castCadaverImp();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(card.getId());

        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Daggerback Basilisk");
        harness.assertNotInGraveyard(player1, "Daggerback Basilisk");
    }

    @Test
    @DisplayName("Declining the optional return leaves the creature card in the graveyard")
    void decliningReturnsNothing() {
        Card card = new DaggerbackBasilisk();
        harness.setGraveyard(player1, List.of(card));

        castCadaverImp();

        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Daggerback Basilisk");
        harness.assertNotInHand(player1, "Daggerback Basilisk");
    }

    @Test
    @DisplayName("ETB does not target noncreature cards")
    void nonCreatureIsNotTargetable() {
        Card card = new FlameSlash();
        harness.setGraveyard(player1, List.of(card));

        castCadaverImp();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Flame Slash");
    }

    @Test
    @DisplayName("ETB does not target cards in an opponent's graveyard")
    void onlyTargetsOwnGraveyard() {
        Card card = new DaggerbackBasilisk();
        harness.setGraveyard(player2, List.of(card));

        castCadaverImp();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player2, "Daggerback Basilisk");
    }

    @Test
    @DisplayName("ETB with an empty graveyard requires no choice")
    void emptyGraveyardRequiresNoChoice() {
        harness.setGraveyard(player1, List.of());

        castCadaverImp();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Cadaver Imp");
    }

    @Test
    @DisplayName("Only the chosen creature returns from a mixed graveyard")
    void returnsOnlyChosenCreature() {
        Card chosen = new DaggerbackBasilisk();
        Card other = new CadaverImp();
        Card nonCreature = new FlameSlash();
        Card opposing = new DaggerbackBasilisk();
        harness.setGraveyard(player1, List.of(chosen, other, nonCreature));
        harness.setGraveyard(player2, List.of(opposing));

        castCadaverImp();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(chosen.getId(), other.getId());

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other, nonCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposing);
    }

    @Test
    @DisplayName("A target that leaves the graveyard makes the trigger fail without choosing a new target")
    void removedTargetDoesNotReturnAnotherCreature() {
        Card target = new DaggerbackBasilisk();
        Card other = new CadaverImp();
        harness.setGraveyard(player1, List.of(target, other));

        castCadaverImp();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(other));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
    }
}
