package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingKavu;
import com.github.laxika.magicalvibes.cards.v.ViashinoGrappler;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SelectiveObliteration.class, Forest.class, GrizzlyBears.class,
        RagingKavu.class, ViashinoGrappler.class})
class SelectiveObliterationTest extends BaseCardTest {

    @Test
    @DisplayName("Each player chooses a color in active-player order")
    void promptsEachPlayerInApnapOrder() {
        castSelectiveObliteration();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.context()).isInstanceOf(ChoiceContext.EachPlayerChoosesColorThenExileOtherPermanentsChoice.class);

        harness.handleListChoice(player1, "GREEN");

        choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        harness.handleListChoice(player2, "RED");

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Keeps colorless permanents and each controller's chosen monocolored permanents")
    void exilesPermanentsThatDoNotMatchTheirControllersChoice() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new RagingKavu());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new ViashinoGrappler());
        harness.addToBattlefield(player2, new RagingKavu());
        harness.addToBattlefield(player2, new Forest());

        castSelectiveObliteration();
        harness.handleListChoice(player1, "GREEN");
        harness.handleListChoice(player2, "RED");

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Raging Kavu");
        harness.assertOnBattlefield(player2, "Viashino Grappler");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertNotOnBattlefield(player2, "Raging Kavu");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .contains("Raging Kavu");
    }

    private void castSelectiveObliteration() {
        harness.setHand(player1, List.of(new SelectiveObliteration()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
