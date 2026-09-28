package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AjaniTheGreathearted;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VerdantCommand.class, AjaniTheGreathearted.class, GrizzlyBears.class, IcyManipulator.class})
class VerdantCommandTest extends BaseCardTest {

    @Test
    void createsTappedSquirrelsAndGivesTargetPlayerLife() {
        harness.setLife(player2, 10);
        cast(new int[]{0, 3}, null, List.of(player2.getId(), player2.getId()));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
        assertThat(findPermanents(player2, "Squirrel")).hasSize(2).allMatch(Permanent::isTapped);
    }

    @Test
    void exilesTargetGraveyardCard() {
        Card graveyardCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.setLife(player2, 10);

        cast(new int[]{2, 3}, graveyardCard.getId(), List.of(player2.getId()));

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(graveyardCard);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    @Test
    void countersPlaneswalkerLoyaltyAbility() {
        Permanent ajani = new Permanent(new AjaniTheGreathearted());
        ajani.setCounterCount(CounterType.LOYALTY, 4);
        ajani.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(ajani);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passPriority(player2);

        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new VerdantCommand()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castModalInstantWithModes(player1, 0, 2, new int[]{1, 3}, ajani.getCard().getId(),
                List.of(player1.getId()));
        harness.passBothPriorities();

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTargetOtherActivatedAbilities() {
        Permanent icyManipulator = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, target.getId());
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new VerdantCommand()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 2, new int[]{1, 3}, icyManipulator.getCard().getId(),
                List.of(player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, java.util.UUID targetId, List<java.util.UUID> targets) {
        harness.setHand(player1, List.of(new VerdantCommand()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castModalInstantWithModes(player1, 0, 2, modes, targetId, targets);
        harness.passBothPriorities();
    }
}
