package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.c.CivilServant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({XandersLounge.class, CivilServant.class})
class XandersLoungeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new XandersLounge()));

        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping for blue mana produces one blue")
    void tappingProducesBlueMana() {
        harness.addToBattlefield(player1, new XandersLounge());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping for black mana produces one black")
    void tappingProducesBlackMana() {
        harness.addToBattlefield(player1, new XandersLounge());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping for red mana produces one red")
    void tappingProducesRedMana() {
        harness.addToBattlefield(player1, new XandersLounge());

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new XandersLounge()));
        harness.setLibrary(player1, List.of(new CivilServant()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Xander's Lounge");
        harness.assertInHand(player1, "Civil Servant");
    }

    @Test
    @DisplayName("Cycling pays generic mana and discards before drawing on resolution")
    void cyclingPaysAndDiscardsBeforeResolution() {
        XandersLounge lounge = new XandersLounge();
        CivilServant drawnCard = new CivilServant();
        harness.setHand(player1, List.of(lounge));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(lounge);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.stack).hasSize(1);
        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling cannot be activated with only two mana")
    void cyclingRequiresThreeMana() {
        XandersLounge lounge = new XandersLounge();
        harness.setHand(player1, List.of(lounge));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lounge);
        harness.assertNotInGraveyard(player1, "Xander's Lounge");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without being played still enters tapped")
    void entersTappedWithoutBeingPlayed() {
        Permanent lounge = harness.enterBattlefieldAndReturn(player1, new XandersLounge());

        assertThat(lounge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A tapped Lounge cannot produce mana")
    void tappedLoungeCannotProduceMana() {
        harness.setHand(player1, List.of(new XandersLounge()));
        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Producing mana taps the source and resolves immediately")
    void manaAbilityTapsSourceAndResolvesImmediately() {
        Permanent lounge = harness.addToBattlefieldAndReturn(player1, new XandersLounge());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(lounge.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        for (ManaColor color : ManaColor.values()) {
            if (color != ManaColor.BLUE) {
                assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
            }
        }
    }
}
