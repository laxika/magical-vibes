package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.cards.g.GhituJourneymage;
import com.github.laxika.magicalvibes.cards.m.MarketGnome;
import com.github.laxika.magicalvibes.cards.m.MineshaftSpider;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoamingThrone.class, GhituJourneymage.class, ElvishVisionary.class, Abrade.class, MineshaftSpider.class, MarketGnome.class})
class RoamingThroneTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a creature type makes Roaming Throne that type")
    void choosingSubtypeMakesThroneChosenType() {
        harness.castFromHand(player1, new RoamingThrone(), "{4}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WIZARD");

        Permanent throne = findPermanent(player1, "Roaming Throne");
        assertThat(gqs.computeStaticBonus(gd, throne).grantedSubtypes()).contains(CardSubtype.WIZARD);
    }

    @Test
    @DisplayName("Roaming Throne doubles a triggered ability from another creature of the chosen type")
    void doublesChosenTypeCreatureTrigger() {
        addThrone(CardSubtype.WIZARD);

        harness.castFromHand(player1, new GhituJourneymage(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Roaming Throne does not double a triggered ability from a creature of another type")
    void doesNotDoubleDifferentTypeCreatureTrigger() {
        addThrone(CardSubtype.WIZARD);

        harness.castFromHand(player1, new ElvishVisionary(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void twoThronesAddTwoTriggersRatherThanMultiplyThem() {
        addThrone(CardSubtype.WIZARD);
        addThrone(CardSubtype.WIZARD);

        harness.castFromHand(player1, new GhituJourneymage(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 14);
    }

    @Test
    void doesNotDoubleOpponentsMatchingCreatureTrigger() {
        addThrone(CardSubtype.WIZARD);
        harness.addToBattlefield(player2, new GhituJourneymage());

        harness.enterBattlefieldAndReturn(player2, new GhituJourneymage());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
    }

    @Test
    void doubledMayAbilitiesCanBeAcceptedAndDeclinedIndependently() {
        addThrone(CardSubtype.SPIDER);
        RoamingThrone first = new RoamingThrone();
        RoamingThrone second = new RoamingThrone();
        RoamingThrone third = new RoamingThrone();
        harness.setLibrary(player1, List.of(first, second, third));

        harness.castFromHand(player1, new MineshaftSpider(), "{3}{G}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotDoubleItsOwnWardTrigger() {
        Permanent throne = addThrone(CardSubtype.GOLEM);
        castAbradeAtThrone(throne);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Roaming Throne");
        harness.assertInGraveyard(player2, "Abrade");
    }

    @Test
    void doublesWardOfAnotherMatchingThrone() {
        Permanent target = addThrone(CardSubtype.GOLEM);
        addThrone(CardSubtype.GOLEM);
        castAbradeAtThrone(target);

        assertThat(gd.stack).hasSize(3);
    }

    @Test
    void doesNotDoubleWardOfAnotherThroneWithoutMatchingType() {
        Permanent target = addThrone(CardSubtype.WIZARD);
        addThrone(CardSubtype.SPIDER);
        castAbradeAtThrone(target);

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    void doublesDeathTriggerOfChosenTypeCreature() {
        addThrone(CardSubtype.GNOME);
        Permanent gnome = harness.addToBattlefieldAndReturn(player1, new MarketGnome());
        RoamingThrone first = new RoamingThrone();
        RoamingThrone second = new RoamingThrone();
        RoamingThrone third = new RoamingThrone();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new Abrade()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castModalInstant(player1, 0, 0, List.of(gnome.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Market Gnome");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player1, 22);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
    }
    private void castAbradeAtThrone(Permanent throne) {
        harness.setHand(player2, List.of(new Abrade()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castModalInstant(player2, 0, 1, List.of(throne.getId()));
    }

    private Permanent addThrone(CardSubtype chosenSubtype) {
        Permanent throne = harness.addToBattlefieldAndReturn(player1, new RoamingThrone());
        throne.setChosenSubtype(chosenSubtype);
        return throne;
    }
}
