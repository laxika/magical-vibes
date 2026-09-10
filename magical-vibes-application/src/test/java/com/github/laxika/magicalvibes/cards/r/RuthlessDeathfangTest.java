package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DiabolicEdict;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuthlessDeathfang.class, DiabolicEdict.class, Forest.class, GiantSpider.class, GrizzlyBears.class,
        ZuranOrb.class})
class RuthlessDeathfangTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature makes a chosen opponent sacrifice a creature")
    void sacrificingCreatureTriggersOpponentSacrifice() {
        harness.addToBattlefield(player1, new RuthlessDeathfang());
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        sacrificeAt(player1, sacrificed);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Sacrificing a noncreature permanent does not trigger Ruthless Deathfang")
    void sacrificingNoncreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new RuthlessDeathfang());
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GiantSpider());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    private void sacrificeAt(Player player, Permanent permanent) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player, List.of(new DiabolicEdict()));
        harness.addMana(player, ManaColor.BLACK, 2);
        harness.castInstant(player, 0, player.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player, permanent.getId());
    }
}
