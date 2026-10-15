package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NomadStadium.class, NantukoElder.class})
class NomadStadiumTest extends BaseCardTest {

    @Test
    void tapAbilityAddsWhiteManaAndDealsDamageToController() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new NomadStadium());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        harness.assertOnBattlefield(player1, "Nomad Stadium");
    }

    @Test
    void thresholdAbilityGainsLifeAndSacrificesTheLand() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new NomadStadium());
        harness.setGraveyard(player1, graveyardCards(7));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(0);
        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        harness.assertNotOnBattlefield(player1, "Nomad Stadium");
        harness.assertInGraveyard(player1, "Nomad Stadium");
    }

    @Test
    void thresholdAbilityCannotBeActivatedWithFewerThanSevenCardsInGraveyard() {
        harness.addToBattlefield(player1, new NomadStadium());
        harness.setGraveyard(player1, graveyardCards(6));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cards in your graveyard");
    }

    @Test
    void thresholdAbilityDoesNotCountOpponentsGraveyard() {
        harness.addToBattlefield(player1, new NomadStadium());
        harness.setGraveyard(player2, graveyardCards(7));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cards in your graveyard");
    }

    @Test
    void manaAbilityCannotBeActivatedWhileTheLandIsTapped() {
        harness.addToBattlefield(player1, new NomadStadium());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    void thresholdAbilityPaysSacrificeImmediatelyAndResolvesAfterThresholdIsLost() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new NomadStadium());
        harness.setGraveyard(player1, graveyardCards(7));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Nomad Stadium");
        harness.assertInGraveyard(player1, "Nomad Stadium");
        harness.assertLife(player1, 10);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();

        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
    }

    @Test
    void thresholdCountsLandCardsInGraveyard() {
        harness.setLife(player1, 10);
        harness.addToBattlefield(player1, new NomadStadium());
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            cards.add(new NomadStadium());
        }
        harness.setGraveyard(player1, cards);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
    }

    @Test
    void thresholdAbilityCannotUseManaProducedByTheSameTappedLand() {
        harness.addToBattlefield(player1, new NomadStadium());
        harness.setGraveyard(player1, graveyardCards(7));
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        harness.assertOnBattlefield(player1, "Nomad Stadium");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    void thresholdAbilityCannotBePaidWithColorlessMana() {
        harness.addToBattlefield(player1, new NomadStadium());
        harness.setGraveyard(player1, graveyardCards(7));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Nomad Stadium");
        assertThat(gd.stack).isEmpty();
    }

    private List<Card> graveyardCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new NantukoElder());
        }
        return cards;
    }
}
