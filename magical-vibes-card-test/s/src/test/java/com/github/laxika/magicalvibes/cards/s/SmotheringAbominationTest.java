package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Blisterpod;
import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SmotheringAbomination.class, CruelEdict.class, Forest.class, GrizzlyBears.class,
        ZuranOrb.class, Blisterpod.class, SheerDrop.class, ScourFromExistence.class})
class SmotheringAbominationTest extends BaseCardTest {

    @Test
    void sacrificesACreatureAtUpkeepAndDraws() {
        harness.addToBattlefield(player1, new SmotheringAbomination());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, bears.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    void sacrificingANoncreatureDoesNotDraw() {
        harness.addToBattlefield(player1, new SmotheringAbomination());
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void opponentSacrificingACreatureDoesNotDraw() {
        harness.addToBattlefield(player1, new SmotheringAbomination());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void sacrificesItselfAndDrawsWhenItIsTheOnlyCreature() {
        harness.addToBattlefield(player1, new SmotheringAbomination());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Blisterpod()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Smothering Abomination");
        harness.assertInGraveyard(player1, "Smothering Abomination");
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Blisterpod");
    }

    @Test
    void doesNotSacrificeAtOpponentsUpkeep() {
        harness.addToBattlefield(player1, new SmotheringAbomination());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Blisterpod()));

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Smothering Abomination");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void upkeepSacrificeStillResolvesAfterSourceIsExiled() {
        Permanent abomination = harness.addToBattlefieldAndReturn(player1, new SmotheringAbomination());
        harness.addToBattlefield(player1, new Blisterpod());
        harness.setHand(player1, List.of(new ScourFromExistence()));
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.castAndResolveInstant(player1, 0, abomination.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Smothering Abomination");
        harness.assertInGraveyard(player1, "Blisterpod");
        harness.assertOnBattlefield(player1, "Eldrazi Scion");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void drawsWhenAnAwakenedLandIsSacrificed() {
        harness.addToBattlefield(player1, new SmotheringAbomination());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SmotheringAbomination());
        target.tap();
        harness.setHand(player1, List.of(new SheerDrop()));
        harness.setLibrary(player1, List.of(new Blisterpod()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null,
                List.of(target.getId(), land.getId()));
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, land)).isTrue();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, land.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player1, "Smothering Abomination");
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Blisterpod");
    }

    @Test
    void drawsForSacrificingACreatureTokenAsAManaAbilityCost() {
        harness.addToBattlefield(player1, new SmotheringAbomination());
        Permanent blisterpod = harness.addToBattlefieldAndReturn(player1, new Blisterpod());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, blisterpod.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Eldrazi Scion");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.activateAbility(player1, 1, 0, null, null);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Eldrazi Scion");
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Forest", "Forest");
    }
}
