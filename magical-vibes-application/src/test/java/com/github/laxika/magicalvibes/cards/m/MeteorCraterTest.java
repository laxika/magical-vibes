package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AlphaKavu;
import com.github.laxika.magicalvibes.cards.a.AuroraGriffin;
import com.github.laxika.magicalvibes.cards.q.QuestingPhelddagrif;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MeteorCrater.class, AlphaKavu.class, AuroraGriffin.class, QuestingPhelddagrif.class})
class MeteorCraterTest extends BaseCardTest {

    @Test
    @DisplayName("Produces no mana when no colored permanents are controlled")
    void producesNoManaWithoutColoredPermanents() {
        Permanent crater = harness.addToBattlefieldAndReturn(player1, new MeteorCrater());

        harness.activateAbility(player1, 0, null, null);

        assertThat(crater.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Automatically adds the only available permanent color")
    void autoAddsManaWithSingleColor() {
        harness.addToBattlefield(player1, new MeteorCrater());
        harness.addToBattlefield(player1, new AlphaKavu());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Prompts for a color when multiple permanent colors are available")
    void promptsForChoiceWithMultipleColors() {
        harness.addToBattlefield(player1, new MeteorCrater());
        harness.addToBattlefield(player1, new AuroraGriffin());
        harness.addToBattlefield(player1, new AlphaKavu());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A multicolored permanent makes each of its colors available")
    void multicoloredPermanentContributesEachColor() {
        harness.addToBattlefield(player1, new MeteorCrater());
        harness.addToBattlefield(player1, new QuestingPhelddagrif());

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly("BLUE", "GREEN", "WHITE");

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Opponent colors are not available")
    void opponentColorsDoNotContribute() {
        harness.addToBattlefield(player1, new MeteorCrater());
        harness.addToBattlefield(player2, new AlphaKavu());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }
}
