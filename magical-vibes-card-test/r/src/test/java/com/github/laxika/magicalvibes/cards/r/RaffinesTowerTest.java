package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.i.InspiringOverseer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RaffinesTower.class, InspiringOverseer.class})
class RaffinesTowerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new RaffinesTower()));

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("The mana ability offers white, blue, and black")
    void manaAbilityOffersThreeColors() {
        harness.addToBattlefield(player1, new RaffinesTower());

        harness.activateAbility(player1, 0, 0, null, null);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLUE", "BLACK");
    }

    @Test
    @DisplayName("Choosing a mana color adds one mana and taps Raffine's Tower")
    void choosingManaColorAddsManaAndTapsSource() {
        Permanent tower = harness.addToBattlefieldAndReturn(player1, new RaffinesTower());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(tower.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cycling discards Raffine's Tower and draws one card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new RaffinesTower()));
        harness.setLibrary(player1, List.of(new InspiringOverseer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Raffine's Tower");
        harness.assertInHand(player1, "Inspiring Overseer");
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLACK"})
    @DisplayName("Each remaining mana choice produces exactly one mana immediately")
    void remainingManaChoicesResolveImmediately(ManaColor color) {
        Permanent tower = harness.addToBattlefieldAndReturn(player1, new RaffinesTower());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(tower.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        for (ManaColor other : ManaColor.values()) {
            if (other != color) {
                assertThat(gd.playerManaPools.get(player1.getId()).get(other)).isZero();
            }
        }
    }

    @Test
    @DisplayName("Cycling pays generic mana and discards before the draw resolves")
    void cyclingPaysAndDiscardsBeforeResolution() {
        RaffinesTower tower = new RaffinesTower();
        InspiringOverseer drawnCard = new InspiringOverseer();
        harness.setHand(player1, List.of(tower));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(tower);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling cannot be activated with only two mana")
    void cyclingRequiresThreeMana() {
        RaffinesTower tower = new RaffinesTower();
        harness.setHand(player1, List.of(tower));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(tower);
        harness.assertNotInGraveyard(player1, "Raffine's Tower");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without being played still enters tapped")
    void entersTappedWithoutBeingPlayed() {
        Permanent tower = harness.enterBattlefieldAndReturn(player1, new RaffinesTower());

        assertThat(tower.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A newly played tapped Tower cannot produce mana")
    void tappedTowerCannotProduceMana() {
        harness.setHand(player1, List.of(new RaffinesTower()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }
        assertThat(gd.stack).isEmpty();
    }
}
