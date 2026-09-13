package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NullRod;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BragoKingEternal.class, GrizzlyBears.class, NullRod.class, Forest.class})
class BragoKingEternalTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage lets you choose any number of your nonland permanents to flicker")
    void flickersChosenPermanents() {
        Permanent brago = addReadyCreature(player1, new BragoKingEternal());
        brago.setAttacking(true);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent nullRod = harness.addToBattlefieldAndReturn(player1, new NullRod());
        harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        resolveCombat();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).contains(bears.getId(), nullRod.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(opponentBears.getId());

        harness.handlePermanentChosen(player1, bears.getId());
        harness.handlePermanentChosen(player1, nullRod.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Null Rod");
        assertThat(findPermanent(player1, "Grizzly Bears").getId()).isNotEqualTo(bears.getId());
        assertThat(findPermanent(player1, "Null Rod").getId()).isNotEqualTo(nullRod.getId());
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Choosing no permanents is allowed")
    void mayChooseNoPermanents() {
        Permanent brago = addReadyCreature(player1, new BragoKingEternal());
        brago.setAttacking(true);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        resolveCombat();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").getId()).isEqualTo(bears.getId());
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        GameData gameData = harness.getGameData();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gameData.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
