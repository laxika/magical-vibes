package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MakeAnExample.class, GrizzlyBears.class, GiantSpider.class})
class MakeAnExampleTest extends BaseCardTest {

    @Test
    void controllerChoosesPileForOpponentToSacrifice() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        cast();

        PendingInteraction.MultiPermanentChoice separation =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(separation.playerId()).isEqualTo(player2.getId());
        assertThat(separation.validIds()).containsExactlyInAnyOrder(bears.getId(), spider.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(bears.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    void choosingSecondPileSacrificesTheUnselectedCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        cast();
        harness.handleMultiplePermanentsChosen(player2, List.of(bears.getId()));
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    private void cast() {
        harness.setHand(player1, List.of(new MakeAnExample()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
