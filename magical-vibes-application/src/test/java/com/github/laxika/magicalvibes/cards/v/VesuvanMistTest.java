package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VesuvanMist.class, Island.class, LlanowarElves.class})
class VesuvanMistTest extends BaseCardTest {

    @Test
    void returnsTargetNontokenNonlandPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new VesuvanMist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInHand(player2, "Llanowar Elves");
        harness.assertNotInHand(player1, "Llanowar Elves");
    }

    @Test
    void kickerConjuresDuplicateWithPerpetualAnyColorPermission() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new VesuvanMist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castKickedInstant(player1, 0, target.getId());
        assertThat(gd.stack.getLast().isKicked()).isTrue();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Llanowar Elves");
        harness.assertInHand(player2, "Llanowar Elves");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    void conjuredDuplicateCanBeTargetedAsANontokenAndConjuredAgain() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new VesuvanMist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castKickedInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent duplicate = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player1, List.of(new VesuvanMist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castKickedInstant(player1, 0, duplicate.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Llanowar Elves"))
                .hasSize(2);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    @Test
    void kickedSpellDoesNotConjureWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new VesuvanMist()));
        harness.setHand(player2, List.of(new VesuvanMist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castKickedInstant(player1, 0, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Llanowar Elves");
        harness.assertNotInHand(player1, "Llanowar Elves");
    }

    @Test
    void cannotTargetCreatureToken() {
        LlanowarElves token = new LlanowarElves();
        token.setToken(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, token);
        harness.setHand(player1, List.of(new VesuvanMist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nontoken, nonland permanent");
    }

    @Test
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new LlanowarElves());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new VesuvanMist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nontoken, nonland permanent");
    }
}
