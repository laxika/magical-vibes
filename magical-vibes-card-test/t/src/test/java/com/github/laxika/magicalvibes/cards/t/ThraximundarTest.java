package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.d.DiabolicEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LilianaTheLastHope;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
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

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Thraximundar.class, DiabolicEdict.class, GrizzlyBears.class, SuntailHawk.class,
        Terminate.class, TreetopVillage.class, Boomerang.class, LilianaTheLastHope.class})
class ThraximundarTest extends BaseCardTest {

    private Permanent thraximundar() {
        return findPermanent(player1, "Thraximundar");
    }

    @Test
    @DisplayName("When another player sacrifices a creature, may put a +1/+1 counter on Thraximundar (accept)")
    void anyPlayerSacrificesAccept() {
        addCreatureReady(player1, new Thraximundar());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castEdictAt(player2);
        // Resolve Diabolic Edict, then Thraximundar's optional counter trigger.
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(thraximundar().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the sacrifice trigger leaves Thraximundar without a counter")
    void anyPlayerSacrificesDecline() {
        addCreatureReady(player1, new Thraximundar());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castEdictAt(player2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(thraximundar().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The controller's own sacrifice also triggers the counter (any player)")
    void controllerOwnSacrificeTriggers() {
        addCreatureReady(player1, new Thraximundar());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castEdictAt(player1);
        harness.passBothPriorities();

        // player1 controls both Thraximundar and Grizzly Bears, so Diabolic Edict prompts a choice.
        harness.handlePermanentChosen(player1, bears.getId());
        // Resolve the queued sacrifice trigger so its "you may" surfaces.
        harness.passBothPriorities();

        // Sacrificing the controller's own creature still triggers Thraximundar's counter ability.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(thraximundar().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking makes the defending player sacrifice their lone creature, then Thraximundar may grow")
    void attackForcesDefenderSacrificeThenCounter() {
        Permanent thrax = addCreatureReady(player1, new Thraximundar());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SuntailHawk());

        declareAttackers(player1, List.of(0));
        // Resolve the attack trigger so player2 sacrifices its only creature.
        harness.passBothPriorities();
        harness.passBothPriorities();

        // The defending player's creature is gone; the attacker's own creature is untouched.
        harness.assertNotOnBattlefield(player2, "Suntail Hawk");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);

        // The sacrifice also triggers the +1/+1 counter ability for the attacker.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(thrax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The defending player chooses which creature to sacrifice when they control several")
    void attackDefenderChoosesWhichToSacrifice() {
        Permanent thrax = addCreatureReady(player1, new Thraximundar());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent hawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        // The defending player (player2) is prompted to choose the creature to sacrifice.
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());

        harness.handlePermanentChosen(player2, hawk.getId());

        // The chosen creature is sacrificed; the other survives.
        harness.assertNotOnBattlefield(player2, "Suntail Hawk");
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        // Resolve the queued sacrifice trigger so its "you may" surfaces.
        harness.passBothPriorities();

        // The interactive sacrifice choice also triggers Thraximundar's +1/+1 counter ability.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        assertThat(thrax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking a defender with no creatures sacrifices nothing and grants no counter")
    void attackDefenderWithNoCreatures() {
        Permanent thrax = addCreatureReady(player1, new Thraximundar());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(thrax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }


    @Test
    @DisplayName("Haste allows Thraximundar to attack while summoning sick")
    void canAttackImmediately() {
        Permanent thrax = harness.addToBattlefieldAndReturn(player1, new Thraximundar());
        thrax.setSummoningSick(true);

        declareAttackers(player1, List.of(0));

        assertThat(thrax.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(thrax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Destroying a creature does not trigger the sacrifice ability")
    void destructionDoesNotGrantCounter() {
        Permanent thrax = addCreatureReady(player1, new Thraximundar());
        Permanent opposingThrax = harness.addToBattlefieldAndReturn(player2, new Thraximundar());
        harness.setHand(player1, List.of(new Terminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, opposingThrax.getId());

        harness.assertInGraveyard(player2, "Thraximundar");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(thrax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The defending player still sacrifices if Thraximundar leaves before its attack trigger resolves")
    void attackTriggerSurvivesSourceRemoval() {
        Permanent thrax = addCreatureReady(player1, new Thraximundar());
        harness.addToBattlefield(player2, new GrizzlyBears());
        declareAttackers(player1, List.of(0));

        harness.setHand(player2, List.of(new Terminate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, thrax.getId());
        harness.assertInGraveyard(player1, "Thraximundar");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Sacrificing an animated land triggers the optional counter ability")
    void animatedLandSacrificeGrantsCounter() {
        Permanent thrax = addCreatureReady(player1, new Thraximundar());
        Permanent village = harness.addToBattlefieldAndReturn(player2, new TreetopVillage());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, village)).isTrue();

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Treetop Village");
        harness.passBothPriorities();

        PendingInteraction.MayAbilityChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        assertThat(thrax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The defending player still sacrifices after the attacked planeswalker leaves")
    void attackTriggerSurvivesAttackedPlaneswalkerLeaving() {
        Permanent thrax = addCreatureReady(player1, new Thraximundar());
        Permanent liliana = harness.addToBattlefieldAndReturn(player2, new LilianaTheLastHope());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);
            harness.clearPriorityPassed();
            harness.beginAttackerDeclarationInput();
            gs.declareAttackers(gd, player1, List.of(0), Map.of(0, liliana.getId()));
            harness.castAndResolveInstant(player1, 0, liliana.getId());
            harness.assertNotOnBattlefield(player2, "Liliana, the Last Hope");
            harness.assertInHand(player2, "Liliana, the Last Hope");

            harness.passBothPriorities();
            harness.assertInGraveyard(player2, "Grizzly Bears");
            harness.passBothPriorities();
            PendingInteraction.MayAbilityChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
            assertThat(choice).isNotNull();
            assertThat(choice.playerId()).isEqualTo(player1.getId());
            harness.handleMayAbilityChosen(player1, true);
        });

        assertThat(thrax.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void castEdictAt(Player target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, target.getId());
    }
}
