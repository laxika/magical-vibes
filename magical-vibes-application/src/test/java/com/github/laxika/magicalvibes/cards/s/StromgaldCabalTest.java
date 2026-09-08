package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.b.BantSureblade;
import com.github.laxika.magicalvibes.cards.k.KjeldoranWarrior;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StromgaldCabal.class, BantSureblade.class, KjeldoranWarrior.class, BalduvianBears.class})
class StromgaldCabalTest extends BaseCardTest {

    // ===== Counters a white spell, paying 1 life =====

    @Test
    @DisplayName("Counters target white spell and pays 1 life")
    void countersWhiteSpell() {
        StromgaldCabal cabal = new StromgaldCabal();
        addCreatureReady(player1, cabal);
        harness.setLife(player1, 20);

        KjeldoranWarrior victim = new KjeldoranWarrior();
        harness.setHand(player2, List.of(victim));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, victim.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Kjeldoran Warrior is countered — goes to graveyard, 1 life paid
        harness.assertInGraveyard(player2, "Kjeldoran Warrior");
        harness.assertNotOnBattlefield(player2, "Kjeldoran Warrior");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @CardUsed(SwordsToPlowshares.class)
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
        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Cannot activate without life to pay")
    void cannotActivateWithoutLife() {
        StromgaldCabal cabal = new StromgaldCabal();
        var cabalPermanent = addCreatureReady(player1, cabal);

        KjeldoranWarrior victim = new KjeldoranWarrior();
        harness.setHand(player2, List.of(victim));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.setLife(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, victim.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
        assertThat(cabalPermanent.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isZero();
    }

    @Test
    @DisplayName("Counters a multicolored spell that is white")
    void countersMulticoloredWhiteSpell() {
        StromgaldCabal cabal = new StromgaldCabal();
        addCreatureReady(player1, cabal);
        harness.setLife(player1, 20);

        BantSureblade victim = new BantSureblade();
        harness.setHand(player2, List.of(victim));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, victim.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Bant Sureblade");
        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    // ===== Cannot target a non-white spell =====

    @Test
    @DisplayName("Cannot target a green spell")
    void cannotTargetGreenSpell() {
        StromgaldCabal cabal = new StromgaldCabal();
        addCreatureReady(player1, cabal);
        harness.setLife(player1, 20);

        BalduvianBears bears = new BalduvianBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
