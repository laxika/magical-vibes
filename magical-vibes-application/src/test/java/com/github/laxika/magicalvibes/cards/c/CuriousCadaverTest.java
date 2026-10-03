package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.Food;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CuriousCadaver.class, Forest.class, Clue.class, Food.class})
class CuriousCadaverTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Clue returns Curious Cadaver from the graveyard to its owner's hand")
    void sacrificingClueReturnsSelfFromGraveyard() {
        CuriousCadaver cadaver = new CuriousCadaver();
        harness.setGraveyard(player1, List.of(cadaver));
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent clue = addClueToken(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertInHand(player1, "Curious Cadaver");
        harness.assertNotInGraveyard(player1, "Curious Cadaver");
        harness.assertNotOnBattlefield(player1, "Clue");
    }

    @Test
    @DisplayName("Sacrificing a non-Clue permanent does not return Curious Cadaver")
    void sacrificingNonClueDoesNotReturnSelf() {
        harness.setGraveyard(player1, List.of(new CuriousCadaver()));
        Permanent artifact = addArtifactToken(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(artifact), null, null);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertInGraveyard(player1, "Curious Cadaver");
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> "Curious Cadaver".equals(card.getName()));
    }

    @Test
    @DisplayName("An opponent sacrificing a Clue does not return your Curious Cadaver")
    void opponentSacrificeDoesNotReturnSelf() {
        harness.setGraveyard(player1, List.of(new CuriousCadaver()));
        harness.setLibrary(player2, List.of(new Forest()));
        Permanent clue = addClueToken(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(clue), null, null);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertInGraveyard(player1, "Curious Cadaver");
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card instanceof CuriousCadaver);
        harness.assertNotOnBattlefield(player2, "Clue");
    }

    @Test
    @DisplayName("One Clue sacrifice returns every Curious Cadaver in your graveyard")
    void oneSacrificeReturnsMultipleCopies() {
        CuriousCadaver first = new CuriousCadaver();
        CuriousCadaver second = new CuriousCadaver();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent clue = addClueToken(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerHands.get(player1.getId())).contains(first, second);
        harness.assertNotInGraveyard(player1, "Curious Cadaver");
    }

    @Test
    @DisplayName("The return trigger does nothing if its source leaves the graveyard")
    void sourceLeavingGraveyardDoesNotReturnAnotherCopy() {
        CuriousCadaver source = new CuriousCadaver();
        CuriousCadaver other = new CuriousCadaver();
        harness.setGraveyard(player1, List.of(source));
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent clue = addClueToken(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);
        harness.setGraveyard(player1, List.of(other));
        harness.setExile(player1, List.of(source));
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(source, other);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
    }

    @Test
    @DisplayName("Curious Cadaver on the battlefield does not trigger on a Clue sacrifice")
    void battlefieldSourceDoesNotTrigger() {
        harness.addToBattlefield(player1, new CuriousCadaver());
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent clue = addClueToken(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(clue), null, null);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertOnBattlefield(player1, "Curious Cadaver");
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card instanceof CuriousCadaver);
    }

    private Permanent addClueToken(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Clue());
    }

    private Permanent addArtifactToken(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Food());
    }
}
