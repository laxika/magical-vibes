package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DeepCavernBat;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CanonizedInBlood.class, DeepCavernBat.class, Forest.class, GrizzlyBears.class, ZuranOrb.class})
class CanonizedInBloodTest extends BaseCardTest {

    @Test
    @DisplayName("At your end step after descending, puts a +1/+1 counter on a creature you control")
    void descendedEndStepPutsCounterOnTargetCreature() {
        Permanent canonized = addCanonizedInBlood();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        sacrificeForestToDescend();

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(canonized.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger at your end step without descending")
    void doesNotTriggerWithoutDescending() {
        addCanonizedInBlood();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The end-step trigger only offers creatures controlled by its controller")
    void endStepTriggerOnlyTargetsOwnCreature() {
        addCanonizedInBlood();
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        sacrificeForestToDescend();

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownBears.getId())
                .doesNotContain(opposingBears.getId());
    }

    @Test
    @DisplayName("Sacrificing the enchantment creates a 4/3 flying white and black Vampire Demon")
    void sacrificeCreatesVampireDemonToken() {
        Permanent canonized = addCanonizedInBlood();
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(canonized);
        Permanent token = findPermanent(player1, "Vampire Demon");
        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(3);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.VAMPIRE, CardSubtype.DEMON);
        assertThat(token.getCard().hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Descending on an opponent's turn does not trigger at their end step")
    void doesNotTriggerAtOpponentsEndStep() {
        addCanonizedInBlood();
        harness.addToBattlefield(player1, new CanonizedInBlood());
        harness.forceActivePlayer(player2);
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Vampire Demon");

        advanceToEndStep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent descending does not enable your end-step trigger")
    void opponentsDescentDoesNotEnableTrigger() {
        addCanonizedInBlood();
        Permanent bat = harness.addToBattlefieldAndReturn(player1, new DeepCavernBat());
        harness.addToBattlefield(player2, new CanonizedInBlood());
        harness.addMana(player2, ManaColor.BLACK, 7);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(bat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The counter trigger resolves even if the enchantment is sacrificed in response")
    void triggerResolvesAfterSourceIsSacrificed() {
        Permanent canonized = addCanonizedInBlood();
        harness.addToBattlefield(player1, new CanonizedInBlood());
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Vampire Demon");

        advanceToEndStep(player1);
        harness.handlePermanentChosen(player1, token.getId());
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(canonized);
        harness.assertInGraveyard(player1, "Canonized in Blood");
        resolveAllTriggers();

        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Vampire Demon")).isEqualTo(2);
    }

    @Test
    @DisplayName("Descending after the end step begins does not retroactively trigger the ability")
    void descendingAfterEndStepBeginsDoesNotTrigger() {
        addCanonizedInBlood();
        harness.addToBattlefield(player1, new CanonizedInBlood());
        Permanent bat = harness.addToBattlefieldAndReturn(player1, new DeepCavernBat());
        advanceToEndStep(player1);

        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(bat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Vampire Demon")).isEqualTo(1);
    }

    private Permanent addCanonizedInBlood() {
        return harness.addToBattlefieldAndReturn(player1, new CanonizedInBlood());
    }

    private void sacrificeForestToDescend() {
        harness.addToBattlefield(player1, new ZuranOrb());
        harness.addToBattlefield(player1, new Forest());
        harness.activateAbility(player1, 2, null, null);
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
