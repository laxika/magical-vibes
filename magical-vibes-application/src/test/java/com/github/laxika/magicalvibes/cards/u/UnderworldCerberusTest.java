package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UnderworldCerberusTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot be blocked by fewer than three creatures")
    void cannotBeBlockedByFewerThanThreeCreatures() {
        Permanent attacker = new Permanent(new UnderworldCerberus());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by 3 or more creatures");
    }

    @Test
    @DisplayName("Cards in graveyards cannot be targeted while it is on the battlefield")
    void preventsGraveyardTargets() {
        harness.addToBattlefield(player1, new UnderworldCerberus());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Death exiles it and returns all creature cards from every graveyard to their owners' hands")
    void deathExilesItAndReturnsAllGraveyardCreaturesToTheirOwnersHands() {
        Permanent cerberus = harness.addToBattlefieldAndReturn(player1, new UnderworldCerberus());
        Card cerberusCard = cerberus.getCard();
        Card ownCreature = new GrizzlyBears();
        Card ownLand = new Plains();
        Card opponentCreature = new GrizzlyBears();
        Card opponentLand = new Plains();
        harness.setGraveyard(player1, List.of(ownCreature, ownLand));
        harness.setGraveyard(player2, List.of(opponentCreature, opponentLand));

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(cerberusCard.getId()));
        assertThat(gd.playerHands.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerHands.get(player2.getId())).contains(opponentCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownLand);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentLand);
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(ownCreature.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(card -> card.getId().equals(opponentCreature.getId()));
    }
}
