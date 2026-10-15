package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.MentorOfTheMeek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({SummonersSending.class, AirElemental.class, GrizzlyBears.class, Shock.class,
        HillGiant.class, GloriousAnthem.class, MentorOfTheMeek.class})
class SummonersSendingTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling a creature with mana value 4 or greater creates a countered Spirit")
    void createsCounteredSpiritForHighManaValueCreature() {
        AirElemental target = new AirElemental();
        harness.setGraveyard(player2, List.of(target));
        harness.addToBattlefield(player1, new SummonersSending());

        resolveEndStepAbility(target);

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player2, "Air Elemental");
    }

    @Test
    @DisplayName("Exiling a lower-mana-value creature creates a Spirit without a counter")
    void createsUncounteredSpiritForLowManaValueCreature() {
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.addToBattlefield(player1, new SummonersSending());

        resolveEndStepAbility(target);

        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Declining the optional ability leaves the creature card in the graveyard")
    void decliningLeavesGraveyardUnchanged() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.addToBattlefield(player1, new SummonersSending());

        advanceToEndStep();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("The ability is not put on the stack without a creature card target")
    void requiresCreatureCardTarget() {
        harness.setGraveyard(player2, List.of(new Shock()));
        harness.addToBattlefield(player1, new SummonersSending());

        advanceToEndStep();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("A creature with mana value exactly four in your graveyard gives the Spirit a counter")
    void createsCounteredSpiritAtThresholdFromOwnGraveyard() {
        HillGiant target = new HillGiant();
        harness.setGraveyard(player1, List.of(target));
        harness.addToBattlefield(player1, new SummonersSending());

        resolveEndStepAbility(target);

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        assertThat(findPermanent(player1, "Spirit").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Hill Giant");
    }

    @Test
    @DisplayName("The ability does not trigger during an opponent's end step")
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new SummonersSending());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("A target removed from the graveyard before resolution creates no Spirit")
    void removedTargetCreatesNoSpirit() {
        GrizzlyBears target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        harness.addToBattlefield(player1, new SummonersSending());
        advanceToEndStep();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(target));

        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The Spirit receives its counter after entering and triggering Mentor of the Meek")
    void counterIsPlacedAfterTheSpiritEnters() {
        HillGiant target = new HillGiant();
        Shock draw = new Shock();
        harness.setLibrary(player1, List.of(draw));
        harness.setHand(player1, List.of());
        harness.setGraveyard(player2, List.of(target));
        harness.addToBattlefield(player1, new MentorOfTheMeek());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player1, new SummonersSending());

        resolveEndStepAbility(target);

        assertThat(findPermanent(player1, "Spirit").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
    }

    private void resolveEndStepAbility(Card target) {
        advanceToEndStep();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
    }
}
