package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ReturnCreatureCardsPutIntoYourGraveyardFromBattlefieldThisTurnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NoRestForTheWickedTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gameData;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gameData = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    @Test
    @DisplayName("No Rest for the Wicked has correct card properties and ability structure")
    void hasCorrectPropertiesAndAbility() {
        NoRestForTheWicked card = new NoRestForTheWicked();

        assertThat(card.getName()).isEqualTo("No Rest for the Wicked");
        assertThat(card.getType()).isEqualTo(CardType.ENCHANTMENT);
        assertThat(card.getManaCost()).isEqualTo("{1}{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeSelfCost.class);
        assertThat(ability.getEffects().get(1))
                .isInstanceOf(ReturnCreatureCardsPutIntoYourGraveyardFromBattlefieldThisTurnToHandEffect.class);
    }

    @Test
    @DisplayName("Activating the ability sacrifices No Rest for the Wicked and puts the ability on the stack")
    void activationSacrificesAndStacksAbility() {
        harness.addToBattlefield(player1, new NoRestForTheWicked());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("No Rest for the Wicked"));
        assertThat(gameData.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("No Rest for the Wicked"));

        assertThat(gameData.stack).hasSize(1);
        StackEntry entry = gameData.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("No Rest for the Wicked");
        assertThat(entry.getTargetPermanentId()).isNull();
    }

    @Test
    @DisplayName("Returns only creature cards that were put into your graveyard from battlefield this turn")
    void returnsOnlyThisTurnBattlefieldCreaturesFromYourGraveyard() {
        Card alreadyInGraveyard = new GrizzlyBears();
        Card diedThisTurn = new GrizzlyBears();

        harness.setGraveyard(player1, List.of(alreadyInGraveyard));
        harness.addToBattlefield(player1, new NoRestForTheWicked());
        harness.addToBattlefield(player1, diedThisTurn);

        UUID targetCreatureId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetCreatureId);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gameData.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(diedThisTurn.getId()));
        assertThat(gameData.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(alreadyInGraveyard.getId()));
    }

    @Test
    @DisplayName("Does not return creatures put into an opponent's graveyard this turn")
    void doesNotReturnOpponentsCreatureCards() {
        Card opponentsCreature = new GrizzlyBears();

        harness.addToBattlefield(player1, new NoRestForTheWicked());
        harness.addToBattlefield(player2, opponentsCreature);

        UUID targetCreatureId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetCreatureId);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gameData.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(opponentsCreature.getId()));
        assertThat(gameData.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(opponentsCreature.getId()));
    }

    @Test
    @DisplayName("Does not return creatures that died on a previous turn")
    void doesNotReturnCreaturesFromPreviousTurn() {
        Card diedLastTurn = new GrizzlyBears();

        harness.addToBattlefield(player1, new NoRestForTheWicked());
        harness.addToBattlefield(player1, diedLastTurn);

        UUID targetCreatureId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetCreatureId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        int noRestIndex = findPermanentIndex(player1.getId(), "No Rest for the Wicked");
        harness.activateAbility(player1, noRestIndex, null, null);
        harness.passBothPriorities();

        assertThat(gameData.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(diedLastTurn.getId()));
        assertThat(gameData.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(diedLastTurn.getId()));
    }

    private int findPermanentIndex(UUID controllerId, String name) {
        List<com.github.laxika.magicalvibes.model.Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).getCard().getName().equals(name)) {
                return i;
            }
        }
        throw new IllegalStateException("Permanent not found: " + name);
    }
}
