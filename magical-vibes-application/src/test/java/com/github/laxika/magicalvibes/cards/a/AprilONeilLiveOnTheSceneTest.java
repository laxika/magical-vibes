package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DonatelloMutantMechanic;
import com.github.laxika.magicalvibes.cards.d.DonatelloTurtleTechie;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NinjaOfTheDeepHours;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        AprilONeilLiveOnTheScene.class,
        DonatelloMutantMechanic.class,
        DonatelloTurtleTechie.class,
        NinjaOfTheDeepHours.class,
        GrizzlyBears.class
})
class AprilONeilLiveOnTheSceneTest extends BaseCardTest {

    @Test
    @DisplayName("Investigates when a Mutant, Ninja, or Turtle enters under your control")
    void investigatesForMatchingCreatureTypes() {
        harness.addToBattlefield(player1, new AprilONeilLiveOnTheScene());

        enter(player1, new DonatelloMutantMechanic());
        enter(player1, new NinjaOfTheDeepHours());
        enter(player1, new DonatelloTurtleTechie());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(3);
    }

    @Test
    @DisplayName("Does not investigate for a nonmatching creature or an opponent's creature")
    void ignoresNonmatchingAndOpposingCreatures() {
        harness.addToBattlefield(player1, new AprilONeilLiveOnTheScene());

        enter(player1, new GrizzlyBears());
        enter(player2, new DonatelloMutantMechanic());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    private Permanent enter(Player player, Card card) {
        return harness.enterBattlefieldAndReturn(player, card);
    }
}
