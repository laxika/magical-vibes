package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AjaniVengeant;
import com.github.laxika.magicalvibes.cards.m.Mosstodon;
import com.github.laxika.magicalvibes.cards.s.SkysovereignConsulFlagship;
import com.github.laxika.magicalvibes.model.CounterType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExuberantFirestoker.class, Mosstodon.class, AjaniVengeant.class, SkysovereignConsulFlagship.class})
class ExuberantFirestokerTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to the chosen opponent when controlling a power-5-or-greater creature and accepting")
    void dealsDamageWhenControllingBigCreature() {
        harness.addToBattlefield(player1, new ExuberantFirestoker());
        harness.addToBattlefield(player1, new Mosstodon());
        harness.setLife(player2, 20);

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve trigger -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Any player is a legal target — controller may be chosen")
    void canTargetController() {
        harness.addToBattlefield(player1, new ExuberantFirestoker());
        harness.addToBattlefield(player1, new Mosstodon());
        harness.setLife(player1, 20);

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player1.getId(), player2.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities(); // resolve trigger -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Declining the may ability deals no damage")
    void decliningDealsNoDamage() {
        harness.addToBattlefield(player1, new ExuberantFirestoker());
        harness.addToBattlefield(player1, new Mosstodon());
        harness.setLife(player2, 20);

        advanceToEndStep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // resolve trigger -> may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger without a power-5-or-greater creature")
    void noTriggerWithoutBigCreature() {
        harness.addToBattlefield(player1, new ExuberantFirestoker()); // 1/1 only
        harness.setLife(player2, 20);

        advanceToEndStep(player1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("{T}: Add {C} produces one colorless mana")
    void tapAddsColorlessMana() {
        addCreatureReady(player1, new ExuberantFirestoker());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can damage a planeswalker but cannot target a creature")
    void damagesPlaneswalkerAndExcludesCreatureTargets() {
        Permanent firestoker = harness.addToBattlefieldAndReturn(player1, new ExuberantFirestoker());
        Permanent mosstodon = harness.addToBattlefieldAndReturn(player1, new Mosstodon());
        Permanent ajani = harness.addToBattlefieldAndReturn(player2, new AjaniVengeant());
        ajani.setCounterCount(CounterType.LOYALTY, 3);

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ajani.getId(), player1.getId(), player2.getId())
                .doesNotContain(firestoker.getId(), mosstodon.getId());
        harness.handlePermanentChosen(player1, ajani.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ajani.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger during the opponent's end step")
    void noTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new ExuberantFirestoker());
        harness.addToBattlefield(player1, new Mosstodon());

        advanceToEndStep(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's large creature does not satisfy the condition")
    void opponentsCreatureDoesNotEnableTrigger() {
        harness.addToBattlefield(player1, new ExuberantFirestoker());
        harness.addToBattlefield(player2, new Mosstodon());

        advanceToEndStep(player1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Uses current power rather than printed power at the beginning of the end step")
    void reducedPowerDoesNotEnableTrigger() {
        harness.addToBattlefield(player1, new ExuberantFirestoker());
        Permanent mosstodon = harness.addToBattlefieldAndReturn(player1, new Mosstodon());
        mosstodon.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        advanceToEndStep(player1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Rechecks the large-creature condition when the trigger resolves")
    void conditionMustStillHoldAtResolution() {
        harness.addToBattlefield(player1, new ExuberantFirestoker());
        Permanent mosstodon = harness.addToBattlefieldAndReturn(player1, new Mosstodon());

        advanceToEndStep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        mosstodon.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @CardUsed({ExuberantFirestoker.class, SkysovereignConsulFlagship.class})
    @DisplayName("An uncrewed Vehicle does not satisfy the creature condition")
    void uncrewedVehicleDoesNotEnableTrigger() {
        harness.addToBattlefield(player1, new ExuberantFirestoker());
        harness.addToBattlefield(player1, new SkysovereignConsulFlagship());

        advanceToEndStep(player1);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Firestoker itself can satisfy the condition after its power increases")
    void firestokerCanEnableItsOwnTrigger() {
        Permanent firestoker = harness.addToBattlefieldAndReturn(player1, new ExuberantFirestoker());
        firestoker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        advanceToEndStep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Mana resolves immediately and tapping prevents another activation")
    void manaAbilityTapsAndDoesNotUseTheStack() {
        Permanent firestoker = addCreatureReady(player1, new ExuberantFirestoker());

        harness.activateAbility(player1, 0, null, null);

        assertThat(firestoker.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Summoning sickness prevents tapping for mana")
    void summoningSicknessPreventsManaActivation() {
        harness.addToBattlefield(player1, new ExuberantFirestoker());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player, TurnStep.END_STEP);
    }
}
