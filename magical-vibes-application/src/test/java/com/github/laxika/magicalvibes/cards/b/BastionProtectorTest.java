package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BastionProtector.class, GrizzlyBears.class, WrathOfGod.class})
class BastionProtectorTest extends BaseCardTest {

    @Test
    void boostsItselfWhenItIsACommander() {
        Permanent bastion = harness.addToBattlefieldAndReturn(player1, new BastionProtector());
        gd.makeCommander(player1.getId(), bastion.getCard());

        assertThat(gqs.getEffectivePower(gd, bastion)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bastion)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bastion, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void boostsAndProtectsOwnCommanderCreaturesOnly() {
        Card ownCommander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), ownCommander);
        Permanent commander = addCreatureReady(player1, ownCommander);
        Permanent ordinaryCreature = addCreatureReady(player1, new GrizzlyBears());
        Card opposingCommander = new GrizzlyBears();
        gd.makeCommander(player2.getId(), opposingCommander);
        Permanent opponentCommander = addCreatureReady(player2, opposingCommander);
        harness.addToBattlefield(player1, new BastionProtector());

        assertThat(gqs.getEffectivePower(gd, commander)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, commander)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, commander, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ordinaryCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ordinaryCreature, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponentCommander)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCommander, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void respondsToCommanderDesignationChanges() {
        Card creature = new GrizzlyBears();
        Permanent permanent = addCreatureReady(player1, creature);
        harness.addToBattlefield(player1, new BastionProtector());

        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.INDESTRUCTIBLE)).isFalse();

        gd.makeCommander(player1.getId(), creature);

        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void indestructibleCommanderSurvivesWrathOfGod() {
        Card commander = new GrizzlyBears();
        gd.makeCommander(player1.getId(), commander);
        addCreatureReady(player1, commander);
        harness.addToBattlefield(player1, new BastionProtector());

        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Bastion Protector");
    }
}
