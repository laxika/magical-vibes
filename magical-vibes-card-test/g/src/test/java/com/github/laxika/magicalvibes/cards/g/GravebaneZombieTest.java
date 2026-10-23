package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.Abduction;
import com.github.laxika.magicalvibes.cards.a.AllIsDust;
import com.github.laxika.magicalvibes.cards.b.BarrinsSpite;
import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.d.DevourFlesh;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.i.InnocentBlood;
import com.github.laxika.magicalvibes.cards.k.KalitasTraitorOfGhet;
import com.github.laxika.magicalvibes.cards.m.MercyKilling;
import com.github.laxika.magicalvibes.cards.p.ParasiticImplant;
import com.github.laxika.magicalvibes.cards.r.Retribution;
import com.github.laxika.magicalvibes.cards.t.TwistedJustice;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.w.WarrenWeirding;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
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

    private void destroyWithWrathOfGod(Player caster) {
        harness.forceActivePlayer(caster);
        harness.castFromHand(caster, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
    }
}
