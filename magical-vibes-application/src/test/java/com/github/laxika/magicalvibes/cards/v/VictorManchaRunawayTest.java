package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VictorManchaRunaway.class, Shock.class, GrizzlyBears.class})
class VictorManchaRunawayTest extends BaseCardTest {

    private Shock castVictorWithShockInGraveyard() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setHand(player1, List.of(new VictorManchaRunaway()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        return shock;
    }

    @Test
    @DisplayName("Exiles a target card from your graveyard and lets you play it while Victor remains controlled")
    void exilesAndAllowsPlayWhileControlled() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Shock shock = castVictorWithShockInGraveyard();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, shock.getId(), bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock);
    }

    @Test
    @DisplayName("Stops allowing the exiled card to be played when Victor leaves the battlefield")
    void permissionEndsWhenVictorLeaves() {
        Shock shock = castVictorWithShockInGraveyard();
        Permanent victor = findPermanent(player1, "Victor Mancha, Runaway");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, victor));

        assertThatThrownBy(() -> harness.castFromExile(player1, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
