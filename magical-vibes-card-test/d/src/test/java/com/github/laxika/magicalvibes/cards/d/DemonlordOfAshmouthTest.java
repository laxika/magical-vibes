package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.MoorlandInquisitor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DemonlordOfAshmouth.class, MoorlandInquisitor.class, DeathWind.class})
class DemonlordOfAshmouthTest extends BaseCardTest {

    private void castDemonlord() {
        harness.castFromHand(player1, new DemonlordOfAshmouth(), "{2}{B}{B}");
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB
    }

    private boolean demonlordExiled() {
        return gd.exiledCards.stream()
                .anyMatch(e -> e.card().getName().equals("Demonlord of Ashmouth"));
    }

    @Test
    @DisplayName("Sacrificing another creature keeps the Demonlord on the battlefield")
    void sacrificeKeepsDemonlord() {
        harness.addToBattlefield(player1, new MoorlandInquisitor());
        castDemonlord();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Moorland Inquisitor");
        harness.assertOnBattlefield(player1, "Demonlord of Ashmouth");
        assertThat(demonlordExiled()).isFalse();
    }

    @Test
    @DisplayName("Declining the sacrifice exiles the Demonlord — undying does not return it")
    void decliningExilesDemonlord() {
        harness.addToBattlefield(player1, new MoorlandInquisitor());
        castDemonlord();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Demonlord of Ashmouth");
        harness.assertNotInGraveyard(player1, "Demonlord of Ashmouth");
        harness.assertOnBattlefield(player1, "Moorland Inquisitor");
        assertThat(demonlordExiled()).isTrue();
    }

    @Test
    @DisplayName("With no other creature the Demonlord is exiled without a prompt")
    void noOtherCreatureExilesWithoutPrompt() {
        castDemonlord();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Demonlord of Ashmouth");
        assertThat(demonlordExiled()).isTrue();
    }

    @Test
    @DisplayName("An opponent's creature can't be sacrificed to save the Demonlord")
    void opponentCreatureDoesNotCount() {
        harness.addToBattlefield(player2, new MoorlandInquisitor());
        castDemonlord();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(demonlordExiled()).isTrue();
        harness.assertOnBattlefield(player2, "Moorland Inquisitor");
    }

    @Test
    @DisplayName("Undying returns the Demonlord but its entry trigger still requires another creature")
    void undyingReturnExilesWithoutAnotherCreature() {
        var demon = harness.addToBattlefieldAndReturn(player1, new DemonlordOfAshmouth());
        harness.setHand(player1, List.of(new DeathWind()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castInstant(player1, 0, 4, demon.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Demonlord of Ashmouth");
        harness.passBothPriorities();

        var returned = findPermanent(player1, "Demonlord of Ashmouth");
        assertThat(returned.getId()).isNotEqualTo(demon.getId());
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Demonlord of Ashmouth");
        harness.assertNotInGraveyard(player1, "Demonlord of Ashmouth");
        assertThat(demonlordExiled()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A returned Demonlord can sacrifice another creature and does not return after dying again")
    void undyingReturnCanPaySacrificeButOnlyReturnsOnce() {
        var demon = harness.addToBattlefieldAndReturn(player1, new DemonlordOfAshmouth());
        harness.addToBattlefield(player1, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new DeathWind(), new DeathWind()));
        harness.addMana(player1, ManaColor.BLACK, 11);

        harness.castInstant(player1, 0, 4, demon.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        var returned = findPermanent(player1, "Demonlord of Ashmouth");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Moorland Inquisitor");
        harness.assertNotInGraveyard(player1, "Demonlord of Ashmouth");
        assertThat(demonlordExiled()).isFalse();

        harness.castInstant(player1, 0, 5, returned.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Demonlord of Ashmouth");
        harness.assertInGraveyard(player1, "Demonlord of Ashmouth");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("With multiple other creatures the controller chooses which one to sacrifice")
    void choosesAnotherCreatureToSacrifice() {
        var first = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        var second = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        castDemonlord();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, second.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(p -> p.getId()).contains(first.getId()).doesNotContain(second.getId());
        harness.assertInGraveyard(player1, "Moorland Inquisitor");
        harness.assertOnBattlefield(player1, "Demonlord of Ashmouth");
        assertThat(demonlordExiled()).isFalse();
    }

    @Test
    @DisplayName("The controller at death controls undying and the owner controls the return entry trigger")
    void stolenDemonlordReturnsToOwnerWithCorrectTriggerControllers() {
        var demon = harness.addToBattlefieldAndReturn(player1, new DemonlordOfAshmouth());
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(CreatureControlService.class)
                .applyControlEffect(gd, player2.getId(), demon,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT), EffectDuration.PERMANENT,
                        null, "Test setup"));
        harness.addToBattlefield(player1, new MoorlandInquisitor());
        harness.setHand(player2, List.of(new DeathWind()));
        harness.addMana(player2, ManaColor.BLACK, 5);

        harness.castInstant(player2, 0, 4, demon.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Demonlord of Ashmouth");
        assertThat(gd.stack).singleElement().satisfies(trigger ->
                assertThat(trigger.getControllerId()).isEqualTo(player2.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Demonlord of Ashmouth");
        harness.assertNotOnBattlefield(player2, "Demonlord of Ashmouth");
        assertThat(gd.stack).singleElement().satisfies(trigger ->
                assertThat(trigger.getControllerId()).isEqualTo(player1.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Moorland Inquisitor");
        harness.assertOnBattlefield(player1, "Demonlord of Ashmouth");
        assertThat(findPermanent(player1, "Demonlord of Ashmouth")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
