package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BloodScrivener;
import com.github.laxika.magicalvibes.cards.b.BreathstealersCrypt;
import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.c.CreepingDread;
import com.github.laxika.magicalvibes.cards.d.DiregrafScavenger;
import com.github.laxika.magicalvibes.cards.d.DoomedDissenter;
import com.github.laxika.magicalvibes.cards.e.EdgarKingOfFigaro;
import com.github.laxika.magicalvibes.cards.g.GameOfChaos;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Impulse;
import com.github.laxika.magicalvibes.cards.i.Inspiration;
import com.github.laxika.magicalvibes.cards.k.KalitasTraitorOfGhet;
import com.github.laxika.magicalvibes.cards.m.ManOWar;
import com.github.laxika.magicalvibes.cards.n.NestInvader;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.p.PactWeapon;
import com.github.laxika.magicalvibes.cards.p.PainsReward;
import com.github.laxika.magicalvibes.cards.p.PartingThoughts;
import com.github.laxika.magicalvibes.cards.r.Reanimate;
import com.github.laxika.magicalvibes.cards.r.Replenish;
import com.github.laxika.magicalvibes.cards.s.SaplingOfColfenor;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.ShowAndTell;
import com.github.laxika.magicalvibes.cards.s.SorinTheMirthless;
import com.github.laxika.magicalvibes.cards.s.StrokeOfLuck;
import com.github.laxika.magicalvibes.cards.s.SufferThePast;
import com.github.laxika.magicalvibes.cards.t.TaintedRemedy;
import com.github.laxika.magicalvibes.cards.t.TheDoctorsTomb;
import com.github.laxika.magicalvibes.cards.t.TheMasterGallifreysEnd;
import com.github.laxika.magicalvibes.cards.t.TheToymakersTrap;
import com.github.laxika.magicalvibes.cards.t.Triskaidekaphobia;
import com.github.laxika.magicalvibes.cards.v.VaporSnag;
import com.github.laxika.magicalvibes.cards.v.VrenTheRelentless;
import com.github.laxika.magicalvibes.cards.w.WheelOfSunAndMoon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.TheMasterGallifreysEndEffect;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FailOnLegacyDepartureFallback;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lich's Duel Mastery's sacrifice for a life loss that follows another change of the same spell or ability (a
 * discard, a mill, an exile, a return, a draw, a boost, a choice): its CR 616.1 question is asked after that change
 * and before any life is lost, and the change happens once.
 */
@CardUsed({LichsDuelMastery.class, GrizzlyBears.class, WheelOfSunAndMoon.class, LeylineOfTheVoid.class})
class LichsDuelMasteryRiderTest extends BaseCardTest {

    private static final String LIBRARY = "Put it on the bottom of its owner's library";
    private static final String EXILE = "Exile it";
    private static final String KALITAS = "Kalitas, Traitor of Ghet";

    @Test
    @CardUsed({CreepingDread.class, Opalescence.class, KalitasTraitorOfGhet.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Creeping Dread asks the opponent's Mastery after both discards and before any life is lost")
    void creepingDreadAsksTheMasteryAfterTheDiscards() {
        Permanent mastery = animatedShieldlessMasteryAgainstKalitas();
        harness.addToBattlefield(player1, new CreepingDread());
        Card ownDiscard = new GrizzlyBears();
        harness.setHand(player1, List.of(ownDiscard));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);

        assertMasteryAskedLibraryOrKalitas();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownDiscard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, KALITAS);

        assertMasteryExiled(mastery);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownDiscard);
        harness.assertLife(player2, 20);
        assertThat(zombieTokens()).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({PainsReward.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Pain's Reward asks the high bidder's Mastery after the auction and before the loss and the draws")
    void painsRewardAsksTheMasteryBeforeTheLossAndTheDraws() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears()));

        harness.castFromHand(player1, new PainsReward(), "{2}{B}");
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);
        harness.handleXValueChosen(player2, 5);
        harness.handleXValueChosen(player1, 0);

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        harness.handleListChoice(player2, EXILE);

        assertMasteryExiled(mastery);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(4);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({Opalescence.class, KalitasTraitorOfGhet.class, VrenTheRelentless.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("The rad counter trigger asks the Mastery after the mill and before any life is lost")
    void radCountersAskTheMasteryAfterTheMill() {
        harness.setLife(player2, 20);
        Permanent mastery = harness.addToBattlefieldAndReturn(player2, new LichsDuelMastery());
        harness.addToBattlefield(player2, new Opalescence());
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        harness.addToBattlefield(player1, new VrenTheRelentless());
        gd.playerRadCounters.put(player2.getId(), 1);
        Card milled = new GrizzlyBears();
        harness.setLibrary(player2, List.of(milled));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();

        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.options()).hasSize(2).contains(KALITAS);
        harness.assertOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(milled);
        assertThat(gd.playerRadCounters.getOrDefault(player2.getId(), 0)).isZero();
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, KALITAS);

        assertMasteryExiled(mastery);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(milled);
        harness.assertLife(player2, 20);
        assertThat(zombieTokens()).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({DiregrafScavenger.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Diregraf Scavenger asks the opponent's Mastery after the exile and before the loss and the gain")
    void graveyardExileThenLifeLossAsksTheMasteryBeforeAnyLifeChanges() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new DiregrafScavenger()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertMasteryAskedLibraryOrExile();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, LIBRARY);

        assertMasteryOnLibraryBottom(mastery);
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({SufferThePast.class, NestInvader.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Suffer the Past asks the target's Mastery after the exile and before the loss and the gain")
    void targetPlayerGraveyardExileThenLifeLossAsksTheMasteryBeforeAnyLifeChanges() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        Card exiled = new NestInvader();
        harness.setGraveyard(player2, List.of(exiled));
        harness.setHand(player1, List.of(new SufferThePast()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, 1, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(exiled.getId()));
        harness.passBothPriorities();

        assertMasteryAskedLibraryOrExile();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(exiled);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, EXILE);

        assertMasteryExiled(mastery);
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({Reanimate.class, Opalescence.class, KalitasTraitorOfGhet.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Reanimate asks the caster's Mastery after the return and before any life is lost")
    void reanimateAsksTheMasteryAfterTheReturn() {
        Permanent mastery = animatedShieldlessMasteryAgainstKalitas();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player2, List.of(new Reanimate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player2, 0, creature.getId());

        assertMasteryAskedLibraryOrKalitas();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, KALITAS);

        assertMasteryExiled(mastery);
        assertThat(findPermanents(player2, "Grizzly Bears")).hasSize(1);
        harness.assertLife(player2, 20);
        assertThat(zombieTokens()).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({StrokeOfLuck.class, Shock.class, Opalescence.class, KalitasTraitorOfGhet.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Stroke of Luck asks the caster's Mastery after the cards go to hand and before any life is lost")
    void strokeOfLuckAsksTheMasteryAfterTheCardsGoToHand() {
        Permanent mastery = animatedShieldlessMasteryAgainstKalitas();
        Card bears = new GrizzlyBears();
        harness.setLibrary(player2, List.of(bears, new Shock(), new Shock(), new Shock()));
        harness.setHand(player2, List.of(new StrokeOfLuck()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player2, 0, 0);
        harness.handleCardChosen(player2, 0);

        assertMasteryAskedLibraryOrKalitas();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(bears);
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, KALITAS);

        assertMasteryExiled(mastery);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(bears);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({PactWeapon.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Pact Weapon asks the Mastery after the draw and the boost and before any life is lost")
    void pactWeaponAsksTheMasteryAfterTheBoost() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player2, new PactWeapon()).setAttachedTo(creature.getId());
        Card drawn = new GrizzlyBears();
        harness.setLibrary(player2, List.of(drawn));
        harness.setHand(player2, List.of());

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(creature)));
        resolveAllTriggers();

        assertMasteryAskedLibraryOrExile();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, EXILE);

        assertMasteryExiled(mastery);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed({SaplingOfColfenor.class, GiantSpider.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Sapling of Colfenor asks the Mastery before the gain, the loss and the card going to hand")
    void saplingAsksTheMasteryBeforeAnythingChanges() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        Permanent sapling = addCreatureReady(player2, new SaplingOfColfenor());
        Card spider = new GiantSpider();
        harness.setLibrary(player2, List.of(spider));
        harness.setHand(player2, List.of());

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(sapling)));
        resolveAllTriggers();

        assertMasteryAskedLibraryOrExile();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(spider);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, EXILE);

        assertMasteryExiled(mastery);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(spider);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertLife(player2, 24);
    }

    @Test
    @CardUsed({TheToymakersTrap.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("The Toymaker's Trap asks the wrong guesser's Mastery before the loss and the draw")
    void toymakersTrapAsksTheMasteryBeforeTheLossAndTheDraw() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.addToBattlefield(player1, new TheToymakersTrap());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        gd.turnNumber = 2;

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "5");
        harness.handleListChoice(player2, "2");

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);

        harness.handleListChoice(player2, EXILE);

        assertMasteryExiled(mastery);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertOnBattlefield(player1, "The Toymaker's Trap");
    }

    @Test
    @CardUsed({VaporSnag.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Vapor Snag asks the bounced creature's controller's Mastery after the bounce")
    void bounceThenLifeLossAsksTheMasteryBeforeAnyLifeIsLost() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new VaporSnag()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertMasteryAskedLibraryOrExile();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(bears.getCard());
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, LIBRARY);

        assertMasteryOnLibraryBottom(mastery);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(bears.getCard());
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({Triskaidekaphobia.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Triskaidekaphobia's losing mode asks the Mastery before either player loses life")
    void triskaidekaphobiaAsksTheMasteryBeforeAnyLifeIsLost() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.addToBattlefield(player1, new Triskaidekaphobia());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1,
                "Each player with exactly 13 life loses the game, then each player loses 1 life.");
        harness.passBothPriorities();

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, EXILE);

        assertMasteryExiled(mastery);
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({TheMasterGallifreysEnd.class, CopperMyr.class, Shock.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("The Master asks the opponent's Mastery after the villainous choice and before any life is lost")
    void villainousChoiceLifeLossAsksTheMasteryBeforeAnyLifeIsLost() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.addToBattlefield(player1, new TheMasterGallifreysEnd());
        Permanent myr = harness.addToBattlefieldAndReturn(player1, new CopperMyr());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, myr.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player2, TheMasterGallifreysEndEffect.LOSE_LIFE_OPTION);

        assertMasteryAskedLibraryOrExile();
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> "Copper Myr".equals(card.getName()));
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, EXILE);

        assertMasteryExiled(mastery);
        harness.assertLife(player2, 20);
        assertThat(findPermanents(player1, "Copper Myr")).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({SorinTheMirthless.class, DoomedDissenter.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Accepting Sorin's +1 asks the Mastery before the card leaves the library")
    void sorinAsksTheMasteryBeforeTheCardMoves() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        Permanent sorin = harness.addToBattlefieldAndReturn(player2, new SorinTheMirthless());
        sorin.setCounterCount(CounterType.LOYALTY, 4);
        sorin.setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Card topCard = new DoomedDissenter();
        harness.setLibrary(player2, List.of(topCard));
        harness.setHand(player2, List.of());

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(sorin), 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertMasteryAskedLibraryOrExile();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, EXILE);

        assertMasteryExiled(mastery);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({BreathstealersCrypt.class, ManOWar.class, Impulse.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Paying Breathstealer's Crypt's life asks the Mastery before any life is paid")
    void cryptLifePaymentAsksTheMasteryBeforeAnyLifeIsPaid() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.addToBattlefield(player1, new BreathstealersCrypt());
        harness.setLibrary(player2, List.of(new ManOWar(), new Impulse()));
        harness.setHand(player2, List.of());
        gd.turnNumber = 2;
        advanceToUpkeep(player2);
        harness.passUntil(player2, TurnStep.DRAW);

        harness.handleMayAbilityChosen(player2, true);

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        harness.assertInHand(player2, "Man-o'-War");

        harness.handleListChoice(player2, EXILE);

        assertMasteryExiled(mastery);
        harness.assertLife(player2, 20);
        harness.assertInHand(player2, "Man-o'-War");
    }

    @Test
    @CardUsed({PartingThoughts.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Parting Thoughts asks the caster's Mastery after the destruction and the draws")
    void partingThoughtsAsksTheMasteryAfterTheDraws() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player2, List.of(new PartingThoughts()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player2, 0, target.getId());

        assertMasteryAskedLibraryOrExile();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, EXILE);

        assertMasteryExiled(mastery);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({GameOfChaos.class, EdgarKingOfFigaro.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Game of Chaos asks the losing opponent's Mastery after the flip, without flipping again")
    void gameOfChaosAsksTheMasteryAfterTheFlipWithoutFlippingAgain() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.addToBattlefield(player1, new EdgarKingOfFigaro());
        harness.setHand(player1, List.of(new GameOfChaos()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertMasteryAskedLibraryOrExile();
        assertThat(flipsLogged()).isEqualTo(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, EXILE);

        assertMasteryExiled(mastery);
        assertThat(flipsLogged()).isEqualTo(1);
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @CardUsed({GameOfChaos.class, EdgarKingOfFigaro.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Flipping Game of Chaos again asks the losing opponent's Mastery after the flip, without flipping again")
    void gameOfChaosFlipAgainAsksTheMasteryAfterTheFlipWithoutFlippingAgain() {
        harness.addToBattlefield(player1, new EdgarKingOfFigaro());
        harness.setHand(player1, List.of(new GameOfChaos()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.assertLife(player2, 19);
        Permanent mastery = harness.addToBattlefieldAndReturn(player2, new LichsDuelMastery());
        harness.addToBattlefieldAndReturn(player2, new WheelOfSunAndMoon()).setAttachedTo(player2.getId());
        harness.addToBattlefield(player1, new LeylineOfTheVoid());
        gd.playersWhoFlippedCoinsThisTurn.clear();

        harness.handleMayAbilityChosen(player1, true);

        assertMasteryAskedLibraryOrExile();
        assertThat(flipsLogged()).isEqualTo(2);
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);

        harness.handleListChoice(player2, LIBRARY);

        assertMasteryOnLibraryBottom(mastery);
        assertThat(flipsLogged()).isEqualTo(2);
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 19);
    }

    @Test
    @CardUsed({BloodScrivener.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Blood Scrivener's draw step replacement asks the Mastery before either card is drawn")
    void bloodScrivenerDrawStepAsksTheMasteryBeforeTheDraws() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.addToBattlefield(player2, new BloodScrivener());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        gd.turnNumber = 2;
        advanceToUpkeep(player2);
        harness.passUntil(player2, TurnStep.DRAW);

        assertMasteryAskedLibraryOrExile();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, EXILE);

        assertMasteryExiled(mastery);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed({BloodScrivener.class, Inspiration.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Blood Scrivener's replacement of a spell's draw asks the Mastery before any card is drawn")
    void bloodScrivenerSpellDrawAsksTheMasteryBeforeTheDraws() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.addToBattlefield(player2, new BloodScrivener());
        harness.setHand(player2, List.of(new Inspiration()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player2, 0, player2.getId());
        harness.passBothPriorities();

        assertMasteryAskedLibraryOrExile();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, EXILE);

        assertMasteryExiled(mastery);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).filteredOn(GrizzlyBears.class::isInstance).hasSize(1);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({TheDoctorsTomb.class, Shock.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("The Doctor's Tomb's life loss asks the Mastery in the same choice as the dying creature")
    void doctorsTombLifeLossAsksTheMasteryBeforeTheCreatureLeaves() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new TheDoctorsTomb(), gd.nextTimestamp()));
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        var bearsChoice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(bearsChoice.playerId()).isEqualTo(player2.getId());
        assertThat(bearsChoice.options()).contains("The Doctor's Tomb");
        harness.handleListChoice(player2, "The Doctor's Tomb");

        assertMasteryAskedLibraryOrExile();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, EXILE);

        assertMasteryExiled(mastery);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.findExiledCard(bears.getCard().getId())).isNotNull();
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({Lich.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Lich asks its caster's Mastery before it enters")
    void lichAsksTheMasteryBeforeItEnters() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player2, new Lich(), "{B}{B}{B}{B}");
        harness.passBothPriorities();

        assertMasteryAskedLibraryOrExile();
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Lich");
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, EXILE);

        assertMasteryExiled(mastery);
        harness.assertOnBattlefield(player2, "Lich");
        assertThat(gd.stack).extracting(entry -> entry.getDescription())
                .containsExactly("Lich's Duel Mastery's ability");
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({SaplingOfColfenor.class, GiantSpider.class, TaintedRemedy.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Sapling of Colfenor asks the Mastery up front when the gain turned into a loss uses up its last shield")
    void saplingAsksTheMasteryWhenTheReplacedGainUsesUpTheLastShield() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        Card shield = new GrizzlyBears();
        gd.exiledCards.add(new ExiledCardEntry(shield, player2.getId(), mastery.getId(), true, gd.turnNumber));
        harness.addToBattlefield(player1, new TaintedRemedy());
        Permanent sapling = addCreatureReady(player2, new SaplingOfColfenor());
        Card spider = new GiantSpider();
        harness.setLibrary(player2, List.of(spider));
        harness.setHand(player2, List.of());

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(sapling)));
        resolveAllTriggers();

        assertMasteryAskedLibraryOrExile();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(spider);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, EXILE);

        assertMasteryExiled(mastery);
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyInAnyOrder(shield, spider);
        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed({Replenish.class, Lich.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Lich returned by Replenish asks its controller's Mastery before it enters")
    void lichReturnedByAnEffectAsksTheMasteryBeforeItEnters() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        Card lich = new Lich();
        harness.setGraveyard(player2, List.of(lich));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player2, new Replenish(), "{3}{W}");
        harness.passBothPriorities();

        assertMasteryAskedLibraryOrExile();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(lich);
        harness.assertNotOnBattlefield(player2, "Lich");
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, EXILE);

        assertMasteryExiled(mastery);
        harness.assertOnBattlefield(player2, "Lich");
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(lich);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({ShowAndTell.class, Lich.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Lich put onto the battlefield by Show and Tell asks its controller's Mastery before it enters")
    void lichPutFromHandAsksTheMasteryBeforeItEnters() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        Card lich = new Lich();
        harness.setHand(player1, List.of(new ShowAndTell()));
        harness.setHand(player2, List.of(lich));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player2, List.of(lich.getId()));

        assertMasteryAskedLibraryOrExile();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(lich);
        harness.assertNotOnBattlefield(player2, "Lich");
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, EXILE);

        assertMasteryExiled(mastery);
        harness.assertOnBattlefield(player2, "Lich");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private long flipsLogged() {
        return gd.gameLog.stream().filter(entry -> entry.plainText().contains(" the flip (stakes ")).count();
    }

    private void assertMasteryAskedLibraryOrExile() {
        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.options()).containsExactlyInAnyOrder(LIBRARY, EXILE);
        harness.assertOnBattlefield(player2, "Lich's Duel Mastery");
    }

    private void assertMasteryAskedLibraryOrKalitas() {
        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.options()).containsExactlyInAnyOrder(LIBRARY, KALITAS);
        harness.assertOnBattlefield(player2, "Lich's Duel Mastery");
    }

    private void assertMasteryExiled(Permanent mastery) {
        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId()).contains(mastery.getCard().getId());
    }

    private void assertMasteryOnLibraryBottom(Permanent mastery) {
        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getId()).isEqualTo(mastery.getCard().getId());
    }

    /**
     * Player 2 controls a noncreature Mastery without shields that Wheel of Sun and Moon's enchanted player owns;
     * player 1 controls Leyline of the Void. Sacrificing the Mastery then needs its controller's choice between the
     * library and the exile (CR 616.1).
     */
    private Permanent noncreatureShieldlessMasteryBetweenWheelAndLeyline() {
        harness.setLife(player2, 20);
        Permanent mastery = harness.addToBattlefieldAndReturn(player2, new LichsDuelMastery());
        harness.addToBattlefieldAndReturn(player2, new WheelOfSunAndMoon()).setAttachedTo(player2.getId());
        harness.addToBattlefield(player1, new LeylineOfTheVoid());
        return mastery;
    }

    /**
     * Player 2 controls a Mastery without shields that Opalescence makes a creature and that Wheel of Sun and Moon's
     * enchanted player owns; player 1 controls Kalitas. Sacrificing the Mastery then needs its controller's choice
     * between the library and Kalitas's exile (CR 616.1), while no other card's move to a graveyard asks anything.
     */
    private Permanent animatedShieldlessMasteryAgainstKalitas() {
        harness.setLife(player2, 20);
        Permanent mastery = harness.addToBattlefieldAndReturn(player2, new LichsDuelMastery());
        harness.addToBattlefield(player2, new Opalescence());
        harness.addToBattlefieldAndReturn(player2, new WheelOfSunAndMoon()).setAttachedTo(player2.getId());
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        return mastery;
    }

    private long zombieTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count();
    }
}
