package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WeRideAtDawn.class, AdelizTheCinderWind.class, GrizzlyBears.class})
class WeRideAtDawnTest extends BaseCardTest {

    @Test
    void givesConvokeToLegendaryCreatureSpells() {
        harness.addToBattlefield(player1, new WeRideAtDawn());
        Permanent convoker = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AdelizTheCinderWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        UUID convokerId = convoker.getId();
        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(convokerId));

        assertThat(convoker.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
    }

    @Test
    void doesNotGiveConvokeToNonlegendaryCreatureSpells() {
        harness.addToBattlefield(player1, new WeRideAtDawn());
        Permanent convoker = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0, List.of(), List.of(convoker.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(convoker.isTapped()).isFalse();
    }

    @Test
    void createsMercenaryWhenYourCommanderAttacks() {
        harness.addToBattlefield(player1, new WeRideAtDawn());
        AdelizTheCinderWind commanderCard = new AdelizTheCinderWind();
        gd.makeCommander(player1.getId(), commanderCard);
        Permanent commander = addCreatureReady(player1, commanderCard);
        commander.setCommander(true);

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Mercenary")).isEqualTo(1);
    }

    @Test
    void mercenaryCanBoostAControlledCreatureAtSorcerySpeed() {
        harness.addToBattlefield(player1, new WeRideAtDawn());
        AdelizTheCinderWind commanderCard = new AdelizTheCinderWind();
        gd.makeCommander(player1.getId(), commanderCard);
        Permanent commander = addCreatureReady(player1, commanderCard);
        commander.setCommander(true);

        declareAttackers(List.of(1));
        resolveAllTriggers();

        Permanent mercenary = findPermanent(player1, "Mercenary");
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        mercenary.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        int mercenaryIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);
        harness.activateAbility(player1, mercenaryIndex, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
    }

    @Test
    void doesNotTriggerForAnOpponentsCommanderYouControl() {
        harness.addToBattlefield(player1, new WeRideAtDawn());
        AdelizTheCinderWind commanderCard = new AdelizTheCinderWind();
        gd.makeCommander(player2.getId(), commanderCard);
        Permanent commander = addCreatureReady(player1, commanderCard);
        commander.setCommander(true);

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Mercenary")).isZero();
    }

    @Test
    void triggersForYourCommanderControlledByAnOpponent() {
        harness.addToBattlefield(player1, new WeRideAtDawn());
        AdelizTheCinderWind commanderCard = new AdelizTheCinderWind();
        gd.makeCommander(player1.getId(), commanderCard);
        Permanent commander = addCreatureReady(player2, commanderCard);
        commander.setCommander(true);

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Mercenary")).isEqualTo(1);
        assertThat(countPermanents(player2, "Mercenary")).isZero();
    }

    @Test
    void attackTriggerComesFromTheEnchantment() {
        harness.addToBattlefield(player1, new WeRideAtDawn());
        Permanent enchantment = findPermanent(player1, "We Ride at Dawn");
        AdelizTheCinderWind commanderCard = new AdelizTheCinderWind();
        gd.makeCommander(player1.getId(), commanderCard);
        Permanent commander = addCreatureReady(player1, commanderCard);
        commander.setCommander(true);

        declareAttackers(List.of(1));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(enchantment.getId());
    }

    @Test
    void doesNotTriggerForANonCommanderLegendaryCreature() {
        harness.addToBattlefield(player1, new WeRideAtDawn());
        addCreatureReady(player1, new AdelizTheCinderWind());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Mercenary")).isZero();
    }

    @Test
    void aSummoningSickCreatureCanConvoke() {
        harness.addToBattlefield(player1, new WeRideAtDawn());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent convoker = findPermanent(player1, "Grizzly Bears");
        convoker.setSummoningSick(true);
        harness.setHand(player1, List.of(new AdelizTheCinderWind()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(convoker.getId()));
        harness.passBothPriorities();

        assertThat(convoker.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Adeliz, the Cinder Wind");
    }

    @Test
    void doesNotGrantConvokeToAnOpponentsSpells() {
        harness.addToBattlefield(player1, new WeRideAtDawn());
        Permanent convoker = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new AdelizTheCinderWind()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(
                player2, 0, List.of(), List.of(convoker.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(convoker.isTapped()).isFalse();
    }

    @Test
    void mercenaryCannotActivateDuringCombatOrTargetAnOpponentsCreature() {
        harness.addToBattlefield(player1, new WeRideAtDawn());
        AdelizTheCinderWind commanderCard = new AdelizTheCinderWind();
        gd.makeCommander(player1.getId(), commanderCard);
        Permanent commander = addCreatureReady(player1, commanderCard);
        commander.setCommander(true);
        declareAttackers(List.of(1));
        resolveAllTriggers();
        Permanent mercenary = findPermanent(player1, "Mercenary");
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        mercenary.setSummoningSick(false);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(mercenary);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, commander.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mercenary.isTapped()).isFalse();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mercenary.isTapped()).isFalse();
    }
}
