package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EiganjoFreeRidersTest extends BaseCardTest {

    private Permanent addPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    @Test
    @DisplayName("Triggers only during its controller's upkeep")
    void triggersOnlyDuringControllerUpkeep() {
        harness.addToBattlefield(player1, new EiganjoFreeRiders());

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Prompt only includes white creatures you control")
    void promptOnlyIncludesWhiteCreaturesYouControl() {
        Permanent freeRiders = addPermanent(player1, new EiganjoFreeRiders());
        Permanent whiteCreature = addPermanent(player1, new SavannahLions());
        Permanent nonWhiteCreature = addPermanent(player1, new GrizzlyBears());
        Permanent opponentsWhiteCreature = addPermanent(player2, new SavannahLions());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(freeRiders.getId(), whiteCreature.getId())
                .doesNotContain(nonWhiteCreature.getId(), opponentsWhiteCreature.getId());
    }

    @Test
    @DisplayName("Chosen white creature is returned to its owner's hand")
    void chosenWhiteCreatureReturnedToOwnersHand() {
        addPermanent(player1, new EiganjoFreeRiders());
        Permanent whiteCreature = addPermanent(player1, new SavannahLions());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, whiteCreature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(whiteCreature.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof SavannahLions);
    }
}
