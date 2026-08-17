package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TrostaniDiscordantTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control get +1/+1")
    void boostsOtherOwnCreatures() {
        harness.addToBattlefield(player1, new TrostaniDiscordant());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("When Trostani enters, it creates two Soldier tokens with lifelink")
    void createsSoldierTokensWithLifelink() {
        castTrostani();

        List<Permanent> soldiers = findPermanents(player1, "Soldier");
        assertThat(soldiers).hasSize(2);
        assertThat(soldiers).allSatisfy(soldier ->
                assertThat(gqs.hasKeyword(gd, soldier, Keyword.LIFELINK)).isTrue());
    }

    @Test
    @DisplayName("At the beginning of the controller's end step, each player gets their owned creatures back")
    void returnsOwnedCreaturesToTheirOwners() {
        harness.addToBattlefield(player1, new TrostaniDiscordant());

        Permanent playerOneCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.stolenCreatures.put(playerOneCreature.getId(), player1.getId());
        Permanent playerTwoCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.stolenCreatures.put(playerTwoCreature.getId(), player2.getId());
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        gd.stolenCreatures.put(nonCreature.getId(), player1.getId());

        resolveControllerEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(playerOneCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(playerTwoCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(nonCreature);
    }

    private void castTrostani() {
        harness.setHand(player1, List.of(new TrostaniDiscordant()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void resolveControllerEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
