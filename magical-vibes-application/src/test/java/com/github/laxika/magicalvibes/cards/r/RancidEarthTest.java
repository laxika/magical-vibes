package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RancidEarth.class, Forest.class, FugitiveWizard.class, GrizzlyBears.class, Boomerang.class})
class RancidEarthTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target land without threshold")
    void destroysTargetLandWithoutThreshold() {
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        cast();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player1, "Fugitive Wizard");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("With threshold, destroys the land and deals 1 damage to each creature and player")
    void thresholdDealsDamageToEachCreatureAndPlayer() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        cast();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertNotOnBattlefield(player1, "Fugitive Wizard");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Threshold is checked before destroying a land you control")
    void thresholdDoesNotTurnOnFromDestroyedLand() {
        harness.setGraveyard(player1, graveyardWithCards(6));
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        cast(player1);

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Fugitive Wizard");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RancidEarth()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("An opponent's graveyard does not enable threshold")
    void opponentsGraveyardDoesNotEnableThreshold() {
        harness.setGraveyard(player1, graveyardWithCards(6));
        harness.setGraveyard(player2, graveyardWithSevenCards());
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player2, new Forest());

        cast();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player1, "Fugitive Wizard");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Rancid Earth");
    }

    @Test
    @DisplayName("Threshold gained after casting applies at resolution")
    void thresholdGainedBeforeResolution() {
        harness.setGraveyard(player1, graveyardWithCards(6));
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new RancidEarth()));
        addMana();
        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Forest"));

        harness.setGraveyard(player1, graveyardWithSevenCards());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertNotOnBattlefield(player1, "Fugitive Wizard");
        harness.assertInGraveyard(player1, "Fugitive Wizard");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Threshold lost after casting does not apply at resolution")
    void thresholdLostBeforeResolution() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new RancidEarth()));
        addMana();
        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Forest"));

        harness.setGraveyard(player1, graveyardWithCards(6));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player1, "Fugitive Wizard");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("With threshold, an illegal land target prevents all damage")
    void illegalTargetPreventsThresholdDamage() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        harness.addToBattlefield(player1, new FugitiveWizard());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new RancidEarth()));
        harness.setHand(player2, List.of(new Boomerang()));
        addMana();
        var landId = harness.getPermanentId(player2, "Forest");
        harness.castSorcery(player1, 0, landId);

        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, landId);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Forest");
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertNotInGraveyard(player2, "Forest");
        harness.assertInGraveyard(player1, "Rancid Earth");
        harness.assertOnBattlefield(player1, "Fugitive Wizard");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void cast() {
        cast(player2);
    }

    private void cast(Player targetPlayer) {
        harness.setHand(player1, List.of(new RancidEarth()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(targetPlayer, "Forest"));
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private List<Card> graveyardWithSevenCards() {
        return graveyardWithCards(7);
    }

    private List<Card> graveyardWithCards(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(ignored -> (Card) new GrizzlyBears())
                .toList();
    }
}
