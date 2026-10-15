package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GreenweaverDruid.class})
class GreenweaverDruidTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Greenweaver Druid produces two green mana")
    void tappingProducesTwoGreenMana() {
        Permanent perm = addCreatureReady(player1, new GreenweaverDruid());

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(perm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Already-tapped Greenweaver Druid cannot produce mana again")
    void alreadyTappedCannotTapAgain() {
        Permanent perm = addCreatureReady(player1, new GreenweaverDruid());
        harness.tapPermanent(player1, 0);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(perm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Summoning-sick Greenweaver Druid cannot tap for mana")
    void summoningSickCannotTap() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new GreenweaverDruid());
        perm.setSummoningSick(true);

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(perm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Mana is added immediately to the controller's pool on the opponent's turn")
    void manaAbilityResolvesImmediatelyForControllerOnOpponentsTurn() {
        harness.forceActivePlayer(player1);
        Permanent perm = addCreatureReady(player2, new GreenweaverDruid());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(perm.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
