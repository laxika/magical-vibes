package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.MemorialToWar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StarCompassTest extends BaseCardTest {

    @Test
    @DisplayName("Produces no mana when you control no basic lands")
    void producesNoManaWithoutBasicLands() {
        harness.addToBattlefield(player1, new StarCompass());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Auto-adds mana when only one basic land color is available")
    void autoAddsManaWithSingleBasicColor() {
        harness.addToBattlefield(player1, new StarCompass());
        harness.addToBattlefield(player1, new Forest()); // green

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Prompts for a color choice when multiple basic land colors are available")
    void promptsForChoiceWithMultipleColors() {
        harness.addToBattlefield(player1, new StarCompass());
        harness.addToBattlefield(player1, new Forest()); // green
        harness.addToBattlefield(player1, new Island()); // blue

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing a color from the available basic land colors adds the correct mana")
    void choosingColorAddsMana() {
        harness.addToBattlefield(player1, new StarCompass());
        harness.addToBattlefield(player1, new Forest()); // green
        harness.addToBattlefield(player1, new Island()); // blue

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A nonbasic land you control does not contribute its colors")
    void nonbasicLandDoesNotContribute() {
        harness.addToBattlefield(player1, new StarCompass());
        harness.addToBattlefield(player1, new MemorialToWar()); // nonbasic land, taps for red

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Basic lands an opponent controls do not contribute")
    void opponentBasicLandsDoNotContribute() {
        harness.addToBattlefield(player1, new StarCompass());
        harness.addToBattlefield(player2, new Forest()); // opponent's basic

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Star Compass enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new StarCompass()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent compass = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(compass.isTapped()).isTrue();
    }
}
