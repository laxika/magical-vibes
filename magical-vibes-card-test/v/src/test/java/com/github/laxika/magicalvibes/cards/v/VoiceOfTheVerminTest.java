package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Strangle;
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

@CardUsed({VoiceOfTheVermin.class, Murder.class, Strangle.class})
class VoiceOfTheVerminTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a shield counter")
    void entersWithShieldCounter() {
        Permanent voice = harness.enterBattlefieldAndReturn(player1, new VoiceOfTheVermin());

        assertThat(voice.getCounterCount(CounterType.SHIELD)).isOne();
    }

    @Test
    @DisplayName("Attacking lets me target a creature I control")
    void attackTriggerRestrictsTargets() {
        Permanent voice = addCreatureReady(player1, new VoiceOfTheVermin());
        Permanent ownCreature = addCreatureReady(player1, new VoiceOfTheVermin());
        Permanent opponentCreature = addCreatureReady(player2, new VoiceOfTheVermin());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(voice.getId(), ownCreature.getId())
                .doesNotContain(opponentCreature.getId());
    }

    @Test
    @DisplayName("Attacking sets the target creature's base power and toughness to 4/4 until end of turn")
    void attackTriggerSetsTargetBasePowerAndToughness() {
        addCreatureReady(player1, new VoiceOfTheVermin());
        Permanent ownCreature = addCreatureReady(player1, new VoiceOfTheVermin());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target itself and preserves power/toughness counters above the new base")
    void selfTargetKeepsCounterBonuses() {
        Permanent voice = addCreatureReady(player1, new VoiceOfTheVermin());
        voice.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, voice.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, voice)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, voice)).isEqualTo(5);

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gqs.getEffectivePower(gd, voice)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, voice)).isEqualTo(5);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, voice)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, voice)).isEqualTo(3);
        assertThat(voice.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isOne();
    }

    @Test
    @DisplayName("Shield prevents lethal damage once, then subsequent damage kills it")
    void shieldPreventsDamageOnce() {
        Permanent voice = harness.enterBattlefieldAndReturn(player2, new VoiceOfTheVermin());
        harness.setHand(player1, List.of(new Strangle(), new Strangle()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, voice.getId());

        harness.assertOnBattlefield(player2, "Voice of the Vermin");
        assertThat(voice.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(voice.getMarkedDamage()).isZero();

        harness.castAndResolveSorcery(player1, 0, voice.getId());

        harness.assertInGraveyard(player2, "Voice of the Vermin");
        harness.assertNotOnBattlefield(player2, "Voice of the Vermin");
    }

    @Test
    @DisplayName("Shield replaces destruction once without tapping the creature")
    void shieldReplacesDestructionOnce() {
        Permanent voice = harness.enterBattlefieldAndReturn(player2, new VoiceOfTheVermin());
        harness.setHand(player1, List.of(new Murder(), new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, voice.getId());

        harness.assertOnBattlefield(player2, "Voice of the Vermin");
        assertThat(voice.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(voice.isTapped()).isFalse();

        harness.castAndResolveInstant(player1, 0, voice.getId());

        harness.assertInGraveyard(player2, "Voice of the Vermin");
        harness.assertNotOnBattlefield(player2, "Voice of the Vermin");
    }
}
