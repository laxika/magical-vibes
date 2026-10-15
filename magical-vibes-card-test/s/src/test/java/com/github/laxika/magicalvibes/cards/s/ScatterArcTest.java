package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BurstOfStrength;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScatterArc.class, Shambleshark.class, BurstOfStrength.class})
class ScatterArcTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        Shambleshark shark = new Shambleshark();
        harness.setHand(player1, List.of(shark));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.setHand(player2, List.of(new ScatterArc()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, shark.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters a noncreature spell and draws a card")
    void countersNoncreatureSpellAndDrawsCard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player2, List.of(new Shambleshark()));
        harness.addToBattlefield(player1, new Shambleshark());

        BurstOfStrength burst = new BurstOfStrength();
        harness.setHand(player1, List.of(burst));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new ScatterArc()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Shambleshark"));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, burst.getId());

        harness.assertInGraveyard(player1, "Burst of Strength");
        harness.assertInGraveyard(player2, "Scatter Arc");
        harness.assertInHand(player2, "Shambleshark");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when its only target leaves the stack")
    void doesNotDrawWhenTargetLeavesStack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new Shambleshark());
        BurstOfStrength burst = new BurstOfStrength();
        harness.setHand(player1, List.of(burst, new ScatterArc()));
        harness.setHand(player2, List.of(new ScatterArc()));
        harness.setLibrary(player1, List.of(new Shambleshark()));
        harness.setLibrary(player2, List.of(new Shambleshark()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Shambleshark"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, burst.getId());
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, burst.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Burst of Strength");
        harness.assertInGraveyard(player1, "Scatter Arc");
        harness.assertInGraveyard(player2, "Scatter Arc");
        harness.assertInHand(player1, "Shambleshark");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }
}
