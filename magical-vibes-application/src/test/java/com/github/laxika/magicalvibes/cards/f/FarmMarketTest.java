package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.m.MummyParamount;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FarmMarket.class, MummyParamount.class, Island.class})
class FarmMarketTest extends BaseCardTest {

    @Test
    @DisplayName("Farm destroys target attacking creature and goes to the graveyard")
    void farmDestroysAttackingCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new MummyParamount());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.setHand(player1, List.of(new FarmMarket()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, attacker.getId());

        harness.assertNotOnBattlefield(player2, "Mummy Paramount");
        harness.assertInGraveyard(player1, "Farm");
    }

    @Test
    @DisplayName("Farm cannot target a non-combat creature")
    void farmCannotTargetNonCombatCreature() {
        Permanent nontarget = harness.addToBattlefieldAndReturn(player1, new MummyParamount());

        harness.setHand(player1, List.of(new FarmMarket()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, nontarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }

    @Test
    @DisplayName("Market cast from graveyard draws two then discards two, then exiles")
    void marketFlashbackDrawsDiscardsAndExiles() {
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setGraveyard(player1, List.of(new FarmMarket()));
        harness.setHand(player1, List.of(new MummyParamount(), new MummyParamount()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Farm") || c.getName().equals("Market"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Farm"));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Market requires sorcery timing")
    void marketRequiresSorceryTiming() {
        harness.setGraveyard(player1, List.of(new FarmMarket()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");
    }

    @Test
    @DisplayName("Market cannot be cast without enough mana")
    void marketFailsWithoutMana() {
        harness.setGraveyard(player1, List.of(new FarmMarket()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Farm destroys a blocking creature, including one its caster controls")
    void farmDestroysBlockingCreature() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player1, new MummyParamount());
        blocker.setBlocking(true);
        harness.setHand(player1, List.of(new FarmMarket()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, blocker.getId());

        harness.assertNotOnBattlefield(player1, "Mummy Paramount");
        harness.assertInGraveyard(player1, "Mummy Paramount");
        harness.assertInGraveyard(player1, "Farm");
    }

    @Test
    @DisplayName("Farm does not destroy a target that is no longer attacking or blocking")
    void farmRechecksCombatStatusOnResolution() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new MummyParamount());
        attacker.setAttacking(true);
        harness.setHand(player1, List.of(new FarmMarket()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Mummy Paramount");
        harness.assertInGraveyard(player1, "Farm");
    }

    @Test
    @DisplayName("Market draws before discarding even when its caster starts with an empty hand")
    void marketDiscardsNewlyDrawnCardsFromEmptyHand() {
        Island first = new Island();
        Island second = new Island();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(new FarmMarket()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(c -> c.getName().equals("Farm"));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
