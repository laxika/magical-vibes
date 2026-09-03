package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarshMercy.class, GrizzlyBears.class, HillGiant.class, RagingGoblin.class})
class HarshMercyTest extends BaseCardTest {

    @Test
    @DisplayName("Each player chooses a type and creatures of any chosen type survive")
    void chosenTypesAreUnionedAcrossPlayers() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new RagingGoblin());
        cast();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleListChoice(player1, "BEAR");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleListChoice(player2, "GIANT");

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player1, "Raging Goblin");
        harness.assertNotOnBattlefield(player2, "Raging Goblin");
    }

    @Test
    @DisplayName("The destruction does not allow regeneration")
    void creaturesCannotBeRegenerated() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        var goblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        goblin.setRegenerationShield(1);
        cast();

        harness.handleListChoice(player1, "BEAR");
        harness.handleListChoice(player2, "BEAR");

        harness.assertNotOnBattlefield(player1, "Raging Goblin");
        harness.assertInGraveyard(player1, "Raging Goblin");
    }

    private void cast() {
        harness.setHand(player1, List.of(new HarshMercy()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
