package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.t.TreetopVillage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlossomingTortoise.class, Forest.class, GrizzlyBears.class, Opt.class, TreetopVillage.class})
class BlossomingTortoiseTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, it mills three cards and returns a land tapped")
    void entersMillsAndReturnsLandTapped() {
        Forest land = new Forest();
        Opt milledOne = new Opt();
        Opt milledTwo = new Opt();
        Opt milledThree = new Opt();
        harness.setGraveyard(player1, List.of(land));
        harness.setLibrary(player1, List.of(milledOne, milledTwo, milledThree));

        castTortoise();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice.validIndices()).containsExactly(0);
        assertThat(choice.mandatory()).isTrue();
        assertThat(choice.enterTapped()).isTrue();

        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent returnedLand = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == land)
                .findFirst()
                .orElseThrow();
        assertThat(returnedLand.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(milledOne, milledTwo, milledThree);
    }

    @Test
    @DisplayName("Attacking mills three cards and returns a land tapped")
    void attackingMillsAndReturnsLandTapped() {
        addCreatureReady(player1, new BlossomingTortoise());
        Forest land = new Forest();
        Opt milledOne = new Opt();
        Opt milledTwo = new Opt();
        Opt milledThree = new Opt();
        harness.setGraveyard(player1, List.of(land));
        harness.setLibrary(player1, List.of(milledOne, milledTwo, milledThree));

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)
                .validIndices()).containsExactly(0);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent returnedLand = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == land)
                .findFirst()
                .orElseThrow();
        assertThat(returnedLand.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(milledOne, milledTwo, milledThree);
    }

    @Test
    @DisplayName("Its static abilities reduce own land activations and boost land creatures")
    void reducesOwnLandActivationsAndBoostsLandCreatures() {
        addCreatureReady(player1, new BlossomingTortoise());
        Permanent treetop = harness.addToBattlefieldAndReturn(player1, new TreetopVillage());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        treetop.setSummoningSick(false);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, treetop)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, treetop)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    private void castTortoise() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BlossomingTortoise()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
