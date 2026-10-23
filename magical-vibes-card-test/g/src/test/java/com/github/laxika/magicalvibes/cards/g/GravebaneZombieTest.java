package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.Abduction;
import com.github.laxika.magicalvibes.cards.a.AllIsDust;
import com.github.laxika.magicalvibes.cards.b.BarrinsSpite;
import com.github.laxika.magicalvibes.cards.b.BarterInBlood;
import com.github.laxika.magicalvibes.cards.b.BrainGorgers;
import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.d.DevourFlesh;
import com.github.laxika.magicalvibes.cards.d.DescendantsFury;
import com.github.laxika.magicalvibes.cards.d.DesecrationDemon;
import com.github.laxika.magicalvibes.cards.d.DiscipleOfBolas;
import com.github.laxika.magicalvibes.cards.e.EldraziMonument;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeartPiercerManticore;
import com.github.laxika.magicalvibes.cards.h.HighSocietyHunter;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.i.IndulgentTormentor;
import com.github.laxika.magicalvibes.cards.i.InnocentBlood;
import com.github.laxika.magicalvibes.cards.k.KalitasTraitorOfGhet;
import com.github.laxika.magicalvibes.cards.k.KethekCrucibleGoliath;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MercyKilling;
import com.github.laxika.magicalvibes.cards.p.ParasiticImplant;
import com.github.laxika.magicalvibes.cards.p.PillarTombsOfAku;
import com.github.laxika.magicalvibes.cards.r.RavenousVampire;
import com.github.laxika.magicalvibes.cards.r.ReignOfThePit;
import com.github.laxika.magicalvibes.cards.r.Retribution;
import com.github.laxika.magicalvibes.cards.r.Rupture;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SyphonFlesh;
import com.github.laxika.magicalvibes.cards.t.TipTheScales;
import com.github.laxika.magicalvibes.cards.t.TwistedJustice;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.w.WarrenWeirding;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

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

    private void destroyWithWrathOfGod(Player caster) {
        harness.forceActivePlayer(caster);
        harness.castFromHand(caster, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
    }
}
