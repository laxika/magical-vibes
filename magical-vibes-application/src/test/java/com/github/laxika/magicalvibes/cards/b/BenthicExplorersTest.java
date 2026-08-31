package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.h.HallOfTheBanditLord;
import com.github.laxika.magicalvibes.cards.h.HeartOfYavimaya;
import com.github.laxika.magicalvibes.cards.l.LakeOfTheDead;
import com.github.laxika.magicalvibes.cards.s.SoldeviExcavations;
import com.github.laxika.magicalvibes.cards.t.ThawingGlaciers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BenthicExplorers.class, HallOfTheBanditLord.class, HeartOfYavimaya.class, LakeOfTheDead.class,
        SoldeviExcavations.class, ThawingGlaciers.class})
class BenthicExplorersTest extends BaseCardTest {

    @Test
    @DisplayName("Untapping the only tapped opponent land untaps it and adds that land's mana")
    void untapsOpponentLandAndAddsItsMana() {
        Permanent explorers = addReadyExplorers();
        Permanent lake = harness.addToBattlefieldAndReturn(player2, new LakeOfTheDead());
        lake.tap();

        harness.activateAbility(player1, 0, null, null);

        assertThat(lake.isTapped()).isFalse();
        assertThat(explorers.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot be activated when no opponent land is tapped")
    void cannotActivateWithoutTappedOpponentLand() {
        addReadyExplorers();
        harness.addToBattlefield(player2, new HeartOfYavimaya());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Counts mana from a land ability with a mana rider")
    void countsManaFromLandAbilityWithManaRider() {
        addReadyExplorers();
        Permanent hall = harness.addToBattlefieldAndReturn(player2, new HallOfTheBanditLord());
        hall.tap();

        harness.activateAbility(player1, 0, null, null);

        assertThat(hall.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped land the controller controls is not a legal payment")
    void ownTappedLandIsNotALegalPayment() {
        addReadyExplorers();
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new HeartOfYavimaya());
        ownLand.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(ownLand.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("With several tapped opponent lands the chosen land decides the mana type")
    void chosenLandDecidesManaType() {
        addReadyExplorers();
        Permanent lake = harness.addToBattlefieldAndReturn(player2, new LakeOfTheDead());
        Permanent heart = harness.addToBattlefieldAndReturn(player2, new HeartOfYavimaya());
        lake.tap();
        heart.tap();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(lake.getId(), heart.getId());

        harness.handlePermanentChosen(player1, heart.getId());

        assertThat(heart.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Can choose colorless mana from a tapped land that produces multiple types")
    void canChooseColorlessMana() {
        addReadyExplorers();
        Permanent excavations = harness.addToBattlefieldAndReturn(player2, new SoldeviExcavations());
        excavations.tap();

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly("BLUE", "COLORLESS");

        harness.handleListChoice(player1, "COLORLESS");

        assertThat(excavations.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A tapped land with no mana ability produces no mana")
    void landWithNoManaAbilityProducesNoMana() {
        addReadyExplorers();
        Permanent glaciers = harness.addToBattlefieldAndReturn(player2, new ThawingGlaciers());
        glaciers.tap();

        harness.activateAbility(player1, 0, null, null);

        assertThat(glaciers.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadyExplorers() {
        return addCreatureReady(player1, new BenthicExplorers());
    }
}
