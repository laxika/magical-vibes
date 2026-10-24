package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.Annihilate;
import com.github.laxika.magicalvibes.cards.d.DiabolicEdict;
import com.github.laxika.magicalvibes.cards.f.FlayingTendrils;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GisaGloriousResurrector;
import com.github.laxika.magicalvibes.cards.g.GravebaneZombie;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HighcliffFelidar;
import com.github.laxika.magicalvibes.cards.i.ImperialEdict;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.cards.l.LiesaForgottenArchangel;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SlaughterDrone;
import com.github.laxika.magicalvibes.cards.t.TarSnare;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.cards.v.VampireEnvoy;
import com.github.laxika.magicalvibes.cards.w.WheelOfSunAndMoon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FailOnLegacyDepartureFallback;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KalitasTraitorOfGhet.class, Shock.class, SlaughterDrone.class,
        TarSnare.class, TurnToFrog.class, VampireEnvoy.class, FlayingTendrils.class})
@FailOnLegacyDepartureFallback
class KalitasTraitorOfGhetTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles an opponent's nontoken creature and creates a Zombie")
    void exilesNontokenOpponentCreatureAndCreatesZombie() {
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new SlaughterDrone());

        destroyCreature(player1, bears);

        harness.assertNotInGraveyard(player2, "Slaughter Drone");
        assertThat(gd.findExiledCard(bears.getCard().getId())).isNotNull();
        assertThat(countTokens(player1, "Zombie")).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not replace an opponent's token creature")
    void doesNotReplaceTokenCreature() {
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent token = addCreature(player2, "Bear Token", List.of(CardSubtype.BEAR), true);

        destroyCreature(player1, token);

        assertThat(gd.findExiledCard(token.getCard().getId())).isNull();
        assertThat(countTokens(player1, "Zombie")).isZero();
    }

    @Test
    @DisplayName("Sacrificing another Vampire or Zombie puts two +1/+1 counters on Kalitas")
    void sacrificesAnotherVampireOrZombieForCounters() {
        Permanent kalitas = harness.addToBattlefieldAndReturn(player1, new KalitasTraitorOfGhet());
        harness.addToBattlefield(player1, new VampireEnvoy());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(kalitas.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Vampire Envoy");
    }

    private void destroyCreature(Player caster, Permanent target) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castAndResolveInstant(caster, 0, target.getId());
    }

    @Test
    void doesNotExileItsControllersCreature() {
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent drone = harness.addToBattlefieldAndReturn(player1, new SlaughterDrone());

        killDrone(drone);

        harness.assertInGraveyard(player1, "Slaughter Drone");
        assertThat(gd.findExiledCard(drone.getCard().getId())).isNull();
        assertThat(countTokens(player1, "Zombie")).isZero();
    }

    @Test
    void losingAbilitiesDisablesDeathReplacement() {
        Permanent kalitas = harness.addToBattlefieldAndReturn(player1, new KalitasTraitorOfGhet());
        Permanent drone = harness.addToBattlefieldAndReturn(player2, new SlaughterDrone());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, kalitas.getId());

        killDrone(drone);

        harness.assertInGraveyard(player2, "Slaughter Drone");
        assertThat(gd.findExiledCard(drone.getCard().getId())).isNull();
        assertThat(countTokens(player1, "Zombie")).isZero();
    }

    @Test
    void opponentChoosesBetweenKalitasAndFlayingTendrilsReplacements() {
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        harness.addToBattlefield(player2, new SlaughterDrone());

        harness.castFromHand(player1, new FlayingTendrils(), "{1}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(countTokens(player1, "Zombie")).isZero();
    }

    @Test
    void choosingKalitasReplacementExilesAndCreatesZombie() {
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent drone = harness.addToBattlefieldAndReturn(player2, new SlaughterDrone());
        harness.castFromHand(player1, new FlayingTendrils(), "{1}{B}{B}");
        harness.passBothPriorities();

        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        harness.handleListChoice(player2, "Kalitas, Traitor of Ghet");

        assertThat(gd.findExiledCard(drone.getCard().getId())).isNotNull();
        harness.assertNotInGraveyard(player2, "Slaughter Drone");
        assertThat(countTokens(player1, "Zombie")).isEqualTo(1);
    }

    @Test
    void choosingFlayingTendrilsReplacementExilesWithoutZombie() {
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent drone = harness.addToBattlefieldAndReturn(player2, new SlaughterDrone());
        harness.castFromHand(player1, new FlayingTendrils(), "{1}{B}{B}");
        harness.passBothPriorities();

        harness.handleListChoice(player2, "Exile it");

        assertThat(gd.findExiledCard(drone.getCard().getId())).isNotNull();
        harness.assertNotInGraveyard(player2, "Slaughter Drone");
        harness.assertNotOnBattlefield(player2, "Slaughter Drone");
        assertThat(countTokens(player1, "Zombie")).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void stateBasedDeathAsksBeforeTheCreatureLeaves() {
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent drone = harness.addToBattlefieldAndReturn(player2, new SlaughterDrone());
        harness.castFromHand(player1, new FlayingTendrils(), "{1}{B}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.assertOnBattlefield(player2, "Slaughter Drone");
        assertThat(gd.findExiledCard(drone.getCard().getId())).isNull();
    }

    @Test
    @CardUsed({LiesaForgottenArchangel.class, DiabolicEdict.class, GrizzlyBears.class})
    void sacrificedCreatureStaysUntilItsControllerChoosesTheExileReplacement() {
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        harness.addToBattlefield(player1, new LiesaForgottenArchangel());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        // Both replacements exile it, but the choice is still made before the event (CR 616.1).
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.findExiledCard(bears.getCard().getId())).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleListChoice(player2, "Kalitas, Traitor of Ghet");

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.findExiledCard(bears.getCard().getId())).isNotNull();
        assertThat(countTokens(player1, "Zombie")).isEqualTo(1);
    }

    @Test
    @CardUsed({LiesaForgottenArchangel.class, Annihilate.class, GrizzlyBears.class, Forest.class})
    void laterEffectsOfTheSpellWaitForTheReplacementChoice() {
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        harness.addToBattlefield(player1, new LiesaForgottenArchangel());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Annihilate()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.handleListChoice(player2, "Kalitas, Traitor of Ghet");

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.findExiledCard(bears.getCard().getId())).isNotNull();
        assertThat(countTokens(player1, "Zombie")).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @CardUsed({GisaGloriousResurrector.class, GrizzlyBears.class})
    void simultaneousDeathsChooseInApnapOrderThenLeaveTogether() {
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GisaGloriousResurrector());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new FlayingTendrils(), "{1}{B}{B}");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleListChoice(player1, "Exile it");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.findExiledCard(ownBears.getCard().getId())).isNull();
        harness.handleListChoice(player2, "Kalitas, Traitor of Ghet");

        assertThat(gd.findExiledCard(ownBears.getCard().getId()).sourcePermanentId()).isNull();
        assertThat(gd.findExiledCard(opposingBears.getCard().getId())).isNotNull();
        assertThat(countTokens(player1, "Zombie")).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({GravebaneZombie.class, GrizzlyBears.class})
    void creatureWithLibraryReplacementChoosesBeforeTheStateBasedBatchDies() {
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent gravebane = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new FlayingTendrils(), "{1}{B}{B}");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .contains("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.handleListChoice(player2, "Put it into its owner's library");
        harness.assertOnBattlefield(player2, "Gravebane Zombie");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.handleListChoice(player2, "Exile it");

        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getId()).isEqualTo(gravebane.getCard().getId());
        assertThat(gd.findExiledCard(bears.getCard().getId())).isNotNull();
        assertThat(countTokens(player1, "Zombie")).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({GravebaneZombie.class})
    void choosingKalitasOverOwnLibraryReplacementExilesAndCreatesZombie() {
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent gravebane = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());

        destroyCreature(player1, gravebane);
        harness.handleListChoice(player2, "Kalitas, Traitor of Ghet");

        assertThat(gd.findExiledCard(gravebane.getCard().getId())).isNotNull();
        assertThat(countTokens(player1, "Zombie")).isEqualTo(1);
    }

    @Test
    @CardUsed({GravebaneZombie.class, Murder.class})
    @DisplayName("Murder on Gravebane Zombie asks library-or-Kalitas while Murder resolves, then the Zombie leaves")
    void murderAsksForTheDeathReplacementBeforeTheZombieLeaves() {
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent gravebane = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        Card murder = new Murder();
        harness.setHand(player1, List.of(murder));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, gravebane.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.stack).extracting(entry -> entry.getCard().getId()).containsExactly(murder.getId());
        harness.assertOnBattlefield(player2, "Gravebane Zombie");

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getId()).isEqualTo(gravebane.getCard().getId());
        harness.assertNotOnBattlefield(player2, "Gravebane Zombie");
        harness.assertInGraveyard(player1, "Murder");
        assertThat(gd.stack).isEmpty();
        assertThat(countTokens(player1, "Zombie")).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({GravebaneZombie.class, DiabolicEdict.class})
    @DisplayName("Diabolic Edict on a lone Gravebane Zombie asks library-or-Kalitas before the sacrifice")
    void edictAsksForTheDeathReplacementBeforeTheZombieIsSacrificed() {
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent gravebane = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        Card edict = new DiabolicEdict();
        harness.setHand(player1, List.of(edict));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.stack).extracting(entry -> entry.getCard().getId()).containsExactly(edict.getId());
        harness.assertOnBattlefield(player2, "Gravebane Zombie");
        assertThat(gd.playersWhoSacrificedPermanentsThisTurn).doesNotContain(player2.getId());

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getId()).isEqualTo(gravebane.getCard().getId());
        harness.assertNotOnBattlefield(player2, "Gravebane Zombie");
        assertThat(gd.playersWhoSacrificedPermanentsThisTurn).contains(player2.getId());
        harness.assertInGraveyard(player1, "Diabolic Edict");
        assertThat(gd.stack).isEmpty();
        assertThat(countTokens(player1, "Zombie")).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({GravebaneZombie.class, DiabolicEdict.class, GrizzlyBears.class})
    @DisplayName("Diabolic Edict asks library-or-Kalitas after the Zombie is chosen, before it is sacrificed")
    void edictChoiceAsksForTheDeathReplacementBeforeTheChosenZombieIsSacrificed() {
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent gravebane = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card edict = new DiabolicEdict();
        harness.setHand(player1, List.of(edict));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player2, gravebane.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Gravebane Zombie");
        assertThat(gameLogContains("sacrifices Gravebane Zombie")).isFalse();
        assertThat(gd.playersWhoSacrificedPermanentsThisTurn).doesNotContain(player2.getId());

        harness.handleListChoice(player2, "Kalitas, Traitor of Ghet");

        harness.assertNotOnBattlefield(player2, "Gravebane Zombie");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getId)
                .contains(gravebane.getCard().getId());
        assertThat(countTokens(player1, "Zombie")).isEqualTo(1);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices Gravebane Zombie"))
                .hasSize(1);
        assertThat(gd.playersWhoSacrificedPermanentsThisTurn).contains(player2.getId());
        harness.assertInGraveyard(player1, "Diabolic Edict");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({GravebaneZombie.class, ImperialEdict.class})
    @DisplayName("Imperial Edict on a lone Gravebane Zombie asks library-or-Kalitas before it is destroyed")
    void targetPlayerChoosesDestroyAsksBeforeTheLoneZombieIsDestroyed() {
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent gravebane = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        Card edict = new ImperialEdict();
        harness.setHand(player1, List.of(edict));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.stack).extracting(entry -> entry.getCard().getId()).containsExactly(edict.getId());
        harness.assertOnBattlefield(player2, "Gravebane Zombie");

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getId()).isEqualTo(gravebane.getCard().getId());
        harness.assertNotOnBattlefield(player2, "Gravebane Zombie");
        harness.assertInGraveyard(player1, "Imperial Edict");
        assertThat(gd.stack).isEmpty();
        assertThat(countTokens(player1, "Zombie")).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({GravebaneZombie.class, ImperialEdict.class, GrizzlyBears.class})
    @DisplayName("Imperial Edict asks library-or-Kalitas after the Zombie is chosen, before it is destroyed")
    void chosenDestroyAsksBeforeTheChosenZombieIsDestroyed() {
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent gravebane = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card edict = new ImperialEdict();
        harness.setHand(player1, List.of(edict));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player2, gravebane.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Gravebane Zombie");
        assertThat(gameLogContains("Gravebane Zombie is destroyed")).isFalse();

        harness.handleListChoice(player2, "Kalitas, Traitor of Ghet");

        harness.assertNotOnBattlefield(player2, "Gravebane Zombie");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getId)
                .contains(gravebane.getCard().getId());
        assertThat(countTokens(player1, "Zombie")).isEqualTo(1);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("Gravebane Zombie is destroyed"))
                .hasSize(1);
        harness.assertInGraveyard(player1, "Imperial Edict");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({GravebaneZombie.class, HighcliffFelidar.class, GrizzlyBears.class})
    @DisplayName("Highcliff Felidar asks library-or-Kalitas after the Zombie is chosen, before it is destroyed")
    void eachOpponentChoosesDestroyAsksBeforeTheChosenZombieIsDestroyed() {
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent gravebane = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new HighcliffFelidar()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        while (!gd.interaction.isAwaitingInput() && !gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.handlePermanentChosen(player1, gravebane.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Gravebane Zombie");
        assertThat(gameLogContains("Gravebane Zombie is destroyed")).isFalse();

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getId()).isEqualTo(gravebane.getCard().getId());
        harness.assertNotOnBattlefield(player2, "Gravebane Zombie");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(countTokens(player1, "Zombie")).isZero();
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("Gravebane Zombie is destroyed"))
                .hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canSacrificeARealVampireWithoutTappingKalitas() {
        Permanent kalitas = harness.addToBattlefieldAndReturn(player1, new KalitasTraitorOfGhet());
        harness.addToBattlefield(player1, new VampireEnvoy());
        kalitas.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Vampire Envoy");
        assertThat(kalitas.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(kalitas.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void canSacrificeTheZombieCreatedByItsReplacement() {
        Permanent kalitas = harness.addToBattlefieldAndReturn(player1, new KalitasTraitorOfGhet());
        Permanent drone = harness.addToBattlefieldAndReturn(player2, new SlaughterDrone());
        killDrone(drone);
        assertThat(countTokens(player1, "Zombie")).isEqualTo(1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(countTokens(player1, "Zombie")).isZero();
        harness.passBothPriorities();

        assertThat(kalitas.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void cannotSacrificeItselfOrAnOpponentsVampire() {
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        harness.addToBattlefield(player2, new VampireEnvoy());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Vampire Envoy");
        assertThat(gd.stack).isEmpty();
    }

    private void killDrone(Permanent drone) {
        harness.setHand(player1, List.of(new TarSnare()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, drone.getId());
    }

    private Permanent addCreature(Player player, String name, List<CardSubtype> subtypes, boolean token) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(token ? "" : "{2}");
        card.setToken(token);
        card.setColor(CardColor.BLACK);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(subtypes);
        return harness.addToBattlefieldAndReturn(player, card);
    }

    private long countTokens(Player player, String subtypeName) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().stream()
                        .anyMatch(subtype -> subtype.name().equalsIgnoreCase(subtypeName)))
                .count();
    }

    @Test
    @CardUsed({IsamaruHoundOfKonda.class, WheelOfSunAndMoon.class})
    @DisplayName("The legend rule asks Wheel-or-Kalitas before the legend that isn't kept leaves")
    void legendRuleAsksTheDeathReplacementBeforeTheOtherLegendLeaves() {
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        harness.addToBattlefieldAndReturn(player2, new WheelOfSunAndMoon()).setAttachedTo(player2.getId());
        Permanent kept = harness.addToBattlefieldAndReturn(player2, new IsamaruHoundOfKonda());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new IsamaruHoundOfKonda());
        harness.setLibrary(player2, List.of());

        harness.runStateBasedActions();
        harness.handlePermanentChosen(player2, kept.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("Put it on the bottom of its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(kept, other);

        harness.handleListChoice(player2, "Kalitas, Traitor of Ghet");

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(kept).doesNotContain(other);
        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getId)
                .contains(other.getCard().getId());
        assertThat(countTokens(player1, "Zombie")).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
