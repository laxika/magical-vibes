package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LurkingInformant.class, GrizzlyBears.class})
class LurkingInformantTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting puts the target player's top card into their graveyard")
    void acceptsPuttingTopCardIntoGraveyard() {
        addReadyInformant();
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player2.getId()).add(topCard);

        activate(player2.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Declining leaves the target player's top card on their library")
    void declinesPuttingTopCardIntoGraveyard() {
        addReadyInformant();
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player2.getId()).add(topCard);

        activate(player2.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(topCard);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    @DisplayName("The ability can target the controller's library")
    void targetsController() {
        addReadyInformant();
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(topCard);

        activate(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("An empty target library produces no may choice")
    void emptyLibrary() {
        addReadyInformant();
        gd.playerDecks.get(player2.getId()).clear();

        activate(player2.getId());

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyInformant() {
        Permanent informant = harness.addToBattlefieldAndReturn(player1, new LurkingInformant());
        informant.setSummoningSick(false);
        return informant;
    }

    private void activate(UUID targetPlayerId) {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, targetPlayerId);
        harness.passBothPriorities();
    }
}
