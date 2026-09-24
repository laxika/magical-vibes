package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.m.MatsuTribeBirdstalker;
import com.github.laxika.magicalvibes.cards.s.Secretkeeper;
import com.github.laxika.magicalvibes.cards.s.SpiritualVisit;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HaruOnna.class, SpiritualVisit.class, Secretkeeper.class, MatsuTribeBirdstalker.class})
class HaruOnnaTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and draws a card")
    void entersAndDrawsCard() {
        harness.setLibrary(player1, List.of(new SpiritualVisit()));

        harness.castFromHand(player1, new HaruOnna(), "{3}{G}");
        resolveAllTriggers();

        harness.assertInHand(player1, "Spiritual Visit");
        harness.assertOnBattlefield(player1, "Haru-Onna");
    }

    @Test
    @DisplayName("Casting a Spirit spell may return Haru-Onna to its owner's hand")
    void spiritSpellReturnsHaruOnna() {
        addHaruOnna();
        harness.castFromHand(player1, new Secretkeeper(), "{3}{U}");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Haru-Onna");
    }

    @Test
    @DisplayName("Casting an Arcane spell may return Haru-Onna to its owner's hand")
    void arcaneSpellReturnsHaruOnna() {
        addHaruOnna();
        harness.castFromHand(player1, new SpiritualVisit(), "{W}");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Haru-Onna");
    }

    @Test
    @DisplayName("Declining the cast trigger leaves Haru-Onna on the battlefield")
    void decliningCastTriggerLeavesHaruOnnaOnBattlefield() {
        addHaruOnna();
        harness.castFromHand(player1, new SpiritualVisit(), "{W}");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Haru-Onna");
    }

    @Test
    @DisplayName("A non-Spirit non-Arcane spell does not trigger Haru-Onna")
    void unrelatedSpellDoesNotTrigger() {
        addHaruOnna();
        harness.castFromHand(player1, new MatsuTribeBirdstalker(), "{2}{G}{G}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Haru-Onna");
    }

    @Test
    @DisplayName("An opponent's Spirit or Arcane spell does not trigger Haru-Onna")
    void opponentSpellDoesNotTrigger() {
        addHaruOnna();
        harness.castFromHand(player2, new SpiritualVisit(), "{W}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Haru-Onna");
    }

    private void addHaruOnna() {
        harness.addToBattlefield(player1, new HaruOnna());
    }
}
