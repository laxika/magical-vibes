package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AdNauseam;
import com.github.laxika.magicalvibes.cards.a.AlabasterLeech;
import com.github.laxika.magicalvibes.cards.b.BlexVexingPest;
import com.github.laxika.magicalvibes.cards.b.BogRats;
import com.github.laxika.magicalvibes.cards.d.DruidOfTheAnima;
import com.github.laxika.magicalvibes.cards.g.GoblinGame;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.IllicitAuction;
import com.github.laxika.magicalvibes.cards.k.KaervekThePunisher;
import com.github.laxika.magicalvibes.cards.k.KalitasTraitorOfGhet;
import com.github.laxika.magicalvibes.cards.m.MagesContest;
import com.github.laxika.magicalvibes.cards.m.MenacingOgre;
import com.github.laxika.magicalvibes.cards.m.MoonlightBargain;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.p.PlagueOfVermin;
import com.github.laxika.magicalvibes.cards.p.PlungeIntoDarkness;
import com.github.laxika.magicalvibes.cards.s.SwordPointDiplomacy;
import com.github.laxika.magicalvibes.cards.s.SylvanLibrary;
import com.github.laxika.magicalvibes.cards.v.VizkopaConfessor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.cards.a.AetherRift;
import com.github.laxika.magicalvibes.cards.b.BloodletterOfAclazotz;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.k.KefkaDancingMad;
import com.github.laxika.magicalvibes.cards.t.ToilTrouble;
import com.github.laxika.magicalvibes.cards.t.TragicSlip;
import com.github.laxika.magicalvibes.cards.a.ArrogantPoet;
import com.github.laxika.magicalvibes.cards.a.AthreosGodOfPassage;
import com.github.laxika.magicalvibes.cards.b.BronzeTablet;
import com.github.laxika.magicalvibes.cards.c.Cleansing;
import com.github.laxika.magicalvibes.cards.c.CityOfShadows;
import com.github.laxika.magicalvibes.cards.d.DashHopes;
import com.github.laxika.magicalvibes.cards.e.EmberwildeDjinn;
import com.github.laxika.magicalvibes.cards.f.FearMyAuthority;
import com.github.laxika.magicalvibes.cards.f.FeralShadow;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GodlessShrine;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KavuPredator;
import com.github.laxika.magicalvibes.cards.m.MazeOfIth;
import com.github.laxika.magicalvibes.cards.p.ProtectionRacket;
import com.github.laxika.magicalvibes.cards.p.Purgatory;
import com.github.laxika.magicalvibes.cards.r.Rebirth;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TempestEfreet;
import com.github.laxika.magicalvibes.cards.w.WandOfDenial;
import com.github.laxika.magicalvibes.cards.w.WandOfIth;
import com.github.laxika.magicalvibes.cards.w.WheelOfSunAndMoon;
import com.github.laxika.magicalvibes.cards.z.ZursWeirding;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FailOnLegacyDepartureFallback;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lich's Duel Mastery replaces a life payment (paying life is losing it, CR 119.4) made while an effect resolves or
 * while a "you may pay life" prompt is answered: with no shield left its sacrifice, whose death replacements
 * (Wheel of Sun and Moon against Leyline of the Void) its controller chooses before anything of the payment's
 * answer happens (CR 616.1).
 */
@CardUsed({LichsDuelMastery.class, WheelOfSunAndMoon.class, LeylineOfTheVoid.class, GrizzlyBears.class})
class LichsDuelMasteryLifePaymentEffectTest extends BaseCardTest {

    @Test
    @CardUsed(GodlessShrine.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("Paying 2 life for a shock land asks the Mastery's sacrifice before the payment")
    void shockLandPaymentAsksTheMasteryFirst() {
        Permanent mastery = shieldlessMasteryBetweenWheelAndLeyline(player1);
        harness.setHand(player1, List.of(new GodlessShrine()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);

        harness.handleMayAbilityChosen(player1, true);

        assertMasteryAsked(player1);
        harness.assertLife(player1, 20);

        answerExile(player1, mastery);

        harness.assertLife(player1, 20);
        assertThat(findPermanent(player1, "Godless Shrine").isTapped()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed(ArrogantPoet.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("\"You may pay 2 life. If you do, ...\" asks the Mastery's sacrifice before the payment")
    void mayPayLifeAsksTheMasteryFirst() {
        Permanent mastery = shieldlessMasteryBetweenWheelAndLeyline(player1);
        Permanent poet = addCreatureReady(player1, new ArrogantPoet());

        declareAttackers(List.of(indexOf(player1, poet)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertMasteryAsked(player1);
        harness.assertLife(player1, 20);
        assertThat(gqs.hasKeyword(gd, poet, Keyword.FLYING)).isFalse();

        answerExile(player1, mastery);

        harness.assertLife(player1, 20);
        assertThat(gqs.hasKeyword(gd, poet, Keyword.FLYING)).isTrue();
    }

    @Test
    @CardUsed(AthreosGodOfPassage.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("Athreos's \"unless target opponent pays 3 life\" payment asks the Mastery's sacrifice first")
    void athreosPaymentAsksTheMasteryFirst() {
        Permanent mastery = shieldlessMasteryBetweenWheelAndLeyline(player2);
        harness.addToBattlefield(player1, new AthreosGodOfPassage());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertMasteryAsked(player2);
        harness.assertLife(player2, 20);

        answerExile(player2, mastery);

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @CardUsed({ProtectionRacket.class, SakuraTribeElder.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Protection Racket's payment asks the Mastery's sacrifice before the revealed card moves")
    void protectionRacketPaymentAsksTheMasteryBeforeTheCardMoves() {
        Permanent mastery = shieldlessMasteryBetweenWheelAndLeyline(player2);
        harness.addToBattlefield(player1, new ProtectionRacket());
        SakuraTribeElder elder = new SakuraTribeElder();
        harness.setLibrary(player1, List.of(elder));
        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player2, true);

        assertMasteryAsked(player2);
        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(elder);

        answerExile(player2, mastery);

        harness.assertLife(player2, 20);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(elder);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @CardUsed({Cleansing.class, CityOfShadows.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Cleansing's payment to keep a land asks the Mastery's sacrifice first")
    void cleansingPaymentAsksTheMasteryFirst() {
        Permanent mastery = shieldlessMasteryBetweenWheelAndLeyline(player2);
        harness.addToBattlefield(player2, new CityOfShadows());
        harness.castFromHand(player1, new Cleansing(), "{W}{W}{W}");
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        assertMasteryAsked(player2);
        harness.assertLife(player2, 20);

        answerExile(player2, mastery);

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "City of Shadows");
    }

    @Test
    @CardUsed(AetherRift.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("Aether Rift's payment to keep the creature away asks the Mastery's sacrifice first")
    void aetherRiftPaymentAsksTheMasteryFirst() {
        Permanent mastery = shieldlessMasteryBetweenWheelAndLeyline(player2);
        harness.addToBattlefield(player1, new AetherRift());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        assertMasteryAsked(player2);
        harness.assertLife(player2, 20);

        answerExile(player2, mastery);

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @CardUsed({WandOfIth.class, MazeOfIth.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Wand of Ith's payment to keep the revealed card asks the Mastery's sacrifice first")
    void wandOfIthPaymentAsksTheMasteryFirst() {
        Permanent mastery = shieldlessMasteryBetweenWheelAndLeyline(player2);
        harness.setHand(player2, List.of(new MazeOfIth()));
        Permanent wand = harness.addToBattlefieldAndReturn(player1, new WandOfIth());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, indexOf(player1, wand), null, player2.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);

        assertMasteryAsked(player2);
        harness.assertLife(player2, 20);

        answerExile(player2, mastery);

        harness.assertLife(player2, 20);
        harness.assertInHand(player2, "Maze of Ith");
    }

    @Test
    @CardUsed(BronzeTablet.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("Bronze Tablet's 10-life payment asks the Mastery's sacrifice before the Tablet moves")
    void bronzeTabletPaymentAsksTheMasteryFirst() {
        Permanent mastery = shieldlessMasteryBetweenWheelAndLeyline(player2);
        Permanent tablet = harness.enterBattlefieldAndReturn(player1, new BronzeTablet());
        tablet.untap();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.activateAbility(player1, indexOf(player1, tablet), null, bears.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);

        assertMasteryAsked(player2);
        harness.assertLife(player2, 20);
        assertThat(inExile("Bronze Tablet")).isTrue();

        answerExile(player2, mastery);

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Bronze Tablet");
        assertThat(inExile("Grizzly Bears")).isTrue();
    }

    @Test
    @CardUsed(TempestEfreet.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("Tempest Efreet's 10-life payment asks the Mastery's sacrifice first")
    void tempestEfreetPaymentAsksTheMasteryFirst() {
        Permanent mastery = shieldlessMasteryBetweenWheelAndLeyline(player2);
        harness.setHand(player1, new ArrayList<>());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        Permanent efreet = addCreatureReady(player1, new TempestEfreet());
        harness.activateAbility(player1, indexOf(player1, efreet), null, player2.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);

        assertMasteryAsked(player2);
        harness.assertLife(player2, 20);

        answerExile(player2, mastery);

        harness.assertLife(player2, 20);
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @CardUsed(Rebirth.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("Rebirth's \"life total becomes 20\" from 30 asks the Mastery's sacrifice before the ante")
    void rebirthAsksTheMasteryBeforeTheAnte() {
        Permanent mastery = shieldlessMasteryBetweenWheelAndLeyline(player2);
        harness.setLife(player2, 30);
        Card anted = new GrizzlyBears();
        harness.setLibrary(player2, List.of(anted));
        harness.castFromHand(player1, new Rebirth(), "{3}{G}{G}{G}");
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        assertMasteryAsked(player2);
        harness.assertLife(player2, 30);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(anted);

        answerExile(player2, mastery);

        harness.assertLife(player2, 30);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.antedCardIds).containsExactly(anted.getId());
    }

    @Test
    @CardUsed({DashHopes.class, KavuPredator.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Paying 5 life to counter Dash Hopes asks the Mastery's sacrifice before the counter")
    void dashHopesPaymentAsksTheMasteryFirst() {
        Permanent mastery = shieldlessMasteryBetweenWheelAndLeyline(player1);
        KavuPredator predator = new KavuPredator();
        harness.castFromHand(player1, predator, "{1}{G}");
        harness.setHand(player2, List.of(new DashHopes()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, predator.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertMasteryAsked(player1);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Dash Hopes"));

        answerExile(player1, mastery);

        harness.assertLife(player1, 20);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Dash Hopes"));
        harness.assertInGraveyard(player2, "Dash Hopes");
    }

    @Test
    @CardUsed(ZursWeirding.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("Zur's Weirding's payment to deny a draw asks the Mastery's sacrifice before the card moves")
    void zursWeirdingPaymentAsksTheMasteryFirst() {
        Permanent mastery = shieldlessMasteryBetweenWheelAndLeyline(player2);
        harness.addToBattlefield(player1, new ZursWeirding());
        Card revealed = new GrizzlyBears();
        harness.setLibrary(player1, List.of(revealed, new Forest()));
        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);

        assertMasteryAsked(player2);
        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player1.getId())).contains(revealed);

        answerExile(player2, mastery);

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @CardUsed(WandOfDenial.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("Wand of Denial's payment asks the Mastery's sacrifice before the card is milled")
    void wandOfDenialPaymentAsksTheMasteryFirst() {
        Permanent mastery = shieldlessMasteryBetweenWheelAndLeyline(player1);
        Permanent wand = harness.addToBattlefieldAndReturn(player1, new WandOfDenial());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(topCard));
        harness.activateAbility(player1, indexOf(player1, wand), null, player2.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertMasteryAsked(player1);
        harness.assertLife(player1, 20);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);

        answerExile(player1, mastery);

        harness.assertLife(player1, 20);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @CardUsed(EmberwildeDjinn.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("\"Pay {R}{R} or 2 life\" paid with life asks the Mastery's sacrifice first")
    void manaOrLifePaymentAsksTheMasteryFirst() {
        Permanent mastery = shieldlessMasteryBetweenWheelAndLeyline(player2);
        UUID djinnId = harness.addToBattlefieldAndReturn(player1, new EmberwildeDjinn()).getId();
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);

        assertMasteryAsked(player2);
        harness.assertLife(player2, 20);
        assertThat(controls(player1, djinnId)).isTrue();

        answerExile(player2, mastery);

        harness.assertLife(player2, 20);
        assertThat(controls(player2, djinnId)).isTrue();
    }

    @Test
    @CardUsed({Purgatory.class, FeralShadow.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Purgatory's \"pay {4} and 2 life\" asks the Mastery's sacrifice before any of it is paid")
    void manaAndLifePaymentAsksTheMasteryBeforeTheManaIsSpent() {
        Permanent mastery = shieldlessMasteryBetweenWheelAndLeyline(player1);
        UUID purgatoryId = harness.addToBattlefieldAndReturn(player1, new Purgatory()).getId();
        gd.addToExile(player1.getId(), new FeralShadow(), purgatoryId);
        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertMasteryAsked(player1);
        harness.assertLife(player1, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(4);

        answerExile(player1, mastery);

        harness.assertLife(player1, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        harness.assertOnBattlefield(player1, "Feral Shadow");
    }

    @Test
    @CardUsed({FearMyAuthority.class, Shock.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Paying 3 life to keep a scheme asks the Mastery's sacrifice first")
    void schemePaymentAsksTheMasteryFirst() {
        Permanent mastery = shieldlessMasteryBetweenWheelAndLeyline(player1);
        Permanent scheme = harness.addToBattlefieldAndReturn(player1, new FearMyAuthority());
        harness.setHand(player1, List.of(new Shock()));
        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player1, false);

        assertMasteryAsked(player1);
        harness.assertLife(player1, 20);

        answerExile(player1, mastery);

        harness.assertLife(player1, 20);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(scheme);
    }

    @Test
    @CardUsed(PlagueOfVermin.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("Plague of Vermin's life payment asks the Mastery's sacrifice before the payment")
    void payAnyLifeForTokensAsksTheMasteryFirst() {
        Permanent mastery = shieldlessMasteryBetweenWheelAndLeyline(player2);
        PlagueOfVermin plague = new PlagueOfVermin();
        plague.setSetCode("SHM");
        plague.setCollectorNumber("73");
        harness.setHand(player1, List.of(plague));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.handleXValueChosen(player1, 0);
        harness.handleXValueChosen(player2, 3);

        assertMasteryAsked(player2);
        harness.assertLife(player2, 20);

        answerExile(player2, mastery);
        harness.handleXValueChosen(player1, 0);
        harness.handleXValueChosen(player2, 0);

        harness.assertLife(player2, 20);
        assertThat(countPermanents(player2, "Rat")).isEqualTo(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed(PlungeIntoDarkness.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("\"Pay any amount of life\" as an effect asks the Mastery's sacrifice before the payment")
    void payXLifeAsksTheMasteryFirst() {
        Permanent mastery = animatedShieldlessMasteryAgainstKalitas(player1);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setHand(player1, List.of(new PlungeIntoDarkness()));
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{1}, List.of());
        harness.passBothPriorities();

        harness.handleXValueChosen(player1, 2);

        assertMasteryAskedLibraryOrKalitas(player1);
        harness.assertLife(player1, 20);

        answerLibrary(player1, mastery);

        harness.assertLife(player1, 20);
        assertThat(gd.interaction.activeInteraction()).isNotNull();
    }

    @Test
    @CardUsed(VizkopaConfessor.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("Vizkopa Confessor's life payment asks the Mastery's sacrifice before anything is revealed")
    void payAnyLifeRevealAsksTheMasteryFirst() {
        Permanent mastery = shieldlessMasteryBetweenWheelAndLeyline(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));
        harness.setHand(player1, new ArrayList<>(List.of(new VizkopaConfessor())));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleXValueChosen(player1, 2);

        assertMasteryAsked(player1);
        harness.assertLife(player1, 20);

        answerExile(player1, mastery);

        harness.assertLife(player1, 20);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.RevealCardsDiscardChoice.class);
    }

    @Test
    @CardUsed({MagesContest.class, AlabasterLeech.class, KalitasTraitorOfGhet.class, Opalescence.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Mages' Contest's high bid asks the Mastery's sacrifice before the bid is recorded")
    void magesContestHighBidAsksTheMasteryFirst() {
        Permanent mastery = animatedShieldlessMasteryAgainstKalitas(player2);
        AlabasterLeech leech = new AlabasterLeech();
        harness.setHand(player1, List.of(leech));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player2, List.of(new MagesContest()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, leech.getId());

        harness.handleXValueChosen(player1, 5);
        harness.handleXValueChosen(player2, 8);
        harness.handleXValueChosen(player1, 0);

        assertMasteryAskedLibraryOrKalitas(player2);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(leech.getId()));

        answerLibrary(player2, mastery);

        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Alabaster Leech");
    }

    @Test
    @CardUsed(IllicitAuction.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("Illicit Auction's high bid asks the Mastery's sacrifice before the bid is recorded")
    void illicitAuctionHighBidAsksTheMasteryFirst() {
        Permanent mastery = shieldlessMasteryBetweenWheelAndLeyline(player2);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IllicitAuction()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.handleXValueChosen(player2, 5);
        harness.handleXValueChosen(player1, 0);

        assertMasteryAsked(player2);
        harness.assertLife(player2, 20);

        answerExile(player2, mastery);

        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed(GoblinGame.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("Goblin Game's losses ask the Mastery's sacrifice before any life total changes")
    void goblinGameAsksTheMasteryBeforeAnyLifeChanges() {
        Permanent mastery = shieldlessMasteryBetweenWheelAndLeyline(player2);
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new GoblinGame(), "{5}{R}{R}");
        harness.passBothPriorities();

        harness.handleXValueChosen(player1, 1);
        harness.handleXValueChosen(player2, 2);

        assertMasteryAsked(player2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        answerExile(player2, mastery);

        harness.assertLife(player1, 9);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed(GoblinGame.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("Goblin Game's \"then loses half their life\" asks the Mastery once its last shield is gone")
    void goblinGameHalfLifeLossAsksTheMasteryAfterTheFirstLoss() {
        Permanent mastery = shieldlessMasteryBetweenWheelAndLeyline(player2);
        Card shield = addShield(player2, mastery);
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new GoblinGame(), "{5}{R}{R}");
        harness.passBothPriorities();

        harness.handleXValueChosen(player1, 3);
        harness.handleXValueChosen(player2, 1);

        assertMasteryAsked(player2);
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).contains(shield);

        answerExile(player2, mastery);

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed(MenacingOgre.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("Menacing Ogre's losses ask the Mastery's sacrifice before any life total changes")
    void menacingOgreAsksTheMasteryBeforeAnyLifeChanges() {
        Permanent mastery = shieldlessMasteryBetweenWheelAndLeyline(player2);
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new MenacingOgre(), "{3}{R}{R}");
        resolveAllTriggers();
        Permanent ogre = findPermanent(player1, "Menacing Ogre");

        harness.handleXValueChosen(player1, 5);
        harness.handleXValueChosen(player2, 5);

        assertMasteryAsked(player2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        answerExile(player2, mastery);

        harness.assertLife(player1, 15);
        harness.assertLife(player2, 20);
        assertThat(ogre.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @CardUsed({AdNauseam.class, DruidOfTheAnima.class, KalitasTraitorOfGhet.class, Opalescence.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Ad Nauseam asks the Mastery's sacrifice before the revealed card moves")
    void adNauseamAsksTheMasteryBeforeTheCardMoves() {
        Permanent mastery = animatedShieldlessMasteryAgainstKalitas(player1);
        Card revealed = new DruidOfTheAnima();
        harness.setLibrary(player1, new ArrayList<>(List.of(revealed, new DruidOfTheAnima())));
        harness.castFromHand(player1, new AdNauseam(), "{3}{B}{B}");
        harness.passBothPriorities();

        assertMasteryAskedLibraryOrKalitas(player1);
        harness.assertLife(player1, 20);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isEqualTo(revealed);

        answerLibrary(player1, mastery);

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealed);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.AdNauseamRepeatChoice.class);
    }

    @Test
    @CardUsed({AdNauseam.class, DruidOfTheAnima.class, KalitasTraitorOfGhet.class, Opalescence.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Ad Nauseam's repeat asks the Mastery's sacrifice before the next card moves")
    void adNauseamRepeatAsksTheMasteryBeforeTheCardMoves() {
        Permanent mastery = animatedShieldlessMasteryAgainstKalitas(player1);
        Card land = new Forest();
        Card revealed = new DruidOfTheAnima();
        harness.setLibrary(player1, new ArrayList<>(List.of(land, revealed, new DruidOfTheAnima())));
        harness.castFromHand(player1, new AdNauseam(), "{3}{B}{B}");
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);

        harness.handleMayAbilityChosen(player1, true);

        assertMasteryAskedLibraryOrKalitas(player1);
        harness.assertLife(player1, 20);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isEqualTo(revealed);

        answerLibrary(player1, mastery);

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land, revealed);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.AdNauseamRepeatChoice.class);
    }

    @Test
    @CardUsed({KaervekThePunisher.class, BogRats.class, Shock.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Kaervek's \"if you do, you lose 2 life\" asks the Mastery's sacrifice after the cast")
    void castCopyLifeLossAsksTheMasteryAfterTheCast() {
        Permanent mastery = shieldlessMasteryBetweenWheelAndLeyline(player1);
        harness.addToBattlefield(player1, new KaervekThePunisher());
        Card graveyardCard = new BogRats();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertMasteryAsked(player1);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Bog Rats"));

        answerExile(player1, mastery);

        harness.assertLife(player1, 20);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Bog Rats"));
    }

    @Test
    @CardUsed({MoonlightBargain.class, KalitasTraitorOfGhet.class, Opalescence.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Moonlight Bargain's second payment asks the Mastery's sacrifice before any card moves")
    void moonlightBargainAsksTheMasteryBeforeAnyCardMoves() {
        Permanent mastery = animatedShieldlessMasteryAgainstKalitas(player1);
        Card shield = addShield(player1, mastery);
        List<Card> top = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            top.add(new GrizzlyBears());
        }
        harness.setLibrary(player1, new ArrayList<>(top));
        harness.castFromHand(player1, new MoonlightBargain(), "{3}{B}{B}");
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(top.get(0).getId(), top.get(3).getId()));

        assertMasteryAskedLibraryOrKalitas(player1);
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContainAnyElementsOf(top);

        answerLibrary(player1, mastery);

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(top.get(0), top.get(3), shield);
    }

    @Test
    @CardUsed({BlexVexingPest.class, KalitasTraitorOfGhet.class, Opalescence.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Search for Blex's loss for the cards kept asks the Mastery's sacrifice before any card moves")
    void searchForBlexAsksTheMasteryBeforeAnyCardMoves() {
        Permanent mastery = animatedShieldlessMasteryAgainstKalitas(player1);
        Card first = new GrizzlyBears();
        Card second = new HillGiant();
        harness.setLibrary(player1, new ArrayList<>(List.of(first, second, new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears())));
        harness.setHand(player1, List.of(new BlexVexingPest()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castModalSorcery(player1, 0, 1, List.of());
        resolveAllTriggers();

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        assertMasteryAskedLibraryOrKalitas(player1);
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        answerLibrary(player1, mastery);

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
    }

    @Test
    @CardUsed(SwordPointDiplomacy.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("Sword-Point Diplomacy's payment to deny a card asks the Mastery's sacrifice before any card moves")
    void swordPointDiplomacyAsksTheMasteryBeforeAnyCardMoves() {
        Permanent mastery = shieldlessMasteryBetweenWheelAndLeyline(player2);
        Card revealed = new HillGiant();
        Card kept = new GrizzlyBears();
        harness.setLibrary(player1, List.of(revealed, kept));
        harness.setHand(player1, List.of(new SwordPointDiplomacy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player2, List.of(revealed.getId()));

        assertMasteryAsked(player2);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        answerExile(player2, mastery);

        harness.assertLife(player2, 20);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(revealed);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
    }

    @Test
    @CardUsed(SylvanLibrary.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("Sylvan Library's second payment asks the Mastery's sacrifice before any card moves")
    void sylvanLibraryAsksTheMasteryBeforeAnyCardMoves() {
        Permanent mastery = shieldlessMasteryBetweenWheelAndLeyline(player1);
        Card shield = addShield(player1, mastery);
        harness.addToBattlefield(player1, new SylvanLibrary());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.DRAW);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.handleMultipleCardsChosen(player1, List.of());

        assertMasteryAsked(player1);
        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3).doesNotContain(shield);

        answerExile(player1, mastery);

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4).contains(shield);
    }

    @Test
    @CardUsed(LimDLsVault.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("Lim-Dûl's Vault's 1-life payment asks the Mastery's sacrifice before the cards are bottomed")
    void limDulsVaultPaymentAsksTheMasteryFirst() {
        Permanent mastery = shieldlessMasteryBetweenWheelAndLeyline(player1);
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            library.add(new GrizzlyBears());
        }
        harness.setLibrary(player1, new ArrayList<>(library));
        harness.castFromHand(player1, new LimDLsVault(), "{U}{B}");
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertMasteryAsked(player1);
        harness.assertLife(player1, 20);

        answerExile(player1, mastery);

        harness.assertLife(player1, 20);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LimDulsVaultOrderChoice.class);
    }

    @Test
    @CardUsed({KaervekThePunisher.class, TragicSlip.class, Shock.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Kaervek's life loss after a targeted copy asks the Mastery's sacrifice after the cast")
    void targetedCastCopyLifeLossAsksTheMasteryAfterTheCast() {
        Permanent mastery = shieldlessMasteryBetweenWheelAndLeyline(player1);
        harness.addToBattlefield(player1, new KaervekThePunisher());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card graveyardCard = new TragicSlip();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.handlePermanentChosen(player1, creature.getId());

        assertMasteryAsked(player1);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Tragic Slip"));

        answerExile(player1, mastery);

        harness.assertLife(player1, 20);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Tragic Slip"));
    }

    @Test
    @CardUsed({KaervekThePunisher.class, ToilTrouble.class, Shock.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Kaervek's life loss after a split card copy asks the Mastery's sacrifice after the cast")
    void splitCastCopyLifeLossAsksTheMasteryAfterTheCast() {
        Permanent mastery = shieldlessMasteryBetweenWheelAndLeyline(player1);
        harness.addToBattlefield(player1, new KaervekThePunisher());
        Card graveyardCard = new ToilTrouble();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        var halves = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, halves.options().stream()
                .filter(option -> option.startsWith("Toil")).findFirst().orElseThrow());
        if (gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class) == null
                || !gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options()
                .contains("Exile it")) {
            harness.handlePermanentChosen(player1, player2.getId());
        }

        assertMasteryAsked(player1);
        harness.assertLife(player1, 20);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Toil"));

        answerExile(player1, mastery);

        harness.assertLife(player1, 20);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Toil"));
    }

    @Test
    @CardUsed({KefkaDancingMad.class, SolRing.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Kefka's \"then each owner loses life\" asks the Mastery's sacrifice after the cast")
    void freeCastOwnerLifeLossAsksTheMasteryAfterTheCast() {
        Permanent mastery = shieldlessMasteryBetweenWheelAndLeyline(player2);
        addCreatureReady(player1, new KefkaDancingMad());
        SolRing spell = new SolRing();
        spell.setOwnerId(player2.getId());
        harness.setGraveyard(player2, List.of(spell));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.withAutoStop(TurnStep.END_STEP, () -> {
            harness.passUntil(player1, TurnStep.END_STEP);
            resolveAllTriggers();
        });

        harness.handleMultipleCardsChosen(player1, List.of(spell.getId()));

        assertMasteryAsked(player2);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(spell.getId()));

        answerExile(player2, mastery);

        harness.assertLife(player2, 20);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(spell.getId()));
    }

    @Test
    @CardUsed({Cleansing.class, CityOfShadows.class, BloodletterOfAclazotz.class})
    @DisplayName("Cleansing's 1-life payment during the Bloodletter's controller's turn loses 2 life, not 4")
    void cleansingPaymentIsDoubledOnceByBloodletter() {
        harness.addToBattlefield(player1, new BloodletterOfAclazotz());
        harness.addToBattlefield(player2, new CityOfShadows());
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new Cleansing(), "{W}{W}{W}");
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player2, "City of Shadows");
    }

    /** Gives {@code mastery} one face-down shield owned by {@code controller}. */
    private Card addShield(Player controller, Permanent mastery) {
        Card shield = new GrizzlyBears();
        gd.exiledCards.add(new ExiledCardEntry(shield, controller.getId(), mastery.getId(), true, gd.turnNumber));
        return shield;
    }

    private void assertMasteryAskedLibraryOrKalitas(Player controller) {
        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(controller.getId());
        assertThat(choice.options())
                .containsExactlyInAnyOrder("Put it on the bottom of its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(controller, "Lich's Duel Mastery");
    }

    private void answerLibrary(Player controller, Permanent mastery) {
        harness.handleListChoice(controller, "Put it on the bottom of its owner's library");

        harness.assertNotOnBattlefield(controller, "Lich's Duel Mastery");
        assertThat(gd.playerDecks.get(controller.getId())).contains(mastery.getCard());
    }

    /**
     * {@code controller} controls a Mastery without shields that Opalescence makes a creature and that Wheel of Sun
     * and Moon's enchanted player owns; the other player controls Kalitas. Sacrificing the Mastery then needs its
     * controller's choice between the library and Kalitas's exile (CR 616.1), while other cards put into the
     * controller's graveyard have the Wheel's replacement alone.
     */
    private Permanent animatedShieldlessMasteryAgainstKalitas(Player controller) {
        Player other = controller == player1 ? player2 : player1;
        harness.setLife(controller, 20);
        Permanent mastery = harness.addToBattlefieldAndReturn(controller, new LichsDuelMastery());
        harness.addToBattlefield(controller, new Opalescence());
        harness.addToBattlefieldAndReturn(controller, new WheelOfSunAndMoon()).setAttachedTo(controller.getId());
        harness.addToBattlefield(other, new KalitasTraitorOfGhet());
        return mastery;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private boolean controls(Player player, UUID permanentId) {
        return gd.playerBattlefields.get(player.getId()).stream().anyMatch(p -> p.getId().equals(permanentId));
    }

    private boolean inExile(String cardName) {
        return gd.exiledCards.stream().anyMatch(entry -> entry.card().getName().equals(cardName));
    }

    private void assertMasteryAsked(Player controller) {
        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(controller.getId());
        assertThat(choice.options())
                .containsExactlyInAnyOrder("Put it on the bottom of its owner's library", "Exile it");
        harness.assertOnBattlefield(controller, "Lich's Duel Mastery");
    }

    private void answerExile(Player controller, Permanent mastery) {
        harness.handleListChoice(controller, "Exile it");

        harness.assertNotOnBattlefield(controller, "Lich's Duel Mastery");
        assertThat(gd.getPlayerExiledCards(controller.getId())).contains(mastery.getCard());
    }

    /**
     * {@code controller} controls a Mastery without shields and is enchanted by Wheel of Sun and Moon; the other
     * player controls Leyline of the Void. Sacrificing the Mastery then needs its controller's choice between the
     * library and the exile (CR 616.1).
     */
    private Permanent shieldlessMasteryBetweenWheelAndLeyline(Player controller) {
        Player other = controller == player1 ? player2 : player1;
        harness.setLife(controller, 20);
        Permanent mastery = harness.addToBattlefieldAndReturn(controller, new LichsDuelMastery());
        harness.addToBattlefieldAndReturn(controller, new WheelOfSunAndMoon()).setAttachedTo(controller.getId());
        harness.addToBattlefield(other, new LeylineOfTheVoid());
        return mastery;
    }
}
