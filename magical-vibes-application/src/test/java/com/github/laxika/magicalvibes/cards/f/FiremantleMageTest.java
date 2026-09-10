package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SeaGateLoremaster;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FiremantleMage.class, GrizzlyBears.class, SeaGateLoremaster.class})
class FiremantleMageTest extends BaseCardTest {

    @Test
    @DisplayName("Its own Ally entry gives your creatures menace")
    void ownAllyEntryGrantsMenaceToYourCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FiremantleMage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent mage = findPermanent(player1, "Firemantle Mage");
        assertThat(gqs.hasKeyword(gd, mage, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Another Ally entry gives all your creatures menace")
    void anotherAllyEntryGrantsMenaceToYourCreatures() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new FiremantleMage());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SeaGateLoremaster()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent ally = findPermanent(player1, "Sea Gate Loremaster");
        assertThat(gqs.hasKeyword(gd, mage, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ally, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("A non-Ally creature entry does not trigger Firemantle Mage")
    void nonAllyEntryDoesNotTrigger() {
        Permanent mage = harness.addToBattlefieldAndReturn(player1, new FiremantleMage());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, mage, Keyword.MENACE)).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Granted menace wears off at end of turn")
    void menaceWearsOffAtEndOfTurn() {
        harness.setHand(player1, List.of(new FiremantleMage()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent mage = findPermanent(player1, "Firemantle Mage");
        assertThat(gqs.hasKeyword(gd, mage, Keyword.MENACE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, mage, Keyword.MENACE)).isFalse();
    }
}
