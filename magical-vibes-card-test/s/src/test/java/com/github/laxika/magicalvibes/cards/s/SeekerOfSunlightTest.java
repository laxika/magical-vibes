package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeekerOfSunlight.class, Forest.class})
class SeekerOfSunlightTest extends BaseCardTest {

    @Test
    void exploringLandPutsItIntoHand() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        Permanent seeker = addCreatureReady(player1, new SeekerOfSunlight());
        addExploreMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(land.getId()));
        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void exploringNonLandAddsCounterAndMayPutItIntoGraveyard() {
        Card nonLand = new SeekerOfSunlight();
        harness.setLibrary(player1, List.of(nonLand));
        Permanent seeker = addCreatureReady(player1, new SeekerOfSunlight());
        addExploreMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(nonLand.getId()));
    }

    @Test
    void activationRequiresSorcerySpeed() {
        addCreatureReady(player1, new SeekerOfSunlight());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        addExploreMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void exploringNonLandMayLeaveItOnTop() {
        Card nonLand = new SeekerOfSunlight();
        harness.setLibrary(player1, List.of(nonLand));
        Permanent seeker = addCreatureReady(player1, new SeekerOfSunlight());
        addExploreMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonLand);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(nonLand);
        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void exploringEmptyLibraryStillAddsCounter() {
        harness.setLibrary(player1, List.of());
        Permanent seeker = addCreatureReady(player1, new SeekerOfSunlight());
        addExploreMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void tappedSummoningSickCreatureCanActivate() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new SeekerOfSunlight());
        seeker.setSummoningSick(true);
        seeker.tap();
        addExploreMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(seeker.isTapped()).isTrue();
    }

    @Test
    void activationRequiresOwnTurn() {
        addCreatureReady(player1, new SeekerOfSunlight());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        addExploreMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void activationRequiresEmptyStack() {
        addCreatureReady(player1, new SeekerOfSunlight());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");

        harness.setLibrary(player1, List.of(new Forest()));
        harness.passBothPriorities();
    }

    @Test
    void exploringStillMovesLandAfterSourceLeavesBattlefield() {
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        Permanent seeker = addCreatureReady(player1, new SeekerOfSunlight());
        addExploreMana();
        harness.activateAbility(player1, 0, null, null);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, seeker);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void addExploreMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
