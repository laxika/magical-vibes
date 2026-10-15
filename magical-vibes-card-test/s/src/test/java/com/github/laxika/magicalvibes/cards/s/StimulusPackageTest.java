package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StimulusPackage.class})
class StimulusPackageTest extends BaseCardTest {

    @Test
    void entersWithTwoTreasureTokens() {
        castStimulusPackage();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    void sacrificingTreasureCreatesCitizenToken() {
        castStimulusPackage();
        Permanent treasure = findPermanents(player1, "Treasure").getFirst();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, treasure.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Citizen")).hasSize(1);
    }

    @Test
    void cannotActivateWithoutTreasureToSacrifice() {
        harness.addToBattlefield(player1, new StimulusPackage());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void treasureIsPaidBeforeCitizenAbilityResolves() {
        castStimulusPackage();
        Permanent treasure = findPermanents(player1, "Treasure").getFirst();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, treasure.getId());

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Citizen")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        Permanent citizen = findPermanent(player1, "Citizen");
        assertThat(citizen.getCard().isToken()).isTrue();
        assertThat(citizen.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(citizen.getCard().getSubtypes()).containsExactly(CardSubtype.CITIZEN);
        assertThat(gqs.getEffectivePower(gd, citizen)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, citizen)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, citizen))
                .containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
        assertThat(citizen.isTapped()).isFalse();
    }

    @Test
    void canSacrificeTappedTreasure() {
        castStimulusPackage();
        Permanent treasure = findPermanents(player1, "Treasure").getFirst();
        treasure.tap();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, treasure.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Citizen")).hasSize(1);
    }

    @Test
    void canUseBothTreasuresWithoutTappingEnchantment() {
        castStimulusPackage();

        Permanent firstTreasure = findPermanents(player1, "Treasure").getFirst();
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, firstTreasure.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player1, "Citizen")).hasSize(2);
        assertThat(findPermanent(player1, "Stimulus Package").isTapped()).isFalse();
    }

    private void castStimulusPackage() {
        harness.setHand(player1, List.of(new StimulusPackage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
    }
}
