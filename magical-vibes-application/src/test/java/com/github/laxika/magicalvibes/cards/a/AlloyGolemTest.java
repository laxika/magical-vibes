package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AlloyGolem.class)
class AlloyGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Chooses a color as it enters and becomes that color")
    void choosesColorOnEntry() {
        harness.setHand(player1, List.of(new AlloyGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class))
                .isNotNull();
        harness.handleListChoice(player1, "RED");

        Permanent golem = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(golem.getChosenColors()).containsExactly(CardColor.RED);
        assertThat(gqs.getEffectiveColors(gd, golem)).containsExactly(CardColor.RED);
    }

    @ParameterizedTest
    @EnumSource(CardColor.class)
    @DisplayName("Each of the five colors can be chosen without creating an ETB trigger")
    void canChooseEachColor(CardColor color) {
        harness.setHand(player1, List.of(new AlloyGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        assertThat(gd.stack).isEmpty();

        harness.handleListChoice(player1, color.name());

        Permanent golem = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectiveColors(gd, golem)).containsExactly(color);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Separate copies retain their own chosen colors")
    void copiesChooseColorsIndependently() {
        harness.setHand(player1, List.of(new AlloyGolem(), new AlloyGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 12);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        List<Permanent> golems = gd.playerBattlefields.get(player1.getId());
        assertThat(golems).hasSize(2);
        assertThat(gqs.getEffectiveColors(gd, golems.get(0))).containsExactly(CardColor.BLUE);
        assertThat(gqs.getEffectiveColors(gd, golems.get(1))).containsExactly(CardColor.GREEN);
    }
}
