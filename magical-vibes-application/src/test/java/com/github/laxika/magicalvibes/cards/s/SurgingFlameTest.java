package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FrostRaptor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SurgingFlame.class, FrostRaptor.class})
class SurgingFlameTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to any target")
    void dealsDamageToAnyTarget() {
        castSurgingFlame(player2.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals 2 damage to a creature")
    void dealsDamageToCreature() {
        harness.addToBattlefield(player2, new FrostRaptor());
        UUID targetId = harness.getPermanentId(player2, "Frost Raptor");

        castSurgingFlame(targetId);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Frost Raptor");
    }

    @Test
    @DisplayName("Ripple offers revealed cards with the same name")
    void rippleOffersSameNameCards() {
        prepareCaster(List.of(
                new FrostRaptor(),
                new SurgingFlame(),
                new FrostRaptor(),
                new SurgingFlame()));

        castSurgingFlame(player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.INSTANT_SPELL
                && entry.getCard().getName().equals("Surging Flame"));
    }

    @Test
    @DisplayName("Ripple can free-cast a revealed Surging Flame without paying mana")
    void rippleFreeCastsMatchingSpell() {
        prepareCaster(List.of(new SurgingFlame(), new FrostRaptor()));

        castSurgingFlame(player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    private void prepareCaster(List<Card> libraryTop) {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(libraryTop);
    }

    private void castSurgingFlame(UUID targetId) {
        harness.setHand(player1, List.of(new SurgingFlame()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
