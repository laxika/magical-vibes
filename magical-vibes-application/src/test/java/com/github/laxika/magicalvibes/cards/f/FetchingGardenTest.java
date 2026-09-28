package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.SakuraTribeScout;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FetchingGarden.class, SakuraTribeScout.class})
class FetchingGardenTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when played from hand")
    void entersTappedWhenPlayedFromHand() {
        harness.setHand(player1, List.of(new FetchingGarden()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Fetching Garden").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when put onto the battlefield from hand")
    void entersUntappedWhenPutOntoBattlefieldFromHand() {
        Permanent scout = harness.addToBattlefieldAndReturn(player1, new SakuraTribeScout());
        scout.setSummoningSick(false);
        FetchingGarden garden = new FetchingGarden();
        harness.setHand(player1, List.of(garden));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Fetching Garden").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Produces both green and white mana")
    void producesGreenAndWhiteMana() {
        harness.addToBattlefield(player1, new FetchingGarden());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }
}
