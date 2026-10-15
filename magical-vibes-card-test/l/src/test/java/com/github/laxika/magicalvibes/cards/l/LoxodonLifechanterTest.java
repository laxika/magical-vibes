package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AngelOfVitality;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoxodonLifechanter.class, GreenwoodSentinel.class, AngelOfVitality.class, Unsummon.class})
class LoxodonLifechanterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may set life to the total toughness of creatures you control")
    void etbMaySetLifeToControlledCreatureToughness() {
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.setLife(player1, 5);

        harness.castFromHand(player1, new LoxodonLifechanter(), "{5}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 8);
    }

    @Test
    @DisplayName("ETB life-total change can be declined")
    void etbLifeTotalChangeCanBeDeclined() {
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.setLife(player1, 5);

        harness.castFromHand(player1, new LoxodonLifechanter(), "{5}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 5);
    }

    @Test
    @DisplayName("Activated ability uses the current life total for X")
    void activatedAbilityUsesCurrentLifeTotal() {
        Permanent loxodon = harness.addToBattlefieldAndReturn(player1, new LoxodonLifechanter());
        loxodon.setSummoningSick(false);
        harness.setLife(player1, 7);
        addLoxodonMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(loxodon.getEffectivePower()).isEqualTo(11);
        assertThat(loxodon.getEffectiveToughness()).isEqualTo(13);
    }

    @Test
    @DisplayName("Accepting the entry trigger can lower life and excludes opposing creatures")
    void etbCanLowerLifeAndCountsOnlyControlledCreatures() {
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.addToBattlefield(player2, new GreenwoodSentinel());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new LoxodonLifechanter(), "{5}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 8);
        harness.assertLife(player2, 20);
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.assertLife(player1, 8);
    }

    @Test
    @DisplayName("Activating before the entry trigger resolves changes the toughness it counts")
    void activationBeforeEntryTriggerUsesBoostedToughness() {
        harness.setLife(player1, 5);
        harness.castFromHand(player1, new LoxodonLifechanter(), "{5}{W}");
        harness.passBothPriorities();
        addLoxodonMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 11);
        Permanent loxodon = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(loxodon.getEffectivePower()).isEqualTo(9);
        assertThat(loxodon.getEffectiveToughness()).isEqualTo(11);
    }

    @Test
    @DisplayName("The boost uses life at resolution and does not track later life changes")
    void boostSnapshotsLifeAtResolution() {
        Permanent loxodon = harness.addToBattlefieldAndReturn(player1, new LoxodonLifechanter());
        harness.setLife(player1, 7);
        addLoxodonMana();
        harness.activateAbility(player1, 0, null, null);
        harness.setLife(player1, 10);

        harness.passBothPriorities();
        assertThat(loxodon.getEffectivePower()).isEqualTo(14);
        assertThat(loxodon.getEffectiveToughness()).isEqualTo(16);

        harness.setLife(player1, 3);
        assertThat(loxodon.getEffectivePower()).isEqualTo(14);
        assertThat(loxodon.getEffectiveToughness()).isEqualTo(16);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);
        assertThat(loxodon.getEffectivePower()).isEqualTo(4);
        assertThat(loxodon.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Multiple activations add independently determined boosts")
    void multipleActivationsStack() {
        Permanent loxodon = harness.addToBattlefieldAndReturn(player1, new LoxodonLifechanter());
        harness.setLife(player1, 7);
        addLoxodonMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setLife(player1, 3);
        addLoxodonMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(loxodon.getEffectivePower()).isEqualTo(14);
        assertThat(loxodon.getEffectiveToughness()).isEqualTo(16);
    }

    @Test
    @DisplayName("The entry trigger still resolves after the source leaves and excludes its toughness")
    void entryTriggerSurvivesSourceLeavingBattlefield() {
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.setLife(player1, 10);
        harness.castFromHand(player1, new LoxodonLifechanter(), "{5}{W}");
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Loxodon Lifechanter"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 2);
        harness.assertInHand(player1, "Loxodon Lifechanter");
    }

    @Test
    @DisplayName("Setting life applies life-gain replacement effects")
    void entryLifeGainCanBeModifiedByReplacementEffect() {
        harness.addToBattlefield(player1, new AngelOfVitality());
        harness.setLife(player1, 5);
        harness.castFromHand(player1, new LoxodonLifechanter(), "{5}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 9);
    }

    private void addLoxodonMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
