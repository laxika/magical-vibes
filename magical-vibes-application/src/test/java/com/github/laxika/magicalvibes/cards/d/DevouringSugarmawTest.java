package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.h.HaveForDinner;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DevouringSugarmaw.class, HaveForDinner.class})
class DevouringSugarmawTest extends BaseCardTest {

    @Test
    void adventureCreatesHumanAndFoodAndExilesTheCard() {
        DevouringSugarmaw card = new DevouringSugarmaw();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Human");
        harness.assertOnBattlefield(player1, "Food");
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void sacrificingFoodDuringUpkeepLeavesDevouringSugarmawUntapped() {
        DevouringSugarmaw card = castCreatureFaceAfterAdventure();
        Permanent sugarmaw = findPermanent(player1, "Devouring Sugarmaw");
        Permanent food = findPermanent(player1, "Food");

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, food.getId());

        assertThat(sugarmaw.isTapped()).isFalse();
        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(card).isSameAs(sugarmaw.getCard());
    }

    @Test
    void decliningToSacrificeFoodDuringUpkeepTapsDevouringSugarmaw() {
        castCreatureFaceAfterAdventure();
        Permanent sugarmaw = findPermanent(player1, "Devouring Sugarmaw");

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(sugarmaw.isTapped()).isTrue();
        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
    }

    @Test
    void noArtifactEnchantmentOrTokenTapsDevouringSugarmawWithoutPrompt() {
        Permanent sugarmaw = harness.addToBattlefieldAndReturn(player1, new DevouringSugarmaw());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(sugarmaw.isTapped()).isTrue();
    }

    private DevouringSugarmaw castCreatureFaceAfterAdventure() {
        DevouringSugarmaw card = new DevouringSugarmaw();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();
        return card;
    }
}
