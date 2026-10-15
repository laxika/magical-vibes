package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CradleOfTheAccursed;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NagaVitalist.class, Forest.class, Island.class, CradleOfTheAccursed.class})
class NagaVitalistTest extends BaseCardTest {

    @Test
    @DisplayName("Produces no mana when you control no lands")
    void producesNoManaWithoutLands() {
        addCreatureReady(player1, new NagaVitalist());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Auto-adds mana when only one of your land colors is available")
    void autoAddsManaWithSingleColor() {
        addCreatureReady(player1, new NagaVitalist());
        harness.addToBattlefield(player1, new Forest()); // green source

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Prompts for a color choice when multiple of your land colors are available")
    void promptsForChoiceWithMultipleColors() {
        addCreatureReady(player1, new NagaVitalist());
        harness.addToBattlefield(player1, new Forest()); // green
        harness.addToBattlefield(player1, new Island()); // blue

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing a color from your available land colors adds the correct mana")
    void choosingColorAddsMana() {
        addCreatureReady(player1, new NagaVitalist());
        harness.addToBattlefield(player1, new Forest()); // green
        harness.addToBattlefield(player1, new Island()); // blue

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Opponent lands do not contribute colors")
    void opponentLandsDoNotContribute() {
        addCreatureReady(player1, new NagaVitalist());
        harness.addToBattlefield(player2, new Forest()); // opponent's land

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    void producesColorlessFromColorlessLand() {
        var vitalist = addCreatureReady(player1, new NagaVitalist());
        harness.addToBattlefield(player1, new CradleOfTheAccursed());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(vitalist.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canChooseColorlessAlongsideColoredMana() {
        addCreatureReady(player1, new NagaVitalist());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new CradleOfTheAccursed());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "COLORLESS");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void tappedLandStillDeterminesAvailableMana() {
        var vitalist = addCreatureReady(player1, new NagaVitalist());
        var forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(vitalist.isTapped()).isTrue();
        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        var vitalist = harness.addToBattlefieldAndReturn(player1, new NagaVitalist());
        vitalist.setSummoningSick(true);
        harness.addToBattlefield(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(vitalist.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void cannotActivateAgainWhileTapped() {
        var vitalist = addCreatureReady(player1, new NagaVitalist());
        harness.addToBattlefield(player1, new Forest());
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(vitalist.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }
}
