package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GreatFierceBee.class, Forest.class, GrizzlyBears.class, Shock.class, WrathOfGod.class})
class GreatFierceBeeTest extends BaseCardTest {

    @Test
    @DisplayName("Scry 1 triggers when another creature dies")
    void scriesWhenAnotherCreatureDies() {
        Permanent bee = harness.addToBattlefieldAndReturn(player1, new GreatFierceBee());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bee);
    }

    @Test
    @DisplayName("Does not trigger when only Great Fierce Bee dies")
    void doesNotTriggerWhenOnlySelfDies() {
        Permanent bee = harness.addToBattlefieldAndReturn(player1, new GreatFierceBee());

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, bee.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Triggers only once when multiple other creatures die simultaneously")
    void triggersOnceForSimultaneousDeaths() {
        harness.addToBattlefield(player1, new GreatFierceBee());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.castAndResolveSorcery(player2, 0, 0);

        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
    }

    @Test
    void opposingCreatureDeathScriesControllersLibraryAndCanBottomCard() {
        harness.addToBattlefield(player1, new GreatFierceBee());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Forest top = new Forest();
        Forest next = new Forest();
        Forest opposingTop = new Forest();
        harness.setLibrary(player1, List.of(top, next));
        harness.setLibrary(player2, List.of(opposingTop));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opposingTop);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void separateDeathsEachTriggerAndScryCanKeepTopCard() {
        harness.addToBattlefield(player1, new GreatFierceBee());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Forest top = new Forest();
        Forest next = new Forest();
        harness.setLibrary(player1, List.of(top, next));
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        for (Permanent creature : List.of(first, second)) {
            harness.castAndResolveInstant(player1, 0, creature.getId());
            assertThat(gd.stack).hasSize(1);
            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                    .containsExactly(top);
            gs.handleInteractionAnswer(gd, player1,
                    new InteractionAnswer.ScryOrder(List.of(0), List.of()));
            assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, next);
            assertThat(gd.stack).isEmpty();
        }
    }

    @Test
    void eachBeeTriggersWhenBothDieTogether() {
        harness.addToBattlefield(player1, new GreatFierceBee());
        harness.addToBattlefield(player1, new GreatFierceBee());
        Forest top = new Forest();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(2);
        for (int i = 0; i < 2; i++) {
            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                    .containsExactly(top);
            gs.handleInteractionAnswer(gd, player1,
                    new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        }
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void emptyLibraryDoesNotLeaveScryInteraction() {
        harness.addToBattlefield(player1, new GreatFierceBee());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
