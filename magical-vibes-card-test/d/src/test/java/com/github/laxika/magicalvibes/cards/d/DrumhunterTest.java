package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SkysovereignConsulFlagship;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Drumhunter.class, AvatarOfMight.class, GrizzlyBears.class,
        WoollyThoctar.class, SkysovereignConsulFlagship.class})
class DrumhunterTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card at end step when controlling a power-5-or-greater creature and accepting")
    void drawsWhenControllingBigCreature() {
        harness.addToBattlefield(player1, new Drumhunter());
        harness.addToBattlefield(player1, new AvatarOfMight()); // 8/8
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player1);
        harness.passBothPriorities(); // resolve trigger -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Declining the may ability draws no card")
    void decliningDrawsNoCard() {
        harness.addToBattlefield(player1, new Drumhunter());
        harness.addToBattlefield(player1, new AvatarOfMight()); // 8/8
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player1);
        harness.passBothPriorities(); // resolve trigger -> may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Does not trigger without a power-5-or-greater creature")
    void noTriggerWithoutBigCreature() {
        harness.addToBattlefield(player1, new Drumhunter()); // 2/2 only
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("{T}: Add {C} produces one colorless mana")
    void tapAddsColorlessMana() {
        var drumhunter = addCreatureReady(player1, new Drumhunter());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(drumhunter.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void drawsWithExactlyFivePower() {
        harness.addToBattlefield(player1, new Drumhunter());
        harness.addToBattlefield(player1, new WoollyThoctar());
        harness.setLibrary(player1, List.of(new Drumhunter()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    void opponentsCreatureDoesNotEnableTrigger() {
        harness.addToBattlefield(player1, new Drumhunter());
        harness.addToBattlefield(player2, new WoollyThoctar());

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new Drumhunter());
        harness.addToBattlefield(player1, new WoollyThoctar());

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotDrawIfQualifyingCreatureLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new Drumhunter());
        var creature = harness.addToBattlefieldAndReturn(player1, new WoollyThoctar());
        harness.setLibrary(player1, List.of(new Drumhunter()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void uncrewedVehicleDoesNotEnableTrigger() {
        harness.addToBattlefield(player1, new Drumhunter());
        harness.addToBattlefield(player1, new SkysovereignConsulFlagship());

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance POSTCOMBAT_MAIN -> END_STEP, triggers fire
    }
}
