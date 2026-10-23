package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.Abduction;
import com.github.laxika.magicalvibes.cards.a.AllIsDust;
import com.github.laxika.magicalvibes.cards.a.ArchfiendOfDepravity;
import com.github.laxika.magicalvibes.cards.b.BarrinsSpite;
import com.github.laxika.magicalvibes.cards.b.BarterInBlood;
import com.github.laxika.magicalvibes.cards.b.BrainGorgers;
import com.github.laxika.magicalvibes.cards.b.BraidsArisenNightmare;
import com.github.laxika.magicalvibes.cards.b.BreakingPoint;
import com.github.laxika.magicalvibes.cards.c.CatchRelease;
import com.github.laxika.magicalvibes.cards.c.CombustionMan;
import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.d.DeadlyBrew;
import com.github.laxika.magicalvibes.cards.d.DescendantsFury;
import com.github.laxika.magicalvibes.cards.d.DesecrationDemon;
import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.cards.d.DevourFlesh;
import com.github.laxika.magicalvibes.cards.d.DiscipleOfBolas;
import com.github.laxika.magicalvibes.cards.d.DispenseJustice;
import com.github.laxika.magicalvibes.cards.d.DualShot;
import com.github.laxika.magicalvibes.cards.d.Duneblast;
import com.github.laxika.magicalvibes.cards.e.EldraziMonument;
import com.github.laxika.magicalvibes.cards.e.EssenceVortex;
import com.github.laxika.magicalvibes.cards.f.FadeAway;
import com.github.laxika.magicalvibes.cards.f.FandanielTelophoroiAscian;
import com.github.laxika.magicalvibes.cards.f.FallenCleric;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.FortunateFew;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeartPiercerManticore;
import com.github.laxika.magicalvibes.cards.h.HighSocietyHunter;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.i.IndulgentTormentor;
import com.github.laxika.magicalvibes.cards.i.InnocentBlood;
import com.github.laxika.magicalvibes.cards.k.KalitasTraitorOfGhet;
import com.github.laxika.magicalvibes.cards.k.KethekCrucibleGoliath;
import com.github.laxika.magicalvibes.cards.k.KillingWave;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MagnifyingGlass;
import com.github.laxika.magicalvibes.cards.m.MercyKilling;
import com.github.laxika.magicalvibes.cards.p.ParasiticImplant;
import com.github.laxika.magicalvibes.cards.p.PanickedAltisaur;
import com.github.laxika.magicalvibes.cards.p.PerilousPredicament;
import com.github.laxika.magicalvibes.cards.p.PhyrexianPurge;
import com.github.laxika.magicalvibes.cards.p.PillarTombsOfAku;
import com.github.laxika.magicalvibes.cards.r.RavenousVampire;
import com.github.laxika.magicalvibes.cards.r.ReignOfThePit;
import com.github.laxika.magicalvibes.cards.r.RishadanCutpurse;
import com.github.laxika.magicalvibes.cards.r.Retribution;
import com.github.laxika.magicalvibes.cards.r.RunningIsUseless;
import com.github.laxika.magicalvibes.cards.r.Rupture;
import com.github.laxika.magicalvibes.cards.s.SadisticShellGame;
import com.github.laxika.magicalvibes.cards.s.ShiftingShadow;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.StarAthlete;
import com.github.laxika.magicalvibes.cards.s.StickTogether;
import com.github.laxika.magicalvibes.cards.s.SyphonFlesh;
import com.github.laxika.magicalvibes.cards.t.TheDalekEmperor;
import com.github.laxika.magicalvibes.cards.t.TipTheScales;
import com.github.laxika.magicalvibes.cards.t.ToTheSlaughter;
import com.github.laxika.magicalvibes.cards.t.TragicArrogance;
import com.github.laxika.magicalvibes.cards.t.TwistedJustice;
import com.github.laxika.magicalvibes.cards.u.UnnaturalHunger;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.u.UrzasSylex;
import com.github.laxika.magicalvibes.cards.v.ValgavothTerrorEater;
import com.github.laxika.magicalvibes.cards.w.WarrenWeirding;
import com.github.laxika.magicalvibes.cards.w.Winnowing;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.cards.z.ZoyowaLavaTongue;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GravebaneZombie.class, WrathOfGod.class, Abduction.class, Counterspell.class,
        Humble.class, Unsummon.class})
class GravebaneZombieTest extends BaseCardTest {

    @Test
    @DisplayName("When Gravebane Zombie would die, it is put on top of its owner's library instead")
    void putOnTopOfLibraryInsteadOfDying() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player1, List.of(filler));
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new GravebaneZombie());

        destroyWithWrathOfGod(player2);

        // Not on battlefield, and NOT in the graveyard — replacement effect applied
        harness.assertNotOnBattlefield(player1, "Gravebane Zombie");
        harness.assertNotInGraveyard(player1, "Gravebane Zombie");

        // Placed on TOP of its owner's library (index 0), above the pre-existing card
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).containsExactly(zombie.getCard(), filler);

        // Log confirms the replacement
        assertThat(gameLogContains("Gravebane Zombie is put on top of its owner's library instead of dying."))
                .isTrue();
    }

    @Test
    @DisplayName("When controlled by another player, it is put on its owner's library")
    void putsOnOwnersLibraryWhenControlledByAnotherPlayer() {
        Card ownerFiller = new WrathOfGod();
        Card controllerFiller = new WrathOfGod();
        harness.setLibrary(player1, List.of(ownerFiller));
        harness.setLibrary(player2, List.of(controllerFiller));
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new GravebaneZombie());

        harness.setHand(player2, List.of(new Abduction()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.castEnchantment(player2, 0, zombie.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gravebane Zombie");
        harness.assertOnBattlefield(player2, "Gravebane Zombie");

        destroyWithWrathOfGod(player2);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(zombie.getCard(), ownerFiller);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(controllerFiller);
        harness.assertNotInGraveyard(player1, "Gravebane Zombie");
        harness.assertNotInGraveyard(player2, "Gravebane Zombie");
    }

    @Test
    @DisplayName("Lethal damage also puts it on top of its owner's library")
    void putsOnTopWhenStateBasedActionsWouldMakeItDie() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player1, List.of(filler));
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new GravebaneZombie());
        zombie.setMarkedDamage(2);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Gravebane Zombie");
        harness.assertNotInGraveyard(player1, "Gravebane Zombie");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(zombie.getCard(), filler);
    }

    @Test
    @DisplayName("A countered Gravebane Zombie goes to the graveyard, not the library")
    void counteredSpellGoesToGraveyard() {
        Card zombie = new GravebaneZombie();
        Card filler = new WrathOfGod();
        harness.setLibrary(player1, List.of(filler));
        harness.castFromHand(player1, zombie, "{3}{B}");
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player2, 0, zombie.getId());

        harness.assertInGraveyard(player1, "Gravebane Zombie");
        harness.assertNotOnBattlefield(player1, "Gravebane Zombie");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(filler);
    }

    @Test
    @DisplayName("Returning Gravebane Zombie to hand does not apply its death replacement")
    void returnedToHandInsteadOfLibrary() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player1, List.of(filler));
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new GravebaneZombie());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player2, 0, zombie.getId());

        harness.assertInHand(player1, "Gravebane Zombie");
        harness.assertNotOnBattlefield(player1, "Gravebane Zombie");
        harness.assertNotInGraveyard(player1, "Gravebane Zombie");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(filler);
    }

    @Test
    @DisplayName("Gravebane Zombie goes to the graveyard when destroyed after losing its abilities")
    void diesNormallyAfterLosingAbilities() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player1, List.of(filler));
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new GravebaneZombie());
        harness.setHand(player2, List.of(new Humble()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, zombie.getId());

        destroyWithWrathOfGod(player2);

        harness.assertNotOnBattlefield(player1, "Gravebane Zombie");
        harness.assertInGraveyard(player1, "Gravebane Zombie");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(filler);
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, GrislySpectacle.class})
    @DisplayName("Chosen over Kalitas, it goes on top of the library before Grisly Spectacle's mill")
    void libraryChosenBeforeTheRestOfTheDestroyingEffect() {
        Card first = new WrathOfGod();
        Card second = new WrathOfGod();
        Card third = new WrathOfGod();
        Card fourth = new WrathOfGod();
        harness.setLibrary(player2, List.of(first, second, third, fourth));
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        harness.setHand(player1, List.of(new GrislySpectacle()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, zombie.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Gravebane Zombie");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);

        harness.handleListChoice(player2, "Put it into its owner's library");

        // CR 608.2c: the Zombie is put on top first, then its controller mills three (its power).
        harness.assertNotOnBattlefield(player2, "Gravebane Zombie");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrder(zombie.getCard(), first, second);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(third, fourth);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, GrizzlyBears.class})
    @DisplayName("Under Wrath of God, its replacement is chosen before any creature is destroyed")
    void wrathAsksBeforeAnyCreatureIsDestroyed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        harness.addToBattlefield(player2, new GrizzlyBears());

        destroyWithWrathOfGod(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player1, "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Gravebane Zombie");
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        harness.handleListChoice(player2, "Put it into its owner's library");

        harness.assertNotOnBattlefield(player1, "Kalitas, Traitor of Ghet");
        harness.assertNotOnBattlefield(player2, "Gravebane Zombie");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, InnocentBlood.class})
    @DisplayName("Under Innocent Blood, its replacement is chosen before either sacrifice happens")
    void eachPlayerSacrificeAsksBeforeAnyCreatureIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        Card innocentBlood = new InnocentBlood();
        harness.setHand(player1, List.of(innocentBlood));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.stack).extracting(entry -> entry.getCard().getId()).containsExactly(innocentBlood.getId());
        harness.assertOnBattlefield(player1, "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Gravebane Zombie");

        harness.handleListChoice(player2, "Put it into its owner's library");

        harness.assertInGraveyard(player1, "Kalitas, Traitor of Ghet");
        harness.assertNotOnBattlefield(player2, "Gravebane Zombie");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        assertThat(gd.playersWhoSacrificedPermanentsThisTurn)
                .contains(player1.getId(), player2.getId());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, AllIsDust.class})
    @DisplayName("Under All Is Dust, its replacement is chosen before any permanent is sacrificed")
    void sacrificeEachMatchingAsksBeforeAnyPermanentIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        Card allIsDust = new AllIsDust();
        harness.setHand(player1, List.of(allIsDust));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.stack).extracting(entry -> entry.getCard().getId()).containsExactly(allIsDust.getId());
        harness.assertOnBattlefield(player1, "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Gravebane Zombie");

        harness.handleListChoice(player2, "Put it into its owner's library");

        harness.assertInGraveyard(player1, "Kalitas, Traitor of Ghet");
        harness.assertNotOnBattlefield(player2, "Gravebane Zombie");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, TwistedJustice.class})
    @DisplayName("Under Twisted Justice, its replacement is chosen before the sacrifice and the draw happens once")
    void sacrificeThenDrawAsksBeforeTheZombieIsSacrificed() {
        harness.setLibrary(player1, List.of(new WrathOfGod(), new WrathOfGod(), new WrathOfGod(), new WrathOfGod()));
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        Card twistedJustice = new TwistedJustice();
        harness.setHand(player1, List.of(twistedJustice));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.stack).extracting(entry -> entry.getCard().getId()).containsExactly(twistedJustice.getId());
        harness.assertOnBattlefield(player2, "Gravebane Zombie");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playersWhoSacrificedPermanentsThisTurn).doesNotContain(player2.getId());

        harness.handleListChoice(player2, "Kalitas, Traitor of Ghet");

        harness.assertNotOnBattlefield(player2, "Gravebane Zombie");
        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getId)
                .contains(zombie.getCard().getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, DevourFlesh.class})
    @DisplayName("Under Devour Flesh, its replacement is chosen before the sacrifice and the life gain happens once")
    void sacrificeThenGainLifeAsksBeforeTheZombieIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        int lifeBefore = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(new DevourFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertOnBattlefield(player2, "Gravebane Zombie");
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);

        harness.handleListChoice(player2, "Put it into its owner's library");

        harness.assertNotOnBattlefield(player2, "Gravebane Zombie");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, DevourFlesh.class, GrizzlyBears.class})
    @DisplayName("Under Devour Flesh, a chosen Zombie's replacement is chosen before the sacrifice and the life gain happens once")
    void chosenSacrificeThenGainLifeAsksBeforeTheZombieIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        harness.addToBattlefield(player2, new GrizzlyBears());
        int lifeBefore = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(new DevourFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player2, zombie.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Gravebane Zombie");
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);

        harness.handleListChoice(player2, "Put it into its owner's library");

        harness.assertNotOnBattlefield(player2, "Gravebane Zombie");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, MercyKilling.class})
    @DisplayName("Under Mercy Killing, its replacement is chosen before the sacrifice and the tokens are created once")
    void sacrificeTargetThenCreateTokensAsksBeforeTheZombieIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        harness.setHand(player1, List.of(new MercyKilling()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, zombie.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Gravebane Zombie");
        harness.assertNotOnBattlefield(player2, "Elf Warrior");

        harness.handleListChoice(player2, "Put it into its owner's library");

        harness.assertNotOnBattlefield(player2, "Gravebane Zombie");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Elf Warrior"))
                .hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, ParasiticImplant.class})
    @DisplayName("Under Parasitic Implant, its replacement is chosen before the sacrifice and the Myr is created once")
    void enchantedCreatureSacrificeAsksBeforeTheZombieIsSacrificed() {
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent zombie = addCreatureReady(player2, new GravebaneZombie());
        harness.setHand(player1, List.of(new ParasiticImplant()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castEnchantment(player1, 0, zombie.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Gravebane Zombie");
        harness.assertNotOnBattlefield(player1, "Phyrexian Myr");
        assertThat(gameLogContains("Gravebane Zombie is sacrificed")).isFalse();

        harness.handleListChoice(player2, "Kalitas, Traitor of Ghet");

        harness.assertNotOnBattlefield(player2, "Gravebane Zombie");
        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getId)
                .contains(zombie.getCard().getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Phyrexian Myr"))
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("Gravebane Zombie is sacrificed"))
                .hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, BarrinsSpite.class, GrizzlyBears.class})
    @DisplayName("Under Barrin's Spite, a chosen Zombie's replacement is chosen before the sacrifice and the other creature returns once")
    void sacrificeOneOfTwoThenReturnOtherAsksBeforeTheZombieIsSacrificed() {
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent zombie = addCreatureReady(player2, new GravebaneZombie());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarrinsSpite()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, List.of(zombie.getId(), bears.getId()));
        harness.handlePermanentChosen(player2, zombie.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Gravebane Zombie");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gameLogContains("sacrifices Gravebane Zombie")).isFalse();

        harness.handleListChoice(player2, "Kalitas, Traitor of Ghet");

        harness.assertNotOnBattlefield(player2, "Gravebane Zombie");
        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getId)
                .contains(zombie.getCard().getId());
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices Gravebane Zombie"))
                .hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, Retribution.class, GrizzlyBears.class})
    @DisplayName("Under Retribution, a chosen Zombie's replacement is chosen before the sacrifice and the -1/-1 counter is placed once")
    void sacrificeOneOfTwoThenCounterAsksBeforeTheZombieIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent zombie = addCreatureReady(player2, new GravebaneZombie());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Retribution()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, List.of(zombie.getId(), bears.getId()));
        harness.handlePermanentChosen(player2, zombie.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Gravebane Zombie");
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();

        harness.handleListChoice(player2, "Put it into its owner's library");

        harness.assertNotOnBattlefield(player2, "Gravebane Zombie");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, WarrenWeirding.class, GrizzlyBears.class})
    @DisplayName("Under Warren Weirding, a chosen Zombie's replacement is chosen before the sacrifice is logged")
    void targetPlayerSacrificeIfSubtypeAsksBeforeTheChosenZombieIsSacrificed() {
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent zombie = addCreatureReady(player2, new GravebaneZombie());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WarrenWeirding()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handlePermanentChosen(player2, zombie.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Gravebane Zombie");
        assertThat(gameLogContains("sacrifices Gravebane Zombie")).isFalse();

        harness.handleListChoice(player2, "Kalitas, Traitor of Ghet");

        harness.assertNotOnBattlefield(player2, "Gravebane Zombie");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getId)
                .contains(zombie.getCard().getId());
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices Gravebane Zombie"))
                .hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, WarrenWeirding.class})
    @DisplayName("Under Warren Weirding, a lone Zombie's replacement is chosen before it is sacrificed")
    void targetPlayerSacrificeIfSubtypeAsksBeforeTheLoneZombieIsSacrificed() {
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent zombie = addCreatureReady(player2, new GravebaneZombie());
        Card weirding = new WarrenWeirding();
        harness.setHand(player1, List.of(weirding));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.stack).extracting(entry -> entry.getCard().getId()).containsExactly(weirding.getId());
        harness.assertOnBattlefield(player2, "Gravebane Zombie");
        assertThat(gameLogContains("sacrifices Gravebane Zombie")).isFalse();

        harness.handleListChoice(player2, "Kalitas, Traitor of Ghet");

        harness.assertNotOnBattlefield(player2, "Gravebane Zombie");
        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getId)
                .contains(zombie.getCard().getId());
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices Gravebane Zombie"))
                .hasSize(1);
        harness.assertInGraveyard(player1, "Warren Weirding");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, DiscipleOfBolas.class})
    @DisplayName("Under Disciple of Bolas, its replacement is chosen before the sacrifice and the life gain and draws happen once")
    void sacrificeAnotherCreatureThenGainLifeAndDrawAsksBeforeTheZombieIsSacrificed() {
        harness.addToBattlefield(player2, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new GravebaneZombie());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new WrathOfGod(), new WrathOfGod(), new WrathOfGod(), new WrathOfGod()));
        harness.setHand(player1, List.of(new DiscipleOfBolas()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        int lifeBefore = gd.getLife(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.handlePermanentChosen(player1, zombie.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player1, "Gravebane Zombie");
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);

        harness.handleListChoice(player1, "Kalitas, Traitor of Ghet");

        harness.assertNotOnBattlefield(player1, "Gravebane Zombie");
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId)
                .contains(zombie.getCard().getId());
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices Gravebane Zombie"))
                .hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, HeartPiercerManticore.class})
    @DisplayName("Under Heart-Piercer Manticore, its replacement is chosen before the sacrifice and the reflexive trigger is asked once")
    void sacrificeAnotherCreatureThenReflexiveDamageAsksBeforeTheZombieIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player1, List.of(filler));
        harness.addToBattlefield(player2, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new GravebaneZombie());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new HeartPiercerManticore()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        int lifeBefore = gd.getLife(player2.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, zombie.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player1, "Gravebane Zombie");
        assertThat(gameLogContains("choose a target for the reflexive trigger")).isFalse();

        harness.handleListChoice(player1, "Put it into its owner's library");

        harness.assertNotOnBattlefield(player1, "Gravebane Zombie");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(zombie.getCard(), filler);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("choose a target for the reflexive trigger"))
                .hasSize(1);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, TipTheScales.class})
    @DisplayName("Under Tip the Scales, its replacement is chosen before the sacrifice and the reflexive trigger is put on the stack once")
    void sacrificePermanentThenReflexiveAsksBeforeTheZombieIsSacrificed() {
        Permanent kalitas = harness.addToBattlefieldAndReturn(player2, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new GravebaneZombie());
        harness.setHand(player1, List.of(new TipTheScales()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.handlePermanentChosen(player1, zombie.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player1, "Gravebane Zombie");
        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);

        harness.handleListChoice(player1, "Kalitas, Traitor of Ghet");

        harness.assertNotOnBattlefield(player1, "Gravebane Zombie");
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId)
                .contains(zombie.getCard().getId());
        assertThat(gd.stack).filteredOn(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .hasSize(1);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices Gravebane Zombie"))
                .hasSize(1);

        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, kalitas)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, Rupture.class})
    @DisplayName("Under Rupture, its replacement is chosen before the sacrifice and the damage is dealt once")
    void sacrificeThenMassDamageAsksBeforeTheZombieIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player1, List.of(filler));
        harness.addToBattlefield(player2, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new GravebaneZombie());
        harness.setHand(player1, List.of(new Rupture()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
        int player1Life = gd.getLife(player1.getId());
        int player2Life = gd.getLife(player2.getId());

        harness.handlePermanentChosen(player1, zombie.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player1, "Gravebane Zombie");
        harness.assertLife(player1, player1Life);
        harness.assertLife(player2, player2Life);

        harness.handleListChoice(player1, "Put it into its owner's library");

        harness.assertNotOnBattlefield(player1, "Gravebane Zombie");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(zombie.getCard(), filler);
        harness.assertLife(player1, player1Life - 3);
        harness.assertLife(player2, player2Life - 3);
        harness.assertOnBattlefield(player2, "Kalitas, Traitor of Ghet");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, KethekCrucibleGoliath.class, LlanowarElves.class})
    @DisplayName("Under Kethek, Crucible Goliath, its replacement is chosen before the sacrifice and the reveal happens once")
    void sacrificeOtherCreatureThenRevealAsksBeforeTheZombieIsSacrificed() {
        Card elves = new LlanowarElves();
        harness.setLibrary(player1, List.of(elves));
        harness.addToBattlefield(player2, new KalitasTraitorOfGhet());
        harness.addToBattlefield(player1, new KethekCrucibleGoliath());
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new GravebaneZombie());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.handlePermanentChosen(player1, zombie.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player1, "Gravebane Zombie");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(elves);

        harness.handleListChoice(player1, "Kalitas, Traitor of Ghet");

        harness.assertNotOnBattlefield(player1, "Gravebane Zombie");
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId)
                .contains(zombie.getCard().getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(permanent -> permanent.getCard() == elves)
                .hasSize(1);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices Gravebane Zombie"))
                .hasSize(1);
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, DescendantsFury.class, Shock.class})
    @DisplayName("Under Descendants' Fury, its replacement is chosen before the sacrifice and the reveal happens once")
    void sacrificeCombatDamageDealerThenRevealAsksBeforeTheZombieIsSacrificed() {
        Card shock = new Shock();
        Card libraryZombie = new GravebaneZombie();
        harness.setLibrary(player1, List.of(shock, libraryZombie));
        harness.addToBattlefield(player2, new KalitasTraitorOfGhet());
        harness.addToBattlefield(player1, new DescendantsFury());
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new GravebaneZombie());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        zombie.setSummoningSick(false);
        bears.setSummoningSick(false);
        zombie.setAttacking(true);
        bears.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.passUntil(TurnStep.COMBAT_DAMAGE);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.handlePermanentChosen(player1, zombie.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player1, "Gravebane Zombie");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock, libraryZombie);

        harness.handleListChoice(player1, "Kalitas, Traitor of Ghet");

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(zombie);
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId)
                .contains(zombie.getCard().getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() == libraryZombie).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices Gravebane Zombie"))
                .hasSize(1);
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, DesecrationDemon.class})
    @DisplayName("Under Desecration Demon, an opponent's chosen Zombie's replacement is chosen before the sacrifice and the Demon grows once")
    void anyOpponentSacrificeAsksBeforeTheChosenZombieIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new DesecrationDemon());
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.handlePermanentChosen(player2, zombie.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Gravebane Zombie");
        assertThat(demon.isTapped()).isFalse();
        assertThat(demon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.handleListChoice(player2, "Put it into its owner's library");

        harness.assertNotOnBattlefield(player2, "Gravebane Zombie");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(demon.isTapped()).isTrue();
        assertThat(demon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices Gravebane Zombie"))
                .hasSize(1);
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, RavenousVampire.class})
    @DisplayName("Under Ravenous Vampire, its replacement is chosen before the sacrifice and the counter is placed once")
    void maySacrificeForCounterAsksBeforeTheZombieIsSacrificed() {
        harness.addToBattlefield(player2, new KalitasTraitorOfGhet());
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new RavenousVampire());
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new GravebaneZombie());
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.handlePermanentChosen(player1, zombie.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player1, "Gravebane Zombie");
        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.handleListChoice(player1, "Kalitas, Traitor of Ghet");

        harness.assertNotOnBattlefield(player1, "Gravebane Zombie");
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId)
                .contains(zombie.getCard().getId());
        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(vampire.isTapped()).isFalse();
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices Gravebane Zombie"))
                .hasSize(1);
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, BrainGorgers.class})
    @DisplayName("Under Brain Gorgers, a chosen Zombie's replacement is chosen before the sacrifice and the spell is countered once")
    void anyPlayerSacrificeToCounterAsksBeforeTheChosenZombieIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new BrainGorgers(), "{3}{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        harness.handlePermanentChosen(player2, zombie.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Gravebane Zombie");
        harness.assertNotInGraveyard(player1, "Brain Gorgers");

        harness.handleListChoice(player2, "Put it into its owner's library");

        harness.assertNotOnBattlefield(player2, "Gravebane Zombie");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Brain Gorgers");
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices Gravebane Zombie"))
                .hasSize(1);
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, PillarTombsOfAku.class})
    @DisplayName("Under Pillar Tombs of Aku, a chosen Zombie's replacement is chosen before the sacrifice and no life is lost")
    void forcedCostSacrificeAsksBeforeTheChosenZombieIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player1, List.of(filler));
        harness.addToBattlefield(player2, new KalitasTraitorOfGhet());
        harness.addToBattlefield(player1, new PillarTombsOfAku());
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new GravebaneZombie());
        harness.addToBattlefield(player1, new GrizzlyBears());
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.handlePermanentChosen(player1, zombie.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player1, "Gravebane Zombie");
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices Gravebane Zombie"))
                .isEmpty();

        harness.handleListChoice(player1, "Put it into its owner's library");

        harness.assertNotOnBattlefield(player1, "Gravebane Zombie");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(zombie.getCard(), filler);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Pillar Tombs of Aku");
        harness.assertLife(player1, 20);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices Gravebane Zombie"))
                .hasSize(1);
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, IndulgentTormentor.class})
    @DisplayName("Under Indulgent Tormentor, a chosen Zombie's replacement is chosen before the sacrifice and no card is drawn")
    void tormentSacrificeAsksBeforeTheChosenZombieIsSacrificed() {
        Card filler = new WrathOfGod();
        Card tormentorDraw = new WrathOfGod();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(tormentorDraw));
        harness.setLibrary(player2, List.of(filler));
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        harness.addToBattlefield(player1, new IndulgentTormentor());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        harness.addToBattlefield(player2, new GrizzlyBears());
        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, ChoiceContext.IndulgentTormentorChoice.SACRIFICE);

        harness.handlePermanentChosen(player2, zombie.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Gravebane Zombie");
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices Gravebane Zombie"))
                .isEmpty();

        harness.handleListChoice(player2, "Put it into its owner's library");

        harness.assertNotOnBattlefield(player2, "Gravebane Zombie");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(tormentorDraw);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices Gravebane Zombie"))
                .hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, EldraziMonument.class})
    @DisplayName("Under Eldrazi Monument, a lone Zombie's replacement is chosen before it is sacrificed and the Monument stays")
    void forcedCostLoneCandidateSacrificeAsksBeforeTheZombieIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player1, List.of(filler));
        harness.addToBattlefield(player2, new KalitasTraitorOfGhet());
        harness.addToBattlefield(player1, new EldraziMonument());
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new GravebaneZombie());
        advanceToUpkeep(player1);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player1, "Gravebane Zombie");
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices Gravebane Zombie"))
                .isEmpty();

        harness.handleListChoice(player1, "Put it into its owner's library");

        harness.assertNotOnBattlefield(player1, "Gravebane Zombie");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(zombie.getCard(), filler);
        harness.assertOnBattlefield(player1, "Eldrazi Monument");
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices Gravebane Zombie"))
                .hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, DesecrationDemon.class})
    @DisplayName("Under Desecration Demon, an opponent's lone Zombie's replacement is chosen before the \"yes\" sacrifices it")
    void anyOpponentLoneCandidateSacrificeAsksBeforeTheZombieIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new DesecrationDemon());
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Gravebane Zombie");
        assertThat(demon.isTapped()).isFalse();
        assertThat(demon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.handleListChoice(player2, "Put it into its owner's library");

        harness.assertNotOnBattlefield(player2, "Gravebane Zombie");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        assertThat(demon.isTapped()).isTrue();
        assertThat(demon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices Gravebane Zombie"))
                .hasSize(1);
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, HighSocietyHunter.class})
    @DisplayName("Under High-Society Hunter, a lone other Zombie's replacement is chosen before the \"yes\" sacrifices it")
    void maySacrificeForCounterLoneCandidateAsksBeforeTheZombieIsSacrificed() {
        harness.addToBattlefield(player2, new KalitasTraitorOfGhet());
        Permanent hunter = addCreatureReady(player1, new HighSocietyHunter());
        Permanent zombie = addCreatureReady(player1, new GravebaneZombie());
        declareAttackers(List.of(0));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player1, "Gravebane Zombie");
        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.handleListChoice(player1, "Kalitas, Traitor of Ghet");

        harness.assertNotOnBattlefield(player1, "Gravebane Zombie");
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId)
                .contains(zombie.getCard().getId());
        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices Gravebane Zombie"))
                .hasSize(1);
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, BrainGorgers.class})
    @DisplayName("Under Brain Gorgers, a lone Zombie's replacement is chosen before the \"yes\" sacrifices it and the spell is countered once")
    void anyPlayerLoneCandidateSacrificeToCounterAsksBeforeTheZombieIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        harness.castFromHand(player1, new BrainGorgers(), "{3}{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Gravebane Zombie");
        harness.assertNotInGraveyard(player1, "Brain Gorgers");

        harness.handleListChoice(player2, "Put it into its owner's library");

        harness.assertNotOnBattlefield(player2, "Gravebane Zombie");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        harness.assertInGraveyard(player1, "Brain Gorgers");
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices Gravebane Zombie"))
                .hasSize(1);
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, BrainGorgers.class})
    @DisplayName("Under Brain Gorgers, the last player's decline asks the picked Zombie's replacement before the sacrifice and counter")
    void anyPlayerLastDeclineAsksBeforeThePickedZombieIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player1, List.of(filler));
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new GravebaneZombie());
        harness.addToBattlefield(player2, new KalitasTraitorOfGhet());
        harness.castFromHand(player1, new BrainGorgers(), "{3}{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player1, "Gravebane Zombie");
        harness.assertNotInGraveyard(player1, "Brain Gorgers");

        harness.handleListChoice(player1, "Put it into its owner's library");

        harness.assertNotOnBattlefield(player1, "Gravebane Zombie");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(zombie.getCard(), filler);
        harness.assertInGraveyard(player1, "Brain Gorgers");
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices Gravebane Zombie"))
                .hasSize(1);
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, BarterInBlood.class, GrizzlyBears.class})
    @DisplayName("Under Barter in Blood, the chosen Zombie's replacement is asked before any of the chosen creatures is sacrificed")
    void forcedMultiSacrificeAsksBeforeAnyChosenCreatureIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent ownBears1 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownBears2 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarterInBlood()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultiplePermanentsChosen(player1, List.of(ownBears1.getId(), ownBears2.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(zombie.getId(), bears.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(3);

        harness.handleListChoice(player2, "Put it into its owner's library");

        harness.assertNotOnBattlefield(player2, "Gravebane Zombie");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .doesNotContain(bears.getId())
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Kalitas, Traitor of Ghet", "Zombie");
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices Gravebane Zombie"))
                .hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Syphon Flesh asks for the chosen Zombie's death replacement before anything is sacrificed or created")
    void eachOpponentSacrificeThenTokensAsksBeforeTheChosenZombieIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SyphonFlesh()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultiplePermanentsChosen(player2, List.of(zombie.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .containsExactly(bears.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Kalitas, Traitor of Ghet", "Zombie");
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices Gravebane Zombie"))
                .hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Reign of the Pit asks for the chosen Zombie's death replacement before any creature is sacrificed")
    void eachPlayerSacrificeThenTotalPowerTokenAsksBeforeAnyChosenCreatureIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ReignOfThePit()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultiplePermanentsChosen(player1, List.of(ownBears.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(zombie.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Kalitas, Traitor of Ghet", "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices")).isEmpty();

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Kalitas, Traitor of Ghet", "Demon");
        Permanent demon = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Demon"))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, demon)).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, ToTheSlaughter.class, GrizzlyBears.class, DevilthornFox.class,
            Forest.class, DualShot.class, MagnifyingGlass.class})
    @DisplayName("To the Slaughter with delirium asks for the chosen Zombie's death replacement before it is sacrificed")
    void targetPlayerSacrificesCreatureAndPlaneswalkerAsksBeforeTheChosenZombieIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        harness.setGraveyard(player1, List.of(
                new DevilthornFox(), new Forest(), new DualShot(), new MagnifyingGlass()));
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ToTheSlaughter()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(zombie.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices")).isEmpty();

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .containsExactly(bears.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Kalitas, Traitor of Ghet");
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices Gravebane Zombie"))
                .hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, PerilousPredicament.class, GrizzlyBears.class})
    @DisplayName("Perilous Predicament asks for the chosen Zombie's death replacement before it is sacrificed")
    void eachOpponentSacrificesArtifactAndNonartifactCreatureAsksBeforeTheChosenZombieIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new PerilousPredicament(), "{4}{B}");
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player2, List.of(zombie.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices")).isEmpty();

        harness.handleListChoice(player2, "Kalitas, Traitor of Ghet");

        harness.assertNotOnBattlefield(player2, "Gravebane Zombie");
        assertThat(gd.findExiledCard(zombie.getCard().getId())).isNotNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(filler);
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .containsExactly(bears.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Kalitas, Traitor of Ghet", "Zombie");
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices Gravebane Zombie"))
                .hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, DispenseJustice.class, GrizzlyBears.class})
    @DisplayName("Dispense Justice asks for the chosen attacking Zombie's death replacement before it is sacrificed")
    void sacrificeAttackingCreatureAsksBeforeTheChosenZombieIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        zombie.setSummoningSick(false);
        zombie.setAttacking(true);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DispenseJustice()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.passPriority(player2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(zombie.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices")).isEmpty();

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .containsExactly(bears.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Kalitas, Traitor of Ghet");
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices Gravebane Zombie"))
                .hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, ArchfiendOfDepravity.class, GrizzlyBears.class})
    @DisplayName("Archfiend of Depravity asks for the unkept Zombie's death replacement before the rest is sacrificed")
    void keepUpToThenSacrificeRestAsksBeforeTheUnkeptZombieIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        harness.addToBattlefield(player1, new ArchfiendOfDepravity());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent otherBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent thirdBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player2, List.of(bears.getId(), otherBears.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(4);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices")).isEmpty();

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .containsExactlyInAnyOrder(bears.getId(), otherBears.getId());
        assertThat(gd.findExiledCard(thirdBears.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Kalitas, Traitor of Ghet", "Archfiend of Depravity", "Zombie");
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices Gravebane Zombie"))
                .hasSize(1);
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, CatchRelease.class, GrizzlyBears.class})
    @DisplayName("Release asks for the chosen Zombie's death replacement before any permanent is sacrificed")
    void sacrificeOneOfEachTypeAsksBeforeAnyChosenCreatureIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CatchRelease()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(ownBears.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(zombie.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices")).isEmpty();

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .containsExactly(bears.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Kalitas, Traitor of Ghet");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices Gravebane Zombie"))
                .hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, DeadlyBrew.class, GrizzlyBears.class, Forest.class})
    @DisplayName("Deadly Brew asks for the chosen Zombie's death replacement before anything is sacrificed or returned")
    void eachPlayerSacrificeThenMayReturnAsksBeforeAnyChosenCreatureIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DeadlyBrew()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultiplePermanentsChosen(player1, List.of(ownBears.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(zombie.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices")).isEmpty();

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Kalitas, Traitor of Ghet");
        PendingInteraction.GraveyardChoice returnChoice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(returnChoice.playerId()).isEqualTo(player1.getId());
        assertThat(returnChoice.validIndices()).hasSize(1);
        harness.handleGraveyardCardChosen(player1, returnChoice.validIndices().getFirst());

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, Winnowing.class, GrizzlyBears.class, LlanowarElves.class})
    @DisplayName("Winnowing asks for the Zombie's death replacement before any nonsharing creature is sacrificed")
    void winnowingAsksBeforeAnyNonsharingCreatureIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.castFromHand(player1, new Winnowing(), "{4}{W}{W}");
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(3);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices")).isEmpty();

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .containsExactly(bears.getId());
        assertThat(gd.findExiledCard(elves.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Kalitas, Traitor of Ghet", "Zombie");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, TragicArrogance.class, GrizzlyBears.class})
    @DisplayName("Tragic Arrogance asks for the unkept Zombie's death replacement before any permanent is sacrificed")
    void keepOneOfEachTypeAsksBeforeTheUnkeptZombieIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new TragicArrogance(), "{3}{W}{W}");
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("keeps Grizzly Bears")).isEmpty();
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices")).isEmpty();

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .containsExactly(bears.getId());
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("keeps Grizzly Bears")).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Kalitas, Traitor of Ghet");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, StickTogether.class, FallenCleric.class, GrizzlyBears.class})
    @DisplayName("Stick Together asks for the unchosen Zombie's death replacement before any creature is sacrificed")
    void stickTogetherAsksBeforeAnyCreatureOutsideThePartyIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        Permanent kalitas = harness.addToBattlefieldAndReturn(player1, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        Permanent cleric = harness.addToBattlefieldAndReturn(player2, new FallenCleric());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new StickTogether(), "{3}{W}{W}");
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(kalitas.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(cleric.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(3);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices")).isEmpty();

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .containsExactly(cleric.getId());
        assertThat(gd.findExiledCard(bears.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Kalitas, Traitor of Ghet", "Zombie");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, Duneblast.class, GrizzlyBears.class})
    @DisplayName("Duneblast asks for the Zombie's death replacement before any unkept creature is destroyed")
    void destroyRestAsksBeforeAnyUnkeptCreatureIsDestroyed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        Permanent kalitas = harness.addToBattlefieldAndReturn(player1, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Duneblast(), "{4}{W}{B}{G}");
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(kalitas.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .containsExactly(zombie.getId(), bears.getId());

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.findExiledCard(bears.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Kalitas, Traitor of Ghet", "Zombie");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, UrzasSylex.class, GrizzlyBears.class, Forest.class})
    @DisplayName("Urza's Sylex asks for the Zombie's death replacement before any other permanent is destroyed")
    void chooseLandsThenDestroyRestAsksBeforeAnyOtherPermanentIsDestroyed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        harness.addToBattlefield(player1, new UrzasSylex());
        Permanent kalitas = harness.addToBattlefieldAndReturn(player1, new KalitasTraitorOfGhet());
        List<Permanent> forests = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            forests.add(harness.addToBattlefieldAndReturn(player2, new Forest()));
        }
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        List<UUID> keptForests = forests.subList(0, 6).stream().map(Permanent::getId).toList();
        harness.handleMultiplePermanentsChosen(player2, keptForests);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kalitas);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(9).contains(zombie, bears);

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .containsExactlyElementsOf(keptForests);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(kalitas);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, SadisticShellGame.class, GrizzlyBears.class})
    @DisplayName("Sadistic Shell Game asks for the Zombie's death replacement before any chosen creature is destroyed")
    void eachPlayerChoosesOpponentPermanentToDestroyAsksBeforeAnyChosenCreatureIsDestroyed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new SadisticShellGame(), "{4}{B}");
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player2, List.of(bears.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(zombie.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .containsExactly(zombie.getId(), bears.getId());

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.findExiledCard(bears.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Kalitas, Traitor of Ghet", "Zombie");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, FortunateFew.class, GrizzlyBears.class})
    @DisplayName("Fortunate Few asks for the Zombie's death replacement before any unchosen permanent is destroyed")
    void chooseDifferentOpponentPermanentThenDestroyRestAsksBeforeAnyUnchosenPermanentIsDestroyed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        Permanent kalitas = harness.addToBattlefieldAndReturn(player1, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new FortunateFew(), "{3}{W}{W}");
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(kalitas.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .containsExactly(zombie.getId(), bears.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownBears);

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(bears);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(kalitas);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownBears.getCard());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, RunningIsUseless.class, GrizzlyBears.class})
    @DisplayName("Running Is Useless asks for the Zombie's death replacement before any chosen creature is destroyed")
    void chooseCreaturesWithDifferentManaValuesAsksBeforeAnyChosenCreatureIsDestroyed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card scheme = new RunningIsUseless();
        gd.stack.add(new StackEntry(StackEntryType.TRIGGERED_ABILITY, scheme, player1.getId(),
                scheme.getName(), scheme.getEffects(EffectSlot.SPELL)));
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(zombie.getId(), bears.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .containsExactly(zombie.getId(), bears.getId());

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.findExiledCard(bears.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Kalitas, Traitor of Ghet", "Zombie");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void destroyWithWrathOfGod(Player caster) {
        harness.forceActivePlayer(caster);
        harness.castFromHand(caster, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, KillingWave.class, GrizzlyBears.class})
    @DisplayName("Killing Wave asks for the unkept Zombie's death replacement before any life is paid or creature sacrificed")
    void killingWaveAsksBeforeAnyLifeIsPaidOrCreatureSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        Permanent kalitas = harness.addToBattlefieldAndReturn(player1, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new KillingWave()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(kalitas.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(bears.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .containsExactly(zombie.getId(), bears.getId());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("will pay 2 life")).hasSize(1);

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .containsExactly(bears.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .containsExactly(kalitas.getId());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("will pay 2 life")).hasSize(2);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("pays 2 life (")).hasSize(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, FadeAway.class, GrizzlyBears.class})
    @DisplayName("Fade Away asks for the chosen Zombie's death replacement before any mana is paid or permanent sacrificed")
    void fadeAwayAsksBeforeAnyManaIsPaidOrPermanentSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        Permanent kalitas = harness.addToBattlefieldAndReturn(player1, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new FadeAway(), "{2}{U}");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of(kalitas.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(bears.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(zombie.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .containsExactly(zombie.getId(), bears.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("pays {1} for Fade Away")).isEmpty();

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .containsExactly(bears.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(Permanent::getId)
                .containsExactly(kalitas.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("pays {1} for Fade Away")).hasSize(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, PhyrexianPurge.class, GrizzlyBears.class})
    @DisplayName("Phyrexian Purge asks for the Zombie's death replacement before any target is destroyed")
    void destroyEachTargetAsksBeforeAnyTargetIsDestroyed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        Permanent kalitas = harness.addToBattlefieldAndReturn(player1, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card purge = new PhyrexianPurge();
        harness.setHand(player1, List.of(purge));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);
        harness.castSorcery(player1, 0, List.of(zombie.getId(), bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .containsExactly(zombie.getId(), bears.getId());
        assertThat(gd.stack).extracting(entry -> entry.getCard().getId()).containsExactly(purge.getId());

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.findExiledCard(bears.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Kalitas, Traitor of Ghet", "Zombie");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kalitas);
        harness.assertInGraveyard(player1, "Phyrexian Purge");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, EssenceVortex.class})
    @DisplayName("Essence Vortex asks for the Zombie's death replacement before destroying it when its controller can't pay")
    void destroyUnlessPaysToughnessLifeAsksBeforeTheZombieIsDestroyed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        harness.setLife(player2, 1);
        Card vortex = new EssenceVortex();
        harness.setHand(player1, List.of(vortex));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, zombie.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.stack).extracting(entry -> entry.getCard().getId()).containsExactly(vortex.getId());
        // The spell is stopped and resolved again with the answer, rather than carrying on without the Zombie.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).context())
                .isInstanceOfSatisfying(ChoiceContext.DeathExileReplacementChoice.class,
                        choice -> assertThat(choice.rerunsEffect()).isTrue());
        harness.assertOnBattlefield(player2, "Gravebane Zombie");

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        harness.assertNotOnBattlefield(player2, "Gravebane Zombie");
        harness.assertInGraveyard(player1, "Essence Vortex");
        harness.assertLife(player2, 1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, EssenceVortex.class})
    @DisplayName("Declining Essence Vortex's payment asks for the Zombie's death replacement before it is destroyed")
    void declinedToughnessLifePaymentAsksBeforeTheZombieIsDestroyed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new EssenceVortex()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, zombie.getId());

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Gravebane Zombie");
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("Gravebane Zombie is destroyed"))
                .isEmpty();

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        harness.assertNotOnBattlefield(player2, "Gravebane Zombie");
        harness.assertLife(player2, 20);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("Gravebane Zombie is destroyed"))
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(permanent -> permanent.getCard().getName())
                .doesNotContain("Zombie");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, ShiftingShadow.class})
    @DisplayName("Shifting Shadow asks for the Zombie's death replacement before anything is revealed")
    void destroyEnchantedThenRevealAsksBeforeTheZombieIsDestroyed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ShiftingShadow());
        aura.setAttachedTo(zombie.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(zombie);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(filler);
        assertThat(aura.getAttachedTo()).isEqualTo(zombie.getId());

        harness.handleListChoice(player2, "Put it into its owner's library");

        // CR 608.2c: the Zombie goes on top first, so the reveal finds it and puts it back onto the battlefield.
        Permanent returned = findPermanent(player2, "Gravebane Zombie");
        assertThat(returned.getId()).isNotEqualTo(zombie.getId());
        assertThat(aura.getAttachedTo()).isEqualTo(returned.getId());
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(filler);
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(permanent -> permanent.getCard().getName())
                .doesNotContain("Zombie");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, CombustionMan.class})
    @DisplayName("Declining Combustion Man's damage asks for the Zombie's death replacement before it is destroyed")
    void declinedTakeDamageAsksBeforeTheZombieIsDestroyed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent combustionMan = addCreatureReady(player1, new CombustionMan());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(combustionMan);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(attackerIndex));
            harness.handlePermanentChosen(player1, zombie.getId());
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player2, false);

            assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                    .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
            harness.assertOnBattlefield(player2, "Gravebane Zombie");
            assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("Gravebane Zombie is destroyed"))
                    .isEmpty();

            harness.handleListChoice(player2, "Put it into its owner's library");
        });

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        harness.assertNotOnBattlefield(player2, "Gravebane Zombie");
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("Gravebane Zombie is destroyed"))
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(permanent -> permanent.getCard().getName())
                .doesNotContain("Zombie");
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, BreakingPoint.class})
    @DisplayName("Breaking Point's last decline asks for the Zombie's death replacement before any creature is destroyed")
    void breakingPointLastDeclineAsksBeforeAnyCreatureIsDestroyed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        Permanent kalitas = harness.addToBattlefieldAndReturn(player1, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        harness.castFromHand(player1, new BreakingPoint(), "{1}{R}{R}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(kalitas);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(zombie);

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Kalitas, Traitor of Ghet");
        harness.assertInGraveyard(player1, "Breaking Point");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, GiantAlbatross.class})
    @DisplayName("Declining Giant Albatross's life payment asks for the Zombie's death replacement before it is destroyed")
    void declinedAlbatrossLifePaymentAsksBeforeTheZombieIsDestroyed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent albatross = addCreatureReady(player1, new GiantAlbatross());
        albatross.setBlocking(true);
        albatross.addBlockingTarget(0);
        Card zombieCard = new GravebaneZombie();
        zombieCard.setPower(5);
        zombieCard.setToughness(5);
        Permanent zombie = addCreatureReady(player2, zombieCard);
        zombie.setAttacking(true);
        resolveCombat(player2);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Gravebane Zombie");
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("Gravebane Zombie is destroyed"))
                .isEmpty();

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        harness.assertNotOnBattlefield(player2, "Gravebane Zombie");
        harness.assertLife(player2, 20);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("Gravebane Zombie is destroyed"))
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(permanent -> permanent.getCard().getName())
                .doesNotContain("Zombie");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, RishadanCutpurse.class})
    @DisplayName("Declining to pay for Rishadan Cutpurse asks for the lone Zombie's death replacement before it is sacrificed")
    void unpaidSacrificeUnlessPaysAsksBeforeTheZombieIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        harness.castFromHand(player1, new RishadanCutpurse(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);

        assertMayAnswerAskedWhileTheZombieStays();

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertZombieSacrificedOntoLibrary(zombie, filler, false);
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, FandanielTelophoroiAscian.class, Shock.class})
    @DisplayName("Accepting Fandaniel's sacrifice asks for the lone Zombie's death replacement before it is sacrificed")
    void acceptedSacrificeOrLoseLifeAsksBeforeTheZombieIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        harness.addToBattlefield(player1, new FandanielTelophoroiAscian());
        harness.setGraveyard(player1, List.of(new Shock(), new Shock()));
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        resolveEndStepTriggers();

        harness.handleMayAbilityChosen(player2, true);

        assertMayAnswerAskedWhileTheZombieStays();

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertZombieSacrificedOntoLibrary(zombie, filler, true);
        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, ZoyowaLavaTongue.class, PanickedAltisaur.class})
    @DisplayName("Accepting Zoyowa's sacrifice asks for the lone Zombie's death replacement before it is sacrificed")
    void acceptedDiscardOrSacrificeAsksBeforeTheZombieIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        harness.addToBattlefield(player1, new ZoyowaLavaTongue());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new PanickedAltisaur());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, fodder));
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        resolveEndStepTriggers();

        harness.handleMayAbilityChosen(player2, true);

        assertMayAnswerAskedWhileTheZombieStays();

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertZombieSacrificedOntoLibrary(zombie, filler, true);
        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, BraidsArisenNightmare.class})
    @DisplayName("Accepting Braids's matching sacrifice asks for the lone Zombie's death replacement before it is sacrificed")
    void acceptedMatchingSacrificeOrLoseLifeAsksBeforeTheZombieIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        harness.setLibrary(player1, List.of(new WrathOfGod()));
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        harness.addToBattlefield(player1, new BraidsArisenNightmare());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        resolveEndStepTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());

        harness.handleMayAbilityChosen(player2, true);

        assertMayAnswerAskedWhileTheZombieStays();

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertZombieSacrificedOntoLibrary(zombie, filler, true);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, TheDalekEmperor.class})
    @DisplayName("Accepting The Dalek Emperor's sacrifice asks for the lone Zombie's death replacement before it is sacrificed")
    void acceptedSacrificeOrTokenAsksBeforeTheLoneZombieIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        harness.addToBattlefield(player1, new TheDalekEmperor());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        resolveBeginningOfCombatTriggers();

        harness.handleMayAbilityChosen(player2, true);

        assertMayAnswerAskedWhileTheZombieStays();

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertZombieSacrificedOntoLibrary(zombie, filler, false);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, TheDalekEmperor.class})
    @DisplayName("Choosing the Zombie for The Dalek Emperor's sacrifice asks for its death replacement before it is sacrificed")
    void chosenSacrificeOrTokenAsksBeforeTheChosenZombieIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        harness.addToBattlefield(player1, new TheDalekEmperor());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        resolveBeginningOfCombatTriggers();
        harness.handleMayAbilityChosen(player2, true);

        harness.handlePermanentChosen(player2, zombie.getId());

        assertMayAnswerAskedWhileTheZombieStays();

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertZombieSacrificedOntoLibrary(zombie, filler, false);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(bears);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, StarAthlete.class})
    @DisplayName("Accepting Star Athlete's sacrifice asks for the targeted Zombie's death replacement before it is sacrificed")
    void acceptedSacrificeOrDamageAsksBeforeTheTargetedZombieIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        addCreatureReady(player1, new StarAthlete());
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent zombie = addCreatureReady(player2, new GravebaneZombie());
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, zombie.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);

        assertMayAnswerAskedWhileTheZombieStays();

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertZombieSacrificedOntoLibrary(zombie, filler, false);
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, UnnaturalHunger.class})
    @DisplayName("Accepting Unnatural Hunger's sacrifice asks for the lone other Zombie's death replacement before it is sacrificed")
    void acceptedSacrificeOtherOrDamageAsksBeforeTheZombieIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent zombie = addCreatureReady(player2, new GravebaneZombie());
        harness.setHand(player1, List.of(new UnnaturalHunger()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);

        assertMayAnswerAskedWhileTheZombieStays();

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertZombieSacrificedOntoLibrary(zombie, filler, false);
        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(bears);
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, ValgavothTerrorEater.class, FountainOfYouth.class, Shock.class})
    @DisplayName("Paying Valgavoth's ward asks for the chosen Zombie's death replacement before any permanent is sacrificed")
    void wardMultiSacrificeAsksBeforeAnyChosenPermanentIsSacrificed() {
        Card filler = new WrathOfGod();
        harness.setLibrary(player2, List.of(filler));
        Permanent valgavoth = harness.addToBattlefieldAndReturn(player1, new ValgavothTerrorEater());
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        Permanent zombie = harness.addToBattlefieldAndReturn(player2, new GravebaneZombie());
        Permanent fountain1 = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent fountain2 = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, valgavoth.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMultiplePermanentsChosen(player2, List.of(zombie.getId(), fountain1.getId(), fountain2.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(zombie, fountain1, fountain2);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("Bob sacrifices")).isEmpty();

        harness.handleListChoice(player2, "Put it into its owner's library");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(zombie, fountain1, fountain2);
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(permanent -> permanent.getCard().getName())
                .doesNotContain("Zombie");
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices Gravebane Zombie"))
                .hasSize(1);
        assertThat(gd.stack).extracting(entry -> entry.getCard().getName()).containsExactly("Shock");
    }

    private void resolveBeginningOfCombatTriggers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
    }

    private void resolveEndStepTriggers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
    }

    private void assertMayAnswerAskedWhileTheZombieStays() {
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactly("Put it into its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Gravebane Zombie");
        harness.assertLife(player2, 20);
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices Gravebane Zombie"))
                .isEmpty();
    }

    /**
     * The Zombie went on top of its owner's library; after an end step trigger the game moves on to
     * its owner's turn, whose draw takes it back off the top.
     */
    private void assertZombieSacrificedOntoLibrary(Permanent zombie, Card filler, boolean drawnSince) {
        harness.assertNotOnBattlefield(player2, "Gravebane Zombie");
        harness.assertNotInGraveyard(player2, "Gravebane Zombie");
        if (drawnSince) {
            assertThat(gd.playerDecks.get(player2.getId())).containsExactly(filler);
            assertThat(gd.playerHands.get(player2.getId()).getLast()).isSameAs(zombie.getCard());
        } else {
            assertThat(gd.playerDecks.get(player2.getId())).containsExactly(zombie.getCard(), filler);
        }
        assertThat(gd.gameLog).filteredOn(entry -> entry.plainText().contains("sacrifices Gravebane Zombie"))
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(permanent -> permanent.getCard().getName())
                .doesNotContain("Zombie");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
