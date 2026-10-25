package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AnrakyrTheTraveller;
import com.github.laxika.magicalvibes.cards.b.BitterTriumph;
import com.github.laxika.magicalvibes.cards.b.BolassCitadel;
import com.github.laxika.magicalvibes.cards.c.Channel;
import com.github.laxika.magicalvibes.cards.c.CleavingReaper;
import com.github.laxika.magicalvibes.cards.d.DeepAnalysis;
import com.github.laxika.magicalvibes.cards.g.Greed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeatWave;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.m.MoltensteelDragon;
import com.github.laxika.magicalvibes.cards.m.MutagenicGrowth;
import com.github.laxika.magicalvibes.cards.n.NornsAnnex;
import com.github.laxika.magicalvibes.cards.p.PantherWarriors;
import com.github.laxika.magicalvibes.cards.r.RavensCrime;
import com.github.laxika.magicalvibes.cards.s.ShadowgrangeArchfiend;
import com.github.laxika.magicalvibes.cards.s.SnuffOut;
import com.github.laxika.magicalvibes.cards.s.StreetWraith;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.t.TerrorOfThePeaks;
import com.github.laxika.magicalvibes.cards.t.ThranPortal;
import com.github.laxika.magicalvibes.cards.t.ToxicDeluge;
import com.github.laxika.magicalvibes.cards.w.WheelOfSunAndMoon;
import com.github.laxika.magicalvibes.cards.z.ZombieCutthroat;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FailOnLegacyDepartureFallback;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lich's Duel Mastery replaces a life payment of its controller (paying life is losing it, CR 119.4) with its
 * sacrifice once no shield is left; that sacrifice's competing graveyard replacements are chosen (CR 616.1) before
 * any cost of the spell, ability or declaration paying the life is paid (CR 601.2h).
 */
@CardUsed({LichsDuelMastery.class, WheelOfSunAndMoon.class, LeylineOfTheVoid.class, GrizzlyBears.class})
class LichsDuelMasteryLifeCostTest extends BaseCardTest {

    @Test
    @CardUsed(Greed.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("\"Pay 2 life\" in an activation cost asks the Mastery's sacrifice before the activation completes")
    void activatedAbilityLifeCostAsksTheMasteryBeforeTheActivationCompletes() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        Permanent greed = harness.addToBattlefieldAndReturn(player2, new Greed());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        player2MainPhase();

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(greed), null, null);

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        harness.handleListChoice(player2, "Exile it");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(mastery.getCard());
        harness.assertLife(player2, 20);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == greed.getCard());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed(ToxicDeluge.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("\"Pay X life\" as an additional cast cost asks the Mastery's sacrifice before any cost is paid")
    void spellAdditionalLifeCostAsksTheMasteryBeforeAnyCostIsPaid() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.setHand(player2, List.of(new ToxicDeluge()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        player2MainPhase();

        harness.castSorcery(player2, 0, 2);

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(3);

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getId()).isEqualTo(mastery.getCard().getId());
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() instanceof ToxicDeluge && entry.getXValue() == 2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({SnuffOut.class, Swamp.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("An alternative cost of 4 life asks the Mastery's sacrifice before the spell is cast")
    void alternativeLifeCostAsksTheMasteryBeforeTheSpellIsCast() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.addToBattlefield(player2, new Swamp());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new SnuffOut()));
        player2MainPhase();

        harness.castInstantWithAlternateCost(player2, 0, bears.getId(), List.of());

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player2, "Snuff Out");

        harness.handleListChoice(player2, "Exile it");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(mastery.getCard());
        harness.assertLife(player2, 20);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() instanceof SnuffOut);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed(CleavingReaper.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("A graveyard ability's life cost asks the Mastery's sacrifice before the activation completes")
    void graveyardAbilityLifeCostAsksTheMasteryBeforeTheActivationCompletes() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        Card reaper = new CleavingReaper();
        harness.setGraveyard(player2, List.of(reaper));
        player2MainPhase();
        harness.enterBattlefieldAndReturn(player2, new CleavingReaper());

        harness.activateGraveyardAbility(player2, 0);

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();

        harness.handleListChoice(player2, "Exile it");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(mastery.getCard());
        harness.assertLife(player2, 20);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == reaper);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed(StreetWraith.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("Cycling paid with life asks the Mastery's sacrifice before the card is discarded")
    void cyclingLifeCostAsksTheMasteryBeforeTheCardIsDiscarded() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.setHand(player2, List.of(new StreetWraith()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        player2MainPhase();

        harness.activateHandAbility(player2, 0, null);

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        harness.assertInHand(player2, "Street Wraith");
        assertThat(gd.stack).isEmpty();

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        // Wheel of Sun and Moon also puts the discarded Street Wraith on the bottom of its owner's library.
        assertThat(gd.playerDecks.get(player2.getId()).stream().map(Card::getName).toList())
                .containsExactly("Grizzly Bears", "Lich's Duel Mastery", "Street Wraith");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertLife(player2, 20);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() instanceof StreetWraith);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed(NornsAnnex.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("Norn's Annex's attack tax paid with life asks the Mastery's sacrifice before anything is declared")
    void attackLifeCostAsksTheMasteryBeforeAnythingIsDeclared() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.addToBattlefield(player1, new NornsAnnex());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(bears)));

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(bears.isAttacking()).isFalse();
        assertThat(bears.isTapped()).isFalse();
        assertThat(gd.playersDeclaredAttackersThisTurn).doesNotContain(player2.getId());

        harness.handleListChoice(player2, "Exile it");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(mastery.getCard());
        harness.assertLife(player2, 20);
        assertThat(gd.playersDeclaredAttackersThisTurn).contains(player2.getId());
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @CardUsed({HeatWave.class, PantherWarriors.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Heat Wave's block life cost asks the Mastery's sacrifice before anything is declared")
    void blockLifeCostAsksTheMasteryBeforeAnythingIsDeclared() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.addToBattlefield(player1, new HeatWave());
        Permanent attacker = addCreatureReady(player1, new PantherWarriors());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new PantherWarriors());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(blocker.isBlocking()).isFalse();

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getId()).isEqualTo(mastery.getCard().getId());
        harness.assertLife(player2, 20);
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @CardUsed(MutagenicGrowth.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("Phyrexian mana paid with life automatically asks the Mastery's sacrifice before the spell is cast")
    void automaticPhyrexianLifePaymentAsksTheMasteryBeforeTheSpellIsCast() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new MutagenicGrowth()));
        player2MainPhase();

        harness.castInstant(player2, 0, bears.getId());

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player2, "Mutagenic Growth");

        harness.handleListChoice(player2, "Exile it");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(mastery.getCard());
        harness.assertLife(player2, 20);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() instanceof MutagenicGrowth);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed(MoltensteelDragon.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("An activation's Phyrexian mana paid with life asks the Mastery's sacrifice before anything is paid")
    void activationPhyrexianLifePaymentAsksTheMasteryBeforeAnythingIsPaid() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        Permanent dragon = addCreatureReady(player2, new MoltensteelDragon());
        player2MainPhase();

        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(dragon), null, null);

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getId()).isEqualTo(mastery.getCard().getId());
        harness.assertLife(player2, 20);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == dragon.getCard());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({MutagenicGrowth.class, TerrorOfThePeaks.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("With one shield left, a cast's second life payment asks the Mastery's sacrifice before any is paid")
    void secondLifePaymentAfterTheLastShieldAsksTheMasteryBeforeAnyIsPaid() {
        Permanent mastery = masteryWithOneShieldBetweenWheelAndLeyline();
        Permanent terror = addCreatureReady(player1, new TerrorOfThePeaks());
        harness.setHand(player2, List.of(new MutagenicGrowth()));
        player2MainPhase();

        // Mutagenic Growth's {G/P} paid with 2 life, then Terror of the Peaks' 3 more life to target it.
        harness.castInstant(player2, 0, terror.getId());

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Mutagenic Growth");

        harness.handleListChoice(player2, "Exile it");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(mastery.getCard());
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() instanceof MutagenicGrowth);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed(Channel.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("Channel's 1 life for {C} asks the Mastery's sacrifice before the life is paid or the mana added")
    void channelLifePaymentAsksTheMasteryBeforeTheManaIsAdded() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        player2MainPhase();
        gd.mayPayLifeForColorlessManaUntilEndOfTurn.add(player2.getId());

        harness.payLifeForColorlessMana(player2);

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.handleListChoice(player2, "Exile it");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(mastery.getCard());
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed(ThranPortal.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("A land's added life cost to tap for mana asks the Mastery's sacrifice before the land is tapped")
    void tapForManaLifeCostAsksTheMasteryBeforeTheLandIsTapped() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        Permanent portal = harness.addToBattlefieldAndReturn(player2, new ThranPortal());
        portal.setChosenSubtype(CardSubtype.SWAMP);
        portal.setSummoningSick(false);
        player2MainPhase();

        harness.tapPermanent(player2, gd.playerBattlefields.get(player2.getId()).indexOf(portal));

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(portal.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isZero();

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getId()).isEqualTo(mastery.getCard().getId());
        harness.assertLife(player2, 20);
        assertThat(portal.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({ShadowgrangeArchfiend.class, RavensCrime.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("A madness cost's 8 life asks the Mastery's sacrifice before the mana is spent")
    void madnessLifeCostAsksTheMasteryBeforeTheManaIsSpent() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        ShadowgrangeArchfiend archfiend = new ShadowgrangeArchfiend();
        harness.setHand(player2, List.of(archfiend));
        harness.setHand(player1, List.of(new RavensCrime()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.handleMayAbilityChosen(player2, true);

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(3);
        assertThat(gd.findExiledCard(archfiend.getId())).isNotNull();

        harness.handleListChoice(player2, "Exile it");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(mastery.getCard());
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == archfiend);
    }

    @Test
    @CardUsed({AnrakyrTheTraveller.class, MindStone.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Casting an artifact by paying life equal to its mana value asks the Mastery's sacrifice first")
    void lifeEqualToManaValueCastAsksTheMasteryBeforeTheCast() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        MindStone mindStone = new MindStone();
        harness.setHand(player2, List.of(mindStone));
        Permanent anrakyr = addCreatureReady(player2, new AnrakyrTheTraveller());

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(anrakyr)));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).contains(mindStone);

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getId()).isEqualTo(mastery.getCard().getId());
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(mindStone);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == mindStone);
    }

    @Test
    @CardUsed(DeepAnalysis.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("A flashback cost's 3 life asks the Mastery's sacrifice before the spell leaves the graveyard")
    void flashbackLifeCostAsksTheMasteryBeforeAnyCostIsPaid() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        DeepAnalysis analysis = new DeepAnalysis();
        harness.setGraveyard(player2, List.of(analysis));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        player2MainPhase();

        harness.castFlashback(player2, 0, player2.getId());

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(analysis);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(2);

        harness.handleListChoice(player2, "Exile it");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(mastery.getCard());
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == analysis);
    }

    @Test
    @CardUsed(BolassCitadel.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("Casting the top card by paying life equal to its mana value asks the Mastery's sacrifice first")
    void libraryTopLifeCostAsksTheMasteryBeforeTheCast() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.addToBattlefield(player2, new BolassCitadel());
        GrizzlyBears bears = new GrizzlyBears();
        gd.playerDecks.get(player2.getId()).addFirst(bears);
        player2MainPhase();

        harness.castFromLibraryTop(player2);

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(bears);

        harness.handleListChoice(player2, "Exile it");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(mastery.getCard());
        harness.assertLife(player2, 20);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == bears);
    }

    @Test
    @CardUsed(LiesaShroudOfDusk.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("A commander tax paid with life asks the Mastery's sacrifice before the commander leaves the zone")
    void commanderTaxPaidWithLifeAsksTheMasteryBeforeTheCast() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        LiesaShroudOfDusk commander = new LiesaShroudOfDusk();
        gd.format = DeckFormat.COMMANDER;
        gd.makeCommander(player2.getId(), commander);
        gd.playerCommandZones.put(player2.getId(), new ArrayList<>(List.of(commander)));
        gd.commanderTaxByCardId.put(commander.getId(), 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);
        player2MainPhase();
        harness.ensurePriority(player2);

        gs.castCommander(gd, player2, commander.getId(), () -> gs.playCard(gd, player2, 0, null, null, null));

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(5);
        assertThat(gd.stack).isEmpty();

        harness.handleListChoice(player2, "Exile it");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(mastery.getCard());
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getId().equals(commander.getId()));
    }

    @Test
    @CardUsed(ZombieCutthroat.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("A morph cost of 5 life asks the Mastery's sacrifice before the permanent is turned face up")
    void morphLifeCostAsksTheMasteryBeforeTurningFaceUp() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        player2MainPhase();
        harness.setHand(player2, List.of(new ZombieCutthroat()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent cutthroat = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(Permanent::isFaceDown).findFirst().orElseThrow();

        harness.turnFaceUp(player2, gd.playerBattlefields.get(player2.getId()).indexOf(cutthroat));

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(cutthroat.isFaceDown()).isTrue();

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getId()).isEqualTo(mastery.getCard().getId());
        harness.assertLife(player2, 20);
        assertThat(cutthroat.isFaceDown()).isFalse();
    }

    @Test
    @CardUsed(BitterTriumph.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("\"Discard a card or pay 3 life\" paid with life asks the Mastery's sacrifice before any cost is paid")
    void discardOrPayLifeCostPaidWithLifeAsksTheMasteryBeforeAnyCostIsPaid() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new BitterTriumph()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        player2MainPhase();

        harness.castInstant(player2, 0, bears.getId());

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(2);

        harness.handleListChoice(player2, "Exile it");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(mastery.getCard());
        harness.assertLife(player2, 20);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard() instanceof BitterTriumph);
    }

    private void player2MainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void assertMasteryAskedLibraryOrExile() {
        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.options())
                .containsExactlyInAnyOrder("Put it on the bottom of its owner's library", "Exile it");
        harness.assertOnBattlefield(player2, "Lich's Duel Mastery");
    }

    /**
     * Player 2 controls a noncreature Mastery without shields that Wheel of Sun and Moon's enchanted player owns;
     * player 1 controls Leyline of the Void. Sacrificing the Mastery then needs its controller's choice between the
     * library and the exile (CR 616.1).
     */
    /**
     * As {@link #noncreatureShieldlessMasteryBetweenWheelAndLeyline}, but player 2 casts the Mastery with a one-card
     * library, so it has exactly one shield: the first life payment puts that shield into player 2's hand and the
     * next one sacrifices the Mastery.
     */
    private Permanent masteryWithOneShieldBetweenWheelAndLeyline() {
        harness.setLife(player2, 20);
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new LichsDuelMastery(), "{3}{B}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent mastery = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof LichsDuelMastery)
                .findFirst().orElseThrow();
        assertThat(gd.getCardsExiledByPermanent(mastery.getId())).hasSize(1);
        harness.addToBattlefieldAndReturn(player2, new WheelOfSunAndMoon()).setAttachedTo(player2.getId());
        harness.addToBattlefield(player1, new LeylineOfTheVoid());
        return mastery;
    }

    private Permanent noncreatureShieldlessMasteryBetweenWheelAndLeyline() {
        harness.setLife(player2, 20);
        Permanent mastery = harness.addToBattlefieldAndReturn(player2, new LichsDuelMastery());
        harness.addToBattlefieldAndReturn(player2, new WheelOfSunAndMoon()).setAttachedTo(player2.getId());
        harness.addToBattlefield(player1, new LeylineOfTheVoid());
        return mastery;
    }
}
