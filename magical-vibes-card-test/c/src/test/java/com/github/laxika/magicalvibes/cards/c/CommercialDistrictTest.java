package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CommercialDistrict.class})
class CommercialDistrictTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and surveils 1")
    void entersTappedAndSurveilsOne() {
        Card topCard = new CommercialDistrict();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new CommercialDistrict()));

        harness.playLand(player1, 0);
        Permanent district = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(district.isTapped()).isTrue();

        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Taps for red mana")
    void tapsForRedMana() {
        Permanent district = addReadyDistrict();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(district.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Taps for green mana")
    void tapsForGreenMana() {
        Permanent district = addReadyDistrict();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(district.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Surveil may leave the top card in the library")
    void mayKeepTopCard() {
        Card topCard = new CommercialDistrict();
        Card nextCard = new CommercialDistrict();
        Card opponentCard = new CommercialDistrict();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setLibrary(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new CommercialDistrict()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Surveil with an empty library resolves without a choice")
    void surveilsEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new CommercialDistrict()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Surveil trigger resolves after the land leaves the battlefield")
    void triggerResolvesWithoutSource() {
        Card topCard = new CommercialDistrict();
        Card nextCard = new CommercialDistrict();
        Card district = new CommercialDistrict();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setHand(player1, List.of(district));

        harness.playLand(player1, 0);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.setGraveyard(player1, List.of(district));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(district, topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.stack).isEmpty();
    }
    private Permanent addReadyDistrict() {
        Permanent district = harness.addToBattlefieldAndReturn(player1, new CommercialDistrict());
        district.setSummoningSick(false);
        return district;
    }
}
