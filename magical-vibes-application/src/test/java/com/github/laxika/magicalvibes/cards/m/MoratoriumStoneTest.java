package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoratoriumStone.class, GrizzlyBears.class, Plains.class})
class MoratoriumStoneTest extends BaseCardTest {

    @Test
    void exilesTargetCardFromAnyGraveyard() {
        harness.addToBattlefield(player1, new MoratoriumStone());
        Plains target = new Plains();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        prepareActivation(player1);

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId())).containsExactly(target);
        harness.assertOnBattlefield(player1, "Moratorium Stone");
    }

    @Test
    void exilesAllMatchingGraveyardCardsAndPermanents() {
        harness.addToBattlefield(player1, new MoratoriumStone());
        GrizzlyBears player1Permanent = new GrizzlyBears();
        GrizzlyBears player2Permanent = new GrizzlyBears();
        harness.addToBattlefield(player1, player1Permanent);
        harness.addToBattlefield(player2, player2Permanent);
        harness.addToBattlefield(player2, new Plains());

        GrizzlyBears player1Graveyard = new GrizzlyBears();
        GrizzlyBears player2Target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(player1Graveyard));
        harness.setGraveyard(player2, List.of(player2Target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        prepareActivation(player1);

        harness.activateAbility(player1, 0, 1, null, player2Target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(player1Graveyard, player1Permanent);
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(player2Target, player2Permanent);
        harness.assertInGraveyard(player1, "Moratorium Stone");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(harness.getGameData().playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Plains");
    }

    @Test
    void secondAbilityCannotTargetALandCard() {
        harness.addToBattlefield(player1, new MoratoriumStone());
        Plains target = new Plains();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        prepareActivation(player1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 1, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Moratorium Stone");
    }

    private void prepareActivation(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
