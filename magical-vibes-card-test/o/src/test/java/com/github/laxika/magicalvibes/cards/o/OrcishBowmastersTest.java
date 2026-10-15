package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SphinxOfTheSecondSun;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrcishBowmasters.class, GrizzlyBears.class, SphinxOfTheSecondSun.class})
class OrcishBowmastersTest extends BaseCardTest {

    @Test
    void entersDealsDamageAndAmassesOrcs() {
        harness.setHand(player1, List.of(new OrcishBowmasters()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        Permanent army = findPermanent(player1, "Orc Army");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(army.getCard().getSubtypes()).containsExactly(CardSubtype.ORC, CardSubtype.ARMY);
    }

    @Test
    void doesNotTriggerForTheFirstDrawOfTheDrawStep() {
        harness.addToBattlefield(player1, new OrcishBowmasters());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);

        draw(player2);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(findPermanents(player1, "Orc Army")).isEmpty();
    }

    @Test
    void triggersForAnAdditionalDrawAndTargetsAnyPlayer() {
        harness.addToBattlefield(player1, new OrcishBowmasters());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        draw(player2);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(findPermanent(player1, "Orc Army").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    void amassesOnAnExistingArmyAndGrantsOrc() {
        Permanent army = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);
        harness.addToBattlefield(player1, new OrcishBowmasters());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        draw(player2);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Orc Army")).isEmpty();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.ARMY, CardSubtype.ORC);
    }

    @Test
    void doesNotTriggerForItsControllersDraw() {
        harness.addToBattlefield(player1, new OrcishBowmasters());
        harness.setLibrary(player1, List.of(new OrcishBowmasters()));

        draw(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findPermanents(player1, "Orc Army")).isEmpty();
    }

    @Test
    void triggersForTheSecondDrawInTheSameDrawStep() {
        harness.addToBattlefield(player1, new OrcishBowmasters());
        harness.setLibrary(player2, List.of(new OrcishBowmasters(), new OrcishBowmasters()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);

        draw(player2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        draw(player2);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(findPermanent(player1, "Orc Army").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    void doesNotTriggerForTheFirstDrawOfAnAdditionalDrawStep() {
        harness.addToBattlefield(player1, new OrcishBowmasters());
        harness.addToBattlefield(player2, new SphinxOfTheSecondSun());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new OrcishBowmasters(), new OrcishBowmasters()));
        gd.turnNumber = 2;
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);
        draw(player2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntil(player2, TurnStep.POSTCOMBAT_MAIN);
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.DRAW);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(findPermanents(player1, "Orc Army")).isEmpty();
    }

    @Test
    void doesNotAmassWhenTheEnterTriggerTargetBecomesIllegal() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OrcishBowmasters());
        harness.setHand(player1, List.of(new OrcishBowmasters()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Orc Army")).isEmpty();
    }

    @Test
    void amassingSavesTheTargetedArmyFromLethalDamage() {
        harness.setHand(player1, List.of(new OrcishBowmasters()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent army = findPermanent(player1, "Orc Army");
        harness.setLibrary(player2, List.of(new OrcishBowmasters()));

        draw(player2);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, army.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Orc Army")).containsExactly(army);
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
