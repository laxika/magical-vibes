package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BushmeatPoacher.class, GiantSpider.class, AlmightyBrushwagg.class})
class BushmeatPoacherTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices another creature, gains its toughness, and draws a card")
    void sacrificesAnotherCreatureGainsToughnessAndDraws() {
        addCreatureReady(player1, new BushmeatPoacher());
        addCreatureReady(player1, new GiantSpider());
        harness.setLife(player1, 10);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int libraryBefore = gd.playerDecks.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 1);
        harness.assertInGraveyard(player1, "Giant Spider");
    }

    @Test
    @DisplayName("Cannot sacrifice Bushmeat Poacher itself")
    void cannotSacrificeItself() {
        addCreatureReady(player1, new BushmeatPoacher());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void gainsLastKnownBoostedToughnessAndPaysSacrificeBeforeResolution() {
        Permanent poacher = addCreatureReady(player1, new BushmeatPoacher());
        addCreatureReady(player1, new AlmightyBrushwagg());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.setLife(player1, 10);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Almighty Brushwagg");
        assertThat(poacher.isTapped()).isTrue();
        harness.assertLife(player1, 10);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    void gainsToughnessOfChosenCreatureWhenSeveralCanBeSacrificed() {
        addCreatureReady(player1, new BushmeatPoacher());
        Permanent chosen = addCreatureReady(player1, new AlmightyBrushwagg());
        Permanent survivor = addCreatureReady(player1, new AlmightyBrushwagg());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.setLife(player1, 10);
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, chosen.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(survivor).doesNotContain(chosen);
    }

    @Test
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new BushmeatPoacher());
        addCreatureReady(player1, new AlmightyBrushwagg());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent poacher = addCreatureReady(player1, new BushmeatPoacher());
        poacher.tap();
        addCreatureReady(player1, new AlmightyBrushwagg());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent poacher = harness.addToBattlefieldAndReturn(player1, new BushmeatPoacher());
        poacher.setSummoningSick(true);
        addCreatureReady(player1, new AlmightyBrushwagg());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotSacrificeAnOpponentsCreature() {
        addCreatureReady(player1, new BushmeatPoacher());
        addCreatureReady(player2, new AlmightyBrushwagg());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
