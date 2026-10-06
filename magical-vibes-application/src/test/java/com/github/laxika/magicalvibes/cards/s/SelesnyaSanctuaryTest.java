package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SelesnyaSanctuary.class, Forest.class, Watchwolf.class})
class SelesnyaSanctuaryTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and returns a chosen land to its owner's hand")
    void entersTappedAndReturnsChosenLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new SelesnyaSanctuary()));

        harness.playLand(player1, 0);

        Permanent sanctuary = findPermanent(player1, "Selesnya Sanctuary");
        assertThat(sanctuary.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, forest.getId());

        harness.assertOnBattlefield(player1, "Selesnya Sanctuary");
        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Can return itself when it is the only land")
    void canReturnItself() {
        harness.setHand(player1, List.of(new SelesnyaSanctuary()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent sanctuary = findPermanent(player1, "Selesnya Sanctuary");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactly(sanctuary.getId());
        harness.handlePermanentChosen(player1, sanctuary.getId());

        harness.assertNotOnBattlefield(player1, "Selesnya Sanctuary");
        harness.assertInHand(player1, "Selesnya Sanctuary");
    }

    @Test
    @DisplayName("The ETB ability only offers lands controlled by its controller")
    void onlyOffersControlledLands() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addToBattlefieldAndReturn(player1, new Watchwolf());
        harness.setHand(player1, List.of(new SelesnyaSanctuary()));

        harness.playLand(player1, 0);
        Permanent sanctuary = findPermanent(player1, "Selesnya Sanctuary");
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(forest.getId(), sanctuary.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(opponentForest.getId());

        harness.handlePermanentChosen(player1, forest.getId());
    }

    @Test
    @DisplayName("Tapping adds one green and one white mana")
    void manaAbilityAddsGreenAndWhite() {
        Permanent sanctuary = harness.addToBattlefieldAndReturn(player1, new SelesnyaSanctuary());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(sanctuary.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The return trigger still resolves after Sanctuary leaves the battlefield")
    void returnsLandAfterSourceLeaves() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new SelesnyaSanctuary()));
        harness.playLand(player1, 0);
        Permanent sanctuary = findPermanent(player1, "Selesnya Sanctuary");

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToHand(gd, sanctuary));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactly(forest.getId());
        harness.handlePermanentChosen(player1, forest.getId());

        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Selesnya Sanctuary");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("The return trigger does nothing if its controller has no lands on resolution")
    void noLandsOnResolution() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new SelesnyaSanctuary()));
        harness.playLand(player1, 0);
        Permanent sanctuary = findPermanent(player1, "Selesnya Sanctuary");

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToHand(gd, sanctuary));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Selesnya Sanctuary");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertNotInHand(player2, "Forest");
    }

    @Test
    @DisplayName("A controlled land owned by the opponent returns to the opponent's hand")
    void returnsLandToOwnerRatherThanController() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        gd.stolenCreatures.put(forest.getId(), player2.getId());
        harness.setHand(player1, List.of(new SelesnyaSanctuary()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, forest.getId());

        harness.assertInHand(player2, "Forest");
        harness.assertNotInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Selesnya Sanctuary");
    }
}
