package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DiabolicEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UlamogTheInfiniteGyreTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Ulamog destroys the targeted permanent before Ulamog resolves")
    void castingDestroysTargetedPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UlamogTheInfiniteGyre()));
        harness.addMana(player1, ManaColor.COLORLESS, 11);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Ulamog, the Infinite Gyre");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ulamog, the Infinite Gyre");
    }

    @Test
    @DisplayName("Ulamog's annihilator makes the defending player sacrifice four permanents")
    void annihilatorFour() {
        Permanent ulamog = addCreatureReady(player1, new UlamogTheInfiniteGyre());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new GrizzlyBears());
        }

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(ulamog)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("When Ulamog goes to a graveyard, its owner's graveyard is shuffled into their library")
    void shufflesItsOwnersGraveyardIntoLibrary() {
        harness.setLibrary(player1, List.of(new Island()));
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        addCreatureReady(player1, new UlamogTheInfiniteGyre());

        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, player1.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).extracting(Card::getName)
                .containsExactlyInAnyOrder("Island", "Ulamog, the Infinite Gyre", "Grizzly Bears",
                        "Diabolic Edict");
    }

    @Test
    @DisplayName("Ulamog cannot target a player with its cast trigger")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new UlamogTheInfiniteGyre()));
        harness.addMana(player1, ManaColor.COLORLESS, 11);

        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
