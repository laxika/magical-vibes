package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheMightyThorJaneFoster.class, GrizzlyBears.class, Island.class, LeoninScimitar.class})
class TheMightyThorJaneFosterTest extends BaseCardTest {

    @Test
    @DisplayName("Attack trigger targets a nontoken artifact or creature")
    void attackTriggerFiltersTargets() {
        addCreatureReady(player1, new TheMightyThorJaneFoster());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(opponentCreature.getId(), equipment.getId())
                .doesNotContain(land.getId());
    }

    @Test
    @DisplayName("Attack trigger returns the target tapped under its owner's control")
    void attackTriggerFlickersTargetTapped() {
        addCreatureReady(player1, new TheMightyThorJaneFoster());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player2, "Grizzly Bears");
        assertThat(returned).isNotSameAs(target);
        assertThat(returned.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An Equipment entering under your control draws a card")
    void equipmentEnteringDrawsCard() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.enterBattlefieldAndReturn(player1, new TheMightyThorJaneFoster());

        harness.enterBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
    }
}
