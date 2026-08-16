package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MishraExcavationProdigyTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card and discards a card when activated")
    void drawsThenDiscards() {
        addMishra();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Spellbook");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Discarding an artifact adds two red mana")
    void artifactDiscardAddsTwoRedMana() {
        addMishra();
        harness.setHand(player1, List.of(new Spellbook()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        activateLootAbility(0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("Adds red mana at most once each turn for artifact discards")
    void triggersOnlyOnceEachTurn() {
        Permanent mishra = addMishra();
        harness.setHand(player1, List.of(new Spellbook(), new Spellbook()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        activateLootAbility(0);
        mishra.untap();
        activateLootAbility(0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    private Permanent addMishra() {
        Permanent mishra = harness.addToBattlefieldAndReturn(player1, new MishraExcavationProdigy());
        mishra.setSummoningSick(false);
        return mishra;
    }

    private void activateLootAbility(int discardIndex) {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, discardIndex);
        harness.passBothPriorities();
    }
}
