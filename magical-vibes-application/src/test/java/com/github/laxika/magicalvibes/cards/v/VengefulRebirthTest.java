package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VengefulRebirthTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.RED, 5); // 1 for {R}, 4 for generic
        harness.addMana(player1, ManaColor.GREEN, 1); // {G}
    }

    @Test
    @DisplayName("Returns nonland card to hand and deals its mana value in damage to target player")
    void nonlandReturnDamagesPlayer() {
        Card graveyardCreature = new GrizzlyBears(); // mana value 2
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new VengefulRebirth()));
        giveMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, graveyardCreature.getId(), List.of(player2.getId()));
        harness.passBothPriorities();

        // Deals 2 (Grizzly Bears' mana value) to the target player
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        // Card returned to hand
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(graveyardCreature.getId()));
        // Vengeful Rebirth exiled (not in graveyard)
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Vengeful Rebirth"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Vengeful Rebirth"));
    }

    @Test
    @DisplayName("Deals mana value damage to a target creature")
    void nonlandReturnDamagesCreature() {
        Card graveyardCreature = new GrizzlyBears(); // mana value 2
        harness.addToBattlefield(player2, new GoblinPiker()); // dies to 2 damage
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new VengefulRebirth()));
        giveMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        UUID victimId = harness.getPermanentId(player2, "Goblin Piker");

        harness.castSorcery(player1, 0, graveyardCreature.getId(), List.of(victimId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Goblin Piker");
        harness.assertInGraveyard(player2, "Goblin Piker");
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(graveyardCreature.getId()));
    }

    @Test
    @DisplayName("Returning a land card deals no damage")
    void landReturnDealsNoDamage() {
        Card graveyardLand = new Forest();
        harness.setGraveyard(player1, List.of(graveyardLand));
        harness.setHand(player1, List.of(new VengefulRebirth()));
        giveMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, graveyardLand.getId(), List.of(player2.getId()));
        harness.passBothPriorities();

        // No damage — land has no nonland clause
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        // Land still returned to hand
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(graveyardLand.getId()));
        // Spell still exiled
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Vengeful Rebirth"));
    }

    @Test
    @DisplayName("Graveyard target removed before resolution — no return and no damage")
    void graveyardTargetRemovedNoDamage() {
        Card graveyardCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new VengefulRebirth()));
        giveMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, graveyardCreature.getId(), List.of(player2.getId()));

        // Graveyard target leaves before resolution — not returned, so no damage this way
        gd.playerGraveyards.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(graveyardCreature.getId()));
    }

    @Test
    @DisplayName("Cannot target a card in another player's graveyard")
    void cannotTargetOpponentGraveyardCard() {
        Card ownCard = new GrizzlyBears();
        Card opponentCard = new GoblinPiker();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new VengefulRebirth()));
        giveMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, opponentCard.getId(), List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
