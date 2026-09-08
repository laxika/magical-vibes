package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NightmareShepherd.class, GrizzlyBears.class, Shock.class})
class NightmareShepherdTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling a dying creature creates a 1/1 Nightmare token copy")
    void acceptingExilesCreatureAndCreatesNightmareCopy() {
        harness.addToBattlefield(player1, new NightmareShepherd());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UUID bearsCardId = bears.getCard().getId();

        killCreatureWithShock(player2, bears.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(bearsCardId));

        Permanent copy = findPermanent(player1, "Grizzly Bears");
        assertThat(copy.getCard().isToken()).isTrue();
        assertThat(copy.getCard().getPower()).isEqualTo(1);
        assertThat(copy.getCard().getToughness()).isEqualTo(1);
        assertThat(copy.getCard().getSubtypes())
                .contains(CardSubtype.BEAR, CardSubtype.NIGHTMARE);
    }

    @Test
    @DisplayName("Declining leaves the dying creature in its graveyard")
    void decliningDoesNotExileOrCreateCopy() {
        harness.addToBattlefield(player1, new NightmareShepherd());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        killCreatureWithShock(player2, bears.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> "Grizzly Bears".equals(card.getName()));
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for token or opposing creature deaths")
    void ignoresTokensAndOpponents() {
        harness.addToBattlefield(player1, new NightmareShepherd());

        Card tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);
        killCreatureWithShock(player2, token.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();

        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        killCreatureWithShock(player1, opposingCreature.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void killCreatureWithShock(Player caster, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() == null) {
            harness.passBothPriorities();
        }
    }
}
