package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FathomFleetFirebrand;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BreechesEagerPillager.class, FathomFleetFirebrand.class, GrizzlyBears.class})
class BreechesEagerPillagerTest extends BaseCardTest {

    private static final String TREASURE = "Create a Treasure token";
    private static final String CANT_BLOCK = "Target creature can't block this turn";
    private static final String EXILE = "Exile the top card of your library. You may play it this turn.";

    @Test
    @DisplayName("Each Pirate attack creates one trigger, and a mode cannot be chosen twice in a turn")
    void eachPirateAttackConsumesOneMode() {
        addCreatureReady(player1, new BreechesEagerPillager());
        addCreatureReady(player1, new FathomFleetFirebrand());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        chooseMode(TREASURE);

        PendingInteraction.ColorChoice nextChoice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(nextChoice).isNotNull();
        assertThat(nextChoice.options()).doesNotContain(TREASURE);

        harness.handleListChoice(player1, CANT_BLOCK);
        harness.handlePermanentChosen(player1, blocker.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(blocker.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Exiles the top card and lets its controller play it this turn")
    void exilesTopCardWithPlayPermission() {
        addCreatureReady(player1, new BreechesEagerPillager());
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        chooseMode(EXILE);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Does not trigger for a non-Pirate creature attacking")
    void ignoresNonPirateAttacks() {
        addCreatureReady(player1, new BreechesEagerPillager());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void chooseMode(String mode) {
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, mode);
    }
}
