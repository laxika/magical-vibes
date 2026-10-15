package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.EnergyRefractor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GixsCaress.class, GrizzlyBears.class, Forest.class, EnergyRefractor.class})
class GixsCaressTest extends BaseCardTest {

    @Test
    @DisplayName("Discards a chosen nonland card and creates a tapped Powerstone")
    void discardsNonlandAndCreatesPowerstone() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));
        harness.setHand(player1, List.of(new GixsCaress()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);

        List<Permanent> powerstones = findPermanents(player1, "Powerstone");
        assertThat(powerstones).hasSize(1);
        assertThat(powerstones.getFirst().getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(powerstones.getFirst().getCard().getSubtypes()).containsExactly(CardSubtype.POWERSTONE);
        assertThat(powerstones.getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new GixsCaress()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An empty opposing hand still allows creation of a tapped Powerstone")
    void createsPowerstoneWithEmptyHand() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new GixsCaress()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanents(player1, "Powerstone")).hasSize(1);
        assertThat(findPermanents(player1, "Powerstone").getFirst().isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Powerstone");
        harness.assertInGraveyard(player1, "Gix's Caress");
    }

    @Test
    @DisplayName("A land-only opposing hand is unchanged and a tapped Powerstone is created")
    void createsPowerstoneWithOnlyLandsInHand() {
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new GixsCaress()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.assertNotInGraveyard(player2, "Forest");
        assertThat(findPermanents(player1, "Powerstone")).hasSize(1);
        assertThat(findPermanents(player1, "Powerstone").getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("The caster must choose a nonland card and may choose a noncreature spell")
    void casterChoosesNoncreatureSpell() {
        harness.setHand(player2, List.of(new Forest(), new GixsCaress(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new GixsCaress()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(findPermanents(player1, "Powerstone")).isEmpty();
        assertThatThrownBy(() -> harness.handleCardChosen(player2, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);

        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player2, "Gix's Caress");
        harness.assertInHand(player2, "Forest");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(findPermanents(player1, "Powerstone")).hasSize(1);
    }

    @Test
    @DisplayName("The created Powerstone produces mana that can pay for an artifact spell")
    void powerstoneManaPaysForArtifact() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new GixsCaress()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        Permanent powerstone = findPermanents(player1, "Powerstone").getFirst();
        powerstone.untap();
        harness.activateAbility(player1, 0, null, null);
        assertThat(powerstone.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isEqualTo(1);

        harness.setHand(player1, List.of(new EnergyRefractor()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Energy Refractor");
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isZero();
    }

    @Test
    @DisplayName("The created Powerstone cannot pay for a nonartifact spell")
    void powerstoneManaCannotPayForNonartifact() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new GixsCaress()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        findPermanents(player1, "Powerstone").getFirst().untap();
        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player1, List.of(new GixsCaress()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Gix's Caress");
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isEqualTo(1);
    }
}
