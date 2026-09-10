package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GreenhouseRicketyGazebo.class, Forest.class, Shock.class})
class GreenhouseRicketyGazeboTest extends BaseCardTest {

    @Test
    void greenhouseGrantsAnyColorManaToLandsYouControl() {
        castRoom(0);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void ricketyGazeboMillsFourAndReturnsUpToTwoPermanents() {
        Card firstPermanent = new Forest();
        Card secondPermanent = new GreenhouseRicketyGazebo();
        Card thirdPermanent = new Forest();
        Card nonPermanent = new Shock();
        harness.setLibrary(player1, List.of(firstPermanent, secondPermanent, thirdPermanent, nonPermanent));

        castRoom(1);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(2)
                .allMatch(card -> card == firstPermanent
                        || card == secondPermanent
                        || card == thirdPermanent);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(2)
                .contains(nonPermanent);
    }

    private Permanent castRoom(int doorIndex) {
        harness.setHand(player1, List.of(new GreenhouseRicketyGazebo()));
        harness.addMana(player1, ManaColor.GREEN, doorIndex == 0 ? 3 : 4);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }
}
