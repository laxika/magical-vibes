package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheOoze.class, GrizzlyBears.class})
class TheOozeTest extends BaseCardTest {

    @Test
    void createsOneMutagenForEachPlusOneCounterOnLeavingCreature() {
        harness.addToBattlefield(player1, new TheOoze());
        Permanent leavingCreature = addCreatureReady(player1, new GrizzlyBears());
        leavingCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        leavingCreature.setCounterCount(CounterType.CHARGE, 3);

        removeFromBattlefield(leavingCreature);

        assertThat(findPermanents(player1, "Mutagen")).hasSize(2);
    }

    @Test
    void doesNotCreateMutagenWhenLeavingCreatureHasNoPlusOneCounter() {
        harness.addToBattlefield(player1, new TheOoze());
        Permanent leavingCreature = addCreatureReady(player1, new GrizzlyBears());

        removeFromBattlefield(leavingCreature);

        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
    }

    @Test
    void exilesTargetGraveyardCardAndCreatesMutagen() {
        harness.addToBattlefield(player1, new TheOoze());
        Card card = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(card));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, card.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(card);
        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    void cannotActivateGraveyardAbilityOutsideSorcerySpeed() {
        harness.addToBattlefield(player1, new TheOoze());
        Card card = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(card));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, card.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mutagenSacrificesToPutCounterOnTargetCreature() {
        harness.addToBattlefield(player1, new TheOoze());
        Permanent leavingCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        leavingCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        removeFromBattlefield(leavingCreature);

        Permanent mutagen = findPermanents(player1, "Mutagen").getFirst();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mutagen), 0,
                null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
    }

    private void removeFromBattlefield(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
