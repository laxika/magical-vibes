package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeroesForHire.class, Forest.class})
class HeroesForHireTest extends BaseCardTest {

    @Test
    @DisplayName("When Heroes for Hire enters, it creates three Treasures")
    void entersWithThreeTreasures() {
        castHeroesForHire();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(3);
    }

    @Test
    @DisplayName("Sacrificing a Treasure exiles the top card with permission to play it this turn")
    void sacrificeTreasureExilesTopCardWithPlayPermission() {
        castHeroesForHire();
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Treasure").getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
        assertThat(gd.exilePlayWithoutPayingManaCost).doesNotContain(topCard.getId());
    }

    @Test
    void canPlayExiledLand() {
        castHeroesForHire();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));

        sacrificeTreasure();
        harness.passBothPriorities();
        harness.castFromExile(player1, land.getId());

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.findExiledCard(land.getId())).isNull();
    }

    @Test
    void exiledSpellRequiresManaAndCanBeCastAfterPaying() {
        castHeroesForHire();
        Card spell = new HeroesForHire();
        harness.setLibrary(player1, List.of(spell));

        sacrificeTreasure();
        harness.passBothPriorities();
        assertThatThrownBy(() -> harness.castFromExile(player1, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(spell.getId())).isNotNull();

        harness.addMana(player1, ManaColor.RED, 5);
        harness.castFromExile(player1, spell.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Heroes for Hire")).isEqualTo(2);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(5);
        assertThat(gd.findExiledCard(spell.getId())).isNull();
    }

    @Test
    void emptyLibraryStillAllowsActivationAndCostsATreasure() {
        castHeroesForHire();
        harness.setLibrary(player1, List.of());

        sacrificeTreasure();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.exilePlayPermissions).isEmpty();
    }

    private void sacrificeTreasure() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, findPermanent(player1, "Treasure").getId());
    }

    private void castHeroesForHire() {
        harness.setHand(player1, List.of(new HeroesForHire()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
