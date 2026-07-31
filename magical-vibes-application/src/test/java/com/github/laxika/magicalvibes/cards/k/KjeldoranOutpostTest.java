package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KjeldoranOutpostTest extends BaseCardTest {

    @Test
    @DisplayName("Entering sacrifices a chosen Plains and the land enters")
    void entersBySacrificingPlains() {
        harness.addToBattlefield(player1, new Plains());
        Permanent plains = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new KjeldoranOutpost()));

        harness.playLand(player1, 0);

        harness.handlePermanentChosen(player1, plains.getId());

        harness.assertNotOnBattlefield(player1, "Plains");
        harness.assertInGraveyard(player1, "Plains");
        harness.assertOnBattlefield(player1, "Kjeldoran Outpost");
    }

    @Test
    @DisplayName("A tapped Plains is still a legal sacrifice")
    void tappedPlainsCanBeSacrificed() {
        harness.addToBattlefield(player1, new Plains());
        Permanent plains = gd.playerBattlefields.get(player1.getId()).getFirst();
        plains.tap();
        harness.setHand(player1, List.of(new KjeldoranOutpost()));

        harness.playLand(player1, 0);

        harness.handlePermanentChosen(player1, plains.getId());

        harness.assertInGraveyard(player1, "Plains");
        harness.assertOnBattlefield(player1, "Kjeldoran Outpost");
    }

    @Test
    @DisplayName("Declining the sacrifice puts the land into its owner's graveyard")
    void declinedSacrificeSendsLandToGraveyard() {
        harness.addToBattlefield(player1, new Plains());
        harness.setHand(player1, List.of(new KjeldoranOutpost()));

        harness.playLand(player1, 0);

        harness.handlePermanentChosen(player1, player1.getId());

        harness.assertOnBattlefield(player1, "Plains");
        harness.assertNotOnBattlefield(player1, "Kjeldoran Outpost");
        harness.assertInGraveyard(player1, "Kjeldoran Outpost");
    }

    @Test
    @DisplayName("With no Plains the land goes straight to the graveyard without a prompt")
    void noPlainsSendsLandToGraveyard() {
        harness.setHand(player1, List.of(new KjeldoranOutpost()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Kjeldoran Outpost");
        harness.assertInGraveyard(player1, "Kjeldoran Outpost");
    }

    @Test
    @DisplayName("Mana ability adds {W}")
    void manaAbilityAddsWhite() {
        harness.addToBattlefield(player1, new KjeldoranOutpost());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Token ability creates a 1/1 white Soldier token")
    void tokenAbilityCreatesSoldier() {
        harness.addToBattlefield(player1, new KjeldoranOutpost());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> "Soldier".equals(p.getCard().getName()));
    }
}
