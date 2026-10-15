package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.m.MeriekeRiBerit;
import com.github.laxika.magicalvibes.cards.k.KjeldoranWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StromgaldCabal.class, MeriekeRiBerit.class, KjeldoranWarrior.class, BalduvianBears.class,
        SwordsToPlowshares.class})
class StromgaldCabalTest extends BaseCardTest {

    @Test
    @DisplayName("Counters target white spell and pays 1 life")
    void countersWhiteSpell() {
        StromgaldCabal cabal = new StromgaldCabal();
        addCreatureReady(player1, cabal);
        harness.setLife(player1, 20);

        KjeldoranWarrior victim = new KjeldoranWarrior();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, victim, "{W}");
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, victim.getId());
        harness.passBothPriorities();

        // Kjeldoran Warrior is countered — goes to graveyard, 1 life paid
        harness.assertInGraveyard(player2, "Kjeldoran Warrior");
        harness.assertNotOnBattlefield(player2, "Kjeldoran Warrior");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Can counter its controller's own white spell")
    void countersItsControllersOwnWhiteSpell() {
        StromgaldCabal cabal = new StromgaldCabal();
        var cabalPermanent = addCreatureReady(player1, cabal);
        harness.setLife(player1, 20);

        KjeldoranWarrior victim = new KjeldoranWarrior();
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, victim, "{W}");
        harness.activateAbility(player1, 0, null, victim.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Kjeldoran Warrior");
        assertThat(gd.stack).isEmpty();
        assertThat(cabalPermanent.isTapped()).isTrue();
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Counters a white noncreature spell and taps")
    void countersWhiteNonCreatureSpell() {
        StromgaldCabal cabal = new StromgaldCabal();
        var cabalPermanent = addCreatureReady(player1, cabal);
        harness.setLife(player1, 20);

        SwordsToPlowshares swords = new SwordsToPlowshares();
        harness.setHand(player2, List.of(swords));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, cabalPermanent.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, swords.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Swords to Plowshares");
        assertThat(cabalPermanent.isTapped()).isTrue();
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Cannot activate without life to pay")
    void cannotActivateWithoutLife() {
        StromgaldCabal cabal = new StromgaldCabal();
        var cabalPermanent = addCreatureReady(player1, cabal);

        KjeldoranWarrior victim = new KjeldoranWarrior();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, victim, "{W}");
        harness.passPriority(player2);
        harness.setLife(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, victim.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
        assertThat(cabalPermanent.isTapped()).isFalse();
        harness.assertLife(player1, 0);
    }

    @Test
    @DisplayName("Counters a multicolored spell that is white")
    void countersMulticoloredWhiteSpell() {
        StromgaldCabal cabal = new StromgaldCabal();
        addCreatureReady(player1, cabal);
        harness.setLife(player1, 20);

        MeriekeRiBerit victim = new MeriekeRiBerit();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, victim, "{W}{U}{B}");
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, victim.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Merieke Ri Berit");
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Cannot target a green spell")
    void cannotTargetGreenSpell() {
        StromgaldCabal cabal = new StromgaldCabal();
        addCreatureReady(player1, cabal);
        harness.setLife(player1, 20);

        BalduvianBears bears = new BalduvianBears();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, bears, "{1}{G}");
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Pays tap and life costs before the ability resolves")
    void paysCostsOnActivation() {
        var cabalPermanent = addCreatureReady(player1, new StromgaldCabal());
        harness.setLife(player1, 20);

        KjeldoranWarrior victim = new KjeldoranWarrior();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, victim, "{W}");
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, victim.getId());

        assertThat(cabalPermanent.isTapped()).isTrue();
        harness.assertLife(player1, 19);
        assertThat(gd.stack).hasSize(2);
        harness.assertNotInGraveyard(player2, "Kjeldoran Warrior");

        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Kjeldoran Warrior");
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Cannot activate while tapped and does not pay life")
    void cannotActivateWhileTapped() {
        var cabalPermanent = addCreatureReady(player1, new StromgaldCabal());
        cabalPermanent.tap();
        harness.setLife(player1, 20);

        KjeldoranWarrior victim = new KjeldoranWarrior();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, victim, "{W}");
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, victim.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness and does not pay costs")
    void cannotActivateWithSummoningSickness() {
        var cabalPermanent = addCreatureReady(player1, new StromgaldCabal());
        cabalPermanent.setSummoningSick(true);
        harness.setLife(player1, 20);

        KjeldoranWarrior victim = new KjeldoranWarrior();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, victim, "{W}");
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, victim.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(cabalPermanent.isTapped()).isFalse();
        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);
    }
}
