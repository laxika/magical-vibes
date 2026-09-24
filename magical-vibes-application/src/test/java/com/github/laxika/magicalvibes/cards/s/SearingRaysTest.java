package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FiresOfYavimaya;
import com.github.laxika.magicalvibes.cards.k.KavuTitan;
import com.github.laxika.magicalvibes.cards.r.RagingKavu;
import com.github.laxika.magicalvibes.cards.v.ViashinoGrappler;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SearingRays.class, FiresOfYavimaya.class, KavuTitan.class, RagingKavu.class,
        ViashinoGrappler.class})
class SearingRaysTest extends BaseCardTest {

    private void castSearingRays() {
        harness.castFromHand(player1, new SearingRays(), "{2}{R}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Resolving Searing Rays prompts for a color")
    void resolvingPromptsForColor() {
        castSearingRays();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Deals damage to each player based on that player's creatures of the chosen color")
    void dealsPerPlayerCreatureCount() {
        harness.addToBattlefield(player1, new ViashinoGrappler());
        harness.addToBattlefield(player1, new RagingKavu());
        harness.addToBattlefield(player1, new FiresOfYavimaya());
        harness.addToBattlefield(player2, new ViashinoGrappler());
        harness.addToBattlefield(player2, new KavuTitan());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castSearingRays();
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Counts creatures present when the color choice resolves")
    void countsCreaturesAtResolution() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castSearingRays();
        harness.addToBattlefield(player1, new ViashinoGrappler());
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A color with no creatures deals no damage")
    void noMatchingCreaturesDealNoDamage() {
        harness.addToBattlefield(player1, new KavuTitan());
        harness.addToBattlefield(player2, new KavuTitan());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castSearingRays();
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
