package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SpringjackPasture;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CennsEnlistment.class, SpringjackPasture.class})
class CennsEnlistmentTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Cenn's Enlistment creates two 1/1 white Kithkin Soldier tokens")
    void createsTwoKithkinSoldierTokens() {
        harness.setHand(player1, List.of(new CennsEnlistment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        List<Permanent> soldiers = kithkinSoldiers();
        assertThat(soldiers).hasSize(2);
        for (Permanent soldier : soldiers) {
            assertThat(soldier.getCard().getPower()).isEqualTo(1);
            assertThat(soldier.getCard().getToughness()).isEqualTo(1);
            assertThat(soldier.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(soldier.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(soldier.getCard().getSubtypes()).contains(CardSubtype.KITHKIN, CardSubtype.SOLDIER);
            assertThat(soldier.getCard().isToken()).isTrue();
        }
    }

    @Test
    @DisplayName("Retrace creates two Kithkin Soldier tokens and discards a land")
    void retraceCreatesTokensAndDiscardsLand() {
        harness.setGraveyard(player1, List.of(new CennsEnlistment()));
        harness.setHand(player1, List.of(new SpringjackPasture()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castRetrace(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(kithkinSoldiers()).hasSize(2);
        // The discarded land and the resolved Cenn's Enlistment both end up in the graveyard.
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Springjack Pasture");
    }

    @Test
    @DisplayName("Retrace returns Cenn's Enlistment to the graveyard, not exile, so it can be recast")
    void retraceReturnsToGraveyard() {
        harness.setGraveyard(player1, List.of(new CennsEnlistment()));
        harness.setHand(player1, List.of(new SpringjackPasture()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castRetrace(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Cenn's Enlistment");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Cenn's Enlistment"));
    }

    @Test
    @DisplayName("Retrace puts Cenn's Enlistment on the stack as a sorcery without flashback disposition")
    void retracePutsOnStackAsSorcery() {
        harness.setGraveyard(player1, List.of(new CennsEnlistment()));
        harness.setHand(player1, List.of(new SpringjackPasture()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castRetrace(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack.getFirst().isCastWithFlashback()).isFalse();
    }

    @Test
    @DisplayName("Retrace discards the chosen land and keeps other cards in hand")
    void retraceDiscardsChosenLandFromMixedHand() {
        harness.setGraveyard(player1, List.of(new CennsEnlistment()));
        harness.setHand(player1, List.of(new CennsEnlistment(), new SpringjackPasture()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castRetrace(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(kithkinSoldiers()).hasSize(2);
        harness.assertInHand(player1, "Cenn's Enlistment");
        harness.assertNotInHand(player1, "Springjack Pasture");
        harness.assertInGraveyard(player1, "Springjack Pasture");
    }

    @Test
    @DisplayName("Retrace requires discarding a land card")
    void retraceRequiresLandDiscard() {
        harness.setGraveyard(player1, List.of(new CennsEnlistment()));
        harness.setHand(player1, List.of(new CennsEnlistment()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The same Cenn's Enlistment can be retraced repeatedly")
    void canRetraceAgainAfterResolving() {
        CennsEnlistment enlistment = new CennsEnlistment();
        harness.setGraveyard(player1, List.of(enlistment));
        harness.setHand(player1, List.of(new SpringjackPasture(), new SpringjackPasture()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castRetrace(player1, 0, 0);
        harness.passBothPriorities();

        int graveyardIndex = gd.playerGraveyards.get(player1.getId()).indexOf(enlistment);
        assertThat(graveyardIndex).isNotNegative();
        harness.castRetrace(player1, graveyardIndex, 0);
        harness.passBothPriorities();

        assertThat(kithkinSoldiers()).hasSize(4);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(enlistment).hasSize(3);
    }

    @Test
    @DisplayName("Retrace still requires the full mana cost")
    void retraceRequiresManaInAdditionToLand() {
        harness.setGraveyard(player1, List.of(new CennsEnlistment()));
        harness.setHand(player1, List.of(new SpringjackPasture()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Springjack Pasture");
        harness.assertInGraveyard(player1, "Cenn's Enlistment");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Retrace does not allow casting a sorcery during upkeep")
    void retraceRequiresSorceryTiming() {
        harness.setGraveyard(player1, List.of(new CennsEnlistment()));
        harness.setHand(player1, List.of(new SpringjackPasture()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castRetrace(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Springjack Pasture");
        harness.assertInGraveyard(player1, "Cenn's Enlistment");
        assertThat(gd.stack).isEmpty();
    }

    private List<Permanent> kithkinSoldiers() {
        return findPermanents(player1, "Kithkin Soldier");
    }
}
