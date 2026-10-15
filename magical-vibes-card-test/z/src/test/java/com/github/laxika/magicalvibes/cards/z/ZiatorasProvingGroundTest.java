package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.i.InspiringOverseer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZiatorasProvingGround.class, InspiringOverseer.class})
class ZiatorasProvingGroundTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new ZiatorasProvingGround()));

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for black mana produces one black")
    void tappingProducesBlackMana() {
        tapFor(ManaColor.BLACK);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping for red mana produces one red")
    void tappingProducesRedMana() {
        tapFor(ManaColor.RED);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping for green mana produces one green")
    void tappingProducesGreenMana() {
        tapFor(ManaColor.GREEN);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new ZiatorasProvingGround()));
        harness.setLibrary(player1, List.of(new InspiringOverseer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Ziatora's Proving Ground");
        harness.assertInHand(player1, "Inspiring Overseer");
    }

    @Test
    @DisplayName("Cycling pays three generic mana and discards before the draw resolves")
    void cyclingPaysAndDiscardsBeforeResolution() {
        ZiatorasProvingGround land = new ZiatorasProvingGround();
        InspiringOverseer drawnCard = new InspiringOverseer();
        harness.setHand(player1, List.of(land));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling cannot be activated with only two mana")
    void cyclingRequiresThreeMana() {
        ZiatorasProvingGround land = new ZiatorasProvingGround();
        harness.setHand(player1, List.of(land));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        harness.assertNotInGraveyard(player1, "Ziatora's Proving Ground");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without being played still enters tapped")
    void entersTappedWithoutBeingPlayed() {
        Permanent land = harness.enterBattlefieldAndReturn(player1, new ZiatorasProvingGround());

        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A newly played tapped land cannot produce mana")
    void tappedLandCannotProduceMana() {
        harness.setHand(player1, List.of(new ZiatorasProvingGround()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mana ability taps the land and produces exactly one mana without using the stack")
    void manaAbilityResolvesImmediately() {
        tapFor(ManaColor.GREEN);

        assertThat(findPermanent(player1, "Ziatora's Proving Ground").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private void tapFor(ManaColor color) {
        harness.addToBattlefield(player1, new ZiatorasProvingGround());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());
    }
}
