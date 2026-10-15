package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZulaportCutthroat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MakeAnExample.class, GrizzlyBears.class, GiantSpider.class, ZulaportCutthroat.class})
class MakeAnExampleTest extends BaseCardTest {

    @Test
    void controllerChoosesPileForOpponentToSacrifice() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        cast();

        PendingInteraction.MultiPermanentChoice separation =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(separation.playerId()).isEqualTo(player2.getId());
        assertThat(separation.validIds()).containsExactlyInAnyOrder(bears.getId(), spider.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(bears.getId()));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    void choosingSecondPileSacrificesTheUnselectedCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        cast();
        harness.handleMultiplePermanentsChosen(player2, List.of(bears.getId()));
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    void choosingEmptyFirstPileLeavesOpponentCreaturesAlive() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast();
        harness.handleMultiplePermanentsChosen(player2, List.of());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Make an Example");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void choosingNonemptySecondPileSacrificesAllOpponentCreaturesOnly() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        cast();
        harness.handleMultiplePermanentsChosen(player2, List.of());
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Giant Spider");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void choosingEmptySecondPileLeavesOpponentCreaturesAlive() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast();
        harness.handleMultiplePermanentsChosen(player2, List.of(bears.getId()));
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentWithNoCreaturesRequiresNoPileChoice() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        cast();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Make an Example");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void creaturesInChosenPileSeeEachOthersSimultaneousDeaths() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new ZulaportCutthroat());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ZulaportCutthroat());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        cast();
        harness.handleMultiplePermanentsChosen(player2, List.of(first.getId(), second.getId()));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        for (int i = 0; i < 4 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 24);
    }

    private void cast() {
        harness.setHand(player1, List.of(new MakeAnExample()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
