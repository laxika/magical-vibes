package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DoOrDieTest extends BaseCardTest {

    @Test
    @DisplayName("Target player chooses a pile and the chosen creatures are destroyed")
    void targetPlayerChoosesPileToDestroy() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        bears.setRegenerationShield(1);

        castDoOrDie(player2.getId());

        PendingInteraction.MultiPermanentChoice separation =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(separation.playerId()).isEqualTo(player1.getId());
        assertThat(separation.validIds()).containsExactlyInAnyOrder(bears.getId(), spider.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Choosing the second pile destroys the creatures left in that pile")
    void targetPlayerChoosesSecondPile() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        castDoOrDie(player2.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Noncreature permanents are not separated or destroyed")
    void leavesNoncreaturePermanentsAlone() {
        harness.addToBattlefield(player2, new LeoninScimitar());

        castDoOrDie(player2.getId());

        harness.assertOnBattlefield(player2, "Leonin Scimitar");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castDoOrDie(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new DoOrDie()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, targetPlayerId);
        harness.passBothPriorities();
    }
}
