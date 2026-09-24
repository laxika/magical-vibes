package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrderOfSuccession.class, GrizzlyBears.class})
class OrderOfSuccessionTest extends BaseCardTest {

    @Test
    @DisplayName("each player chooses from the next player and gains their chosen creature")
    void eachPlayerChoosesNextPlayersCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent chosenCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castOrderOfSuccession(player1, 0);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).contains(chosenCreature.getId());

        harness.handlePermanentChosen(player1, chosenCreature.getId());

        assertThat(controls(player1, chosenCreature)).isTrue();
        assertThat(controls(player2, ownCreature)).isTrue();
    }

    @Test
    @DisplayName("skips a player with no creatures")
    void skipsPlayerWithNoCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castOrderOfSuccession(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(controls(player2, ownCreature)).isTrue();
    }

    private void castOrderOfSuccession(Player player, int directionMode) {
        harness.setHand(player, List.of(new OrderOfSuccession()));
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);
        harness.castSorcery(player, 0, directionMode);
        harness.passBothPriorities();
    }

    private boolean controls(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).contains(permanent);
    }
}
