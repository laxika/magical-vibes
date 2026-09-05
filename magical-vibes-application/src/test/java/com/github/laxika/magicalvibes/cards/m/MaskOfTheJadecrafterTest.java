package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MaskOfTheJadecrafterTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one X/X colorless Golem artifact creature token and sacrifices itself")
    void createsGolemToken() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new MaskOfTheJadecrafter());
        Permanent mask = findPermanent(player1, "Mask of the Jadecrafter");
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int maskIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mask);
        harness.activateAbility(player1, maskIndex, 0, 3, null);
        harness.passBothPriorities();

        Permanent golem = findPermanent(player1, "Golem");
        assertThat(golem.getCard().isToken()).isTrue();
        assertThat(golem.getEffectivePower()).isEqualTo(3);
        assertThat(golem.getEffectiveToughness()).isEqualTo(3);
        assertThat(golem.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(golem.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(golem.getCard().getColors()).isEmpty();

        harness.assertNotOnBattlefield(player1, "Mask of the Jadecrafter");
        harness.assertInGraveyard(player1, "Mask of the Jadecrafter");
    }

    @Test
    @DisplayName("Unearth returns the mask with haste and exiles it at the next end step")
    void unearthReturnsWithHasteAndExilesAtEndStep() {
        harness.setGraveyard(player1, List.of(new MaskOfTheJadecrafter()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent mask = findPermanent(player1, "Mask of the Jadecrafter");
        assertThat(mask.getGrantedKeywords()).contains(Keyword.HASTE);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Mask of the Jadecrafter");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Mask of the Jadecrafter"));
    }

    @Test
    @DisplayName("Cannot activate the token ability outside sorcery speed")
    void cannotActivateAtInstantSpeed() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player1, new MaskOfTheJadecrafter());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 3, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
