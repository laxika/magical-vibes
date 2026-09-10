package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BenalishInfantry;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Apathy.class, BenalishInfantry.class, MindStone.class})
class ApathyTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent bears = enchantOpponentBears();
        bears.tap();

        advanceToUpkeep(player2);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The controller may discard even when the enchanted creature is untapped")
    void discardingWorksWhenCreatureIsUntapped() {
        Permanent bears = enchantOpponentBears();
        harness.setHand(player2, List.of(new BenalishInfantry()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Benalish Infantry");
    }

    @Test
    @DisplayName("Enchanted creature's controller discards a card at random to untap it")
    void discardingUntapsTheCreature() {
        Permanent bears = enchantOpponentBears();
        bears.tap();
        harness.setHand(player2, List.of(new BenalishInfantry()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Benalish Infantry");
    }

    @Test
    @DisplayName("Declining the discard leaves the creature tapped and the hand intact")
    void decliningLeavesTapped() {
        Permanent bears = enchantOpponentBears();
        bears.tap();
        harness.setHand(player2, List.of(new BenalishInfantry()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(bears.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        harness.setHand(player1, List.of(new Apathy()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("With an empty hand nothing is discarded and the creature stays tapped")
    void emptyHandLeavesTapped() {
        Permanent bears = enchantOpponentBears();
        bears.tap();
        harness.setHand(player2, List.of());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(bears.isTapped()).isTrue();
    }

    private Permanent enchantOpponentBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BenalishInfantry());

        harness.setHand(player1, List.of(new Apathy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Apathy");
        assertThat(aura).isNotNull();
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
        return bears;
    }
}
