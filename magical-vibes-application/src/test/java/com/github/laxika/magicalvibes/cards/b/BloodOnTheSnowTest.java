package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.t.TyvarKell;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodOnTheSnow.class, GrizzlyBears.class, GloriousAnthem.class,
        LlanowarElves.class, HillGiant.class, TyvarKell.class, Ornithopter.class})
class BloodOnTheSnowTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys all creatures and returns an eligible creature from the graveyard")
    void destroysCreaturesAndReturnsEligibleCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GloriousAnthem());
        LlanowarElves eligible = new LlanowarElves();
        HillGiant tooExpensive = new HillGiant();
        harness.setGraveyard(player1, List.of(eligible, tooExpensive));
        harness.setHand(player1, List.of(new BloodOnTheSnow()));
        addBloodMana(1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1,
                gd.playerGraveyards.get(player1.getId()).indexOf(eligible));

        harness.assertOnBattlefield(player1, "Glorious Anthem");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not return a card with mana value above the snow mana spent")
    void doesNotReturnTooExpensiveCard() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        HillGiant tooExpensive = new HillGiant();
        harness.setGraveyard(player1, List.of(tooExpensive));
        harness.setHand(player1, List.of(new BloodOnTheSnow()));
        addBloodMana(1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The planeswalker mode leaves creatures and can return a planeswalker")
    void destroysPlaneswalkersAndReturnsEligiblePlaneswalker() {
        harness.addToBattlefield(player1, new TyvarKell());
        harness.addToBattlefield(player2, new TyvarKell());
        harness.addToBattlefield(player1, new GrizzlyBears());
        TyvarKell eligible = new TyvarKell();
        harness.setGraveyard(player1, List.of(eligible));
        harness.setHand(player1, List.of(new BloodOnTheSnow()));
        addBloodMana(4);

        harness.castAndResolveSorcery(player1, 0, 1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1,
                gd.playerGraveyards.get(player1.getId()).indexOf(eligible));

        harness.assertOnBattlefield(player1, "Tyvar Kell");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Tyvar Kell");
        harness.assertInGraveyard(player2, "Tyvar Kell");
        harness.assertNotOnBattlefield(player2, "Tyvar Kell");
    }

    @Test
    void canReturnCreatureJustDestroyedWithoutGraveyardTargetAtCasting() {
        GrizzlyBears creature = new GrizzlyBears();
        Permanent original = harness.addToBattlefieldAndReturn(player1, creature);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BloodOnTheSnow()));
        addBloodMana(2);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(creature));
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(original.getId()));
    }

    @Test
    void creatureModeCanReturnPlaneswalkerAndLeavesExistingPlaneswalker() {
        harness.addToBattlefieldAndReturn(player2, new TyvarKell()).setCounterCount(CounterType.LOYALTY, 4);
        harness.addToBattlefield(player1, new GrizzlyBears());
        TyvarKell eligible = new TyvarKell();
        harness.setGraveyard(player1, List.of(eligible));
        harness.setHand(player1, List.of(new BloodOnTheSnow()));
        addBloodMana(4);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(eligible));
        harness.assertOnBattlefield(player1, "Tyvar Kell");
        harness.assertOnBattlefield(player2, "Tyvar Kell");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void planeswalkerModeCanReturnCreatureEvenWhenNothingIsDestroyed() {
        GrizzlyBears eligible = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(eligible));
        harness.setHand(player1, List.of(new BloodOnTheSnow()));
        addBloodMana(2);

        harness.castAndResolveSorcery(player1, 0, 1);

        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(eligible));
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void canReturnPlaneswalkerJustDestroyed() {
        TyvarKell planeswalker = new TyvarKell();
        harness.addToBattlefield(player1, planeswalker);
        harness.setHand(player1, List.of(new BloodOnTheSnow()));
        addBloodMana(4);

        harness.castAndResolveSorcery(player1, 0, 1);

        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(planeswalker));
        harness.assertOnBattlefield(player1, "Tyvar Kell");
        harness.assertNotInGraveyard(player1, "Tyvar Kell");
    }

    @Test
    void zeroSnowManaReturnsOnlyZeroManaValueCreature() {
        Ornithopter eligible = new Ornithopter();
        harness.setGraveyard(player1, List.of(eligible, new LlanowarElves(), new GloriousAnthem()));
        harness.setHand(player1, List.of(new BloodOnTheSnow()));
        addBloodMana(0);

        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.GraveyardChoice choice = (PendingInteraction.GraveyardChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(0);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Glorious Anthem");
    }

    @Test
    void snowManaSpentOnBlackSymbolsCountsTowardReturnLimit() {
        HillGiant eligible = new HillGiant();
        harness.setGraveyard(player1, List.of(eligible, new GloriousAnthem()));
        harness.setGraveyard(player2, List.of(new LlanowarElves()));
        harness.setHand(player1, List.of(new BloodOnTheSnow()));
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSnowMana(ManaColor.BLACK, 2);
        pool.addSnowMana(ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.GraveyardChoice choice = (PendingInteraction.GraveyardChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(0);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertInGraveyard(player1, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    private void addBloodMana(int snowMana) {
        harness.addMana(player1, ManaColor.COLORLESS, 4 - snowMana);
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.add(ManaColor.BLACK, 2);
        pool.addSnowMana(ManaColor.COLORLESS, snowMana);
    }
}
