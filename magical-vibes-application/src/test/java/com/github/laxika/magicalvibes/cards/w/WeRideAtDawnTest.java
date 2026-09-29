package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AdelizTheCinderWind;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
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
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(convokerId));

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

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, null, null,
                List.of(), List.of(convoker.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(convoker.isTapped()).isFalse();
    }

    @Test
    void createsMercenaryWhenYourCommanderAttacks() {
        harness.addToBattlefield(player1, new WeRideAtDawn());
        GrizzlyBears commanderCard = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commanderCard);
        Permanent commander = addCreatureReady(player1, commanderCard);
        commander.setCommander(true);

        declareAttackers(List.of(1));
        resolveAllTriggers();

        gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.MERCENARY))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void mercenaryCanBoostAControlledCreatureAtSorcerySpeed() {
        harness.addToBattlefield(player1, new WeRideAtDawn());
        GrizzlyBears commanderCard = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commanderCard);
        Permanent commander = addCreatureReady(player1, commanderCard);
        commander.setCommander(true);

        declareAttackers(List.of(1));
        resolveAllTriggers();

        Permanent mercenary = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.MERCENARY))
                .findFirst()
                .orElseThrow();
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
}
