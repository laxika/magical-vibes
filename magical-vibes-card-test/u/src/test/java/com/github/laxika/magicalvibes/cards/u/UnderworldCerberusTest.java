package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SipOfHemlock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.cards.z.Zombify;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnderworldCerberus.class, GrizzlyBears.class, Plains.class, WrathOfGod.class, Zombify.class, SipOfHemlock.class})
class UnderworldCerberusTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot be blocked by fewer than three creatures")
    void cannotBeBlockedByFewerThanThreeCreatures() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new UnderworldCerberus());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.addToBattlefield(player2, new GrizzlyBears());

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
        harness.castAndResolveSorcery(player1, 0, 0);
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

    @ParameterizedTest
    @ValueSource(ints = {0, 2, 3, 4})
    void blockerCountRestrictionAllowsUnblockedOrAtLeastThree(int blockerCount) {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new UnderworldCerberus());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        List<BlockerAssignment> assignments = new ArrayList<>();
        for (int i = 0; i < blockerCount; i++) {
            harness.addToBattlefield(player2, new GrizzlyBears());
            assignments.add(new BlockerAssignment(i, 0));
        }
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        if (blockerCount == 2) {
            assertThatThrownBy(() -> gs.declareBlockers(gd, player2, assignments))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("can't be blocked except by 3 or more creatures");
        } else {
            gs.declareBlockers(gd, player2, assignments);
            assertThat(gd.creaturesBlockedThisTurn.contains(attacker.getId())).isEqualTo(blockerCount > 0);
        }
    }

    @Test
    void opponentCerberusAlsoPreventsTargetingYourGraveyard() {
        harness.addToBattlefield(player2, new UnderworldCerberus());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Zombify()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void simultaneousDeathsReturnTheOtherCerberusBeforeItsTriggerResolves() {
        Card ownCerberus = new UnderworldCerberus();
        Card opponentCerberus = new UnderworldCerberus();
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, ownCerberus);
        harness.addToBattlefield(player2, opponentCerberus);
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opponentCerberus);
        assertThat(gd.playerHands.get(player1.getId())).contains(ownCerberus, creature);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).contains(ownCerberus, creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(ownCerberus);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void nonTargetingDeathReturnWorksWhileAnotherCerberusRemainsOnBattlefield() {
        Permanent dyingCerberus = harness.addToBattlefieldAndReturn(player1, new UnderworldCerberus());
        Permanent survivingCerberus = harness.addToBattlefieldAndReturn(player2, new UnderworldCerberus());
        Card ownCreature = new GrizzlyBears();
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.setHand(player1, List.of(new SipOfHemlock()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveSorcery(player1, 0, dyingCerberus.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(dyingCerberus.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(survivingCerberus);
        assertThat(gd.playerHands.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerHands.get(player2.getId())).contains(opponentCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(ownCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(opponentCreature);
    }
}
