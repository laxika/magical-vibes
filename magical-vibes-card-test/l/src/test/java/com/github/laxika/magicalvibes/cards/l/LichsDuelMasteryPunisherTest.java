package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.ArchfiendOfSpite;
import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.b.BellowingMauler;
import com.github.laxika.magicalvibes.cards.c.Carnophage;
import com.github.laxika.magicalvibes.cards.d.DwarvenTrader;
import com.github.laxika.magicalvibes.cards.e.EmberwildeDjinn;
import com.github.laxika.magicalvibes.cards.e.EntropicBattlecruiser;
import com.github.laxika.magicalvibes.cards.e.Erosion;
import com.github.laxika.magicalvibes.cards.e.EssenceVortex;
import com.github.laxika.magicalvibes.cards.f.FallenAskari;
import com.github.laxika.magicalvibes.cards.f.ForbiddenRitual;
import com.github.laxika.magicalvibes.cards.g.GenesisOfTheDaleks;
import com.github.laxika.magicalvibes.cards.g.GiantAlbatross;
import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.cards.g.GraveConsequences;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IndulgentTormentor;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KroxaTitanOfDeathsHunger;
import com.github.laxika.magicalvibes.cards.m.MemoryLapse;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.m.Mundungu;
import com.github.laxika.magicalvibes.cards.o.ObNixilisTheAdversary;
import com.github.laxika.magicalvibes.cards.o.Oasis;
import com.github.laxika.magicalvibes.cards.p.PhyrexianProcessor;
import com.github.laxika.magicalvibes.cards.p.PrinceOfThralls;
import com.github.laxika.magicalvibes.cards.p.Python;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Sirocco;
import com.github.laxika.magicalvibes.cards.s.SpoilsOfTheVault;
import com.github.laxika.magicalvibes.cards.s.StrongarmTactics;
import com.github.laxika.magicalvibes.cards.t.TheDeathOfGwenStacy;
import com.github.laxika.magicalvibes.cards.t.ThisIsHowItEnds;
import com.github.laxika.magicalvibes.cards.t.TormentOfVenom;
import com.github.laxika.magicalvibes.cards.t.Tyrannize;
import com.github.laxika.magicalvibes.cards.u.Umbilicus;
import com.github.laxika.magicalvibes.cards.v.VectisDominator;
import com.github.laxika.magicalvibes.cards.w.WallOfBlood;
import com.github.laxika.magicalvibes.cards.w.WernogRidersChaplain;
import com.github.laxika.magicalvibes.cards.w.WheelOfSunAndMoon;
import com.github.laxika.magicalvibes.cards.w.WirewoodElf;
import com.github.laxika.magicalvibes.cards.w.WrenchMind;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCreatureThenOwnerFacesVillainousChoiceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetOpponentFacesGenesisVillainousChoiceEffect;
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
 * The "lose life unless" and "or lose life" punishers: each asks player 2's noncreature, shieldless Lich's Duel
 * Mastery, between Wheel of Sun and Moon and Leyline of the Void, before any life is lost (CR 616.1), and changes
 * nothing of the game twice.
 */
@CardUsed({LichsDuelMastery.class, WheelOfSunAndMoon.class, LeylineOfTheVoid.class})
class LichsDuelMasteryPunisherTest extends BaseCardTest {

    @Test
    @CardUsed(EntropicBattlecruiser.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("Entropic Battlecruiser's loss of an opponent who can't discard asks the Mastery before any life is lost")
    void cantDiscardLossAsksTheMasteryBeforeAnyLifeIsLost() {
        Permanent battlecruiser = addCreatureReady(player1, new EntropicBattlecruiser());
        battlecruiser.setCounterCount(CounterType.CHARGE, 8);
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.setHand(player2, List.of());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Exile it");

        assertMasteryExiled(mastery);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({LilianaWakerOfTheDead.class, Shock.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Liliana's loss after the controller's discard choice asks the Mastery before any life is lost")
    void lossAfterADiscardChoiceAsksTheMasteryBeforeAnyLifeIsLost() {
        Permanent liliana = harness.addToBattlefieldAndReturn(player1, new LilianaWakerOfTheDead());
        liliana.setCounterCount(CounterType.LOYALTY, 3);
        liliana.setSummoningSick(false);
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        Card discarded = new Shock();
        harness.setHand(player1, List.of(discarded));
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded);

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        assertMasteryOnLibraryBottom(mastery);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({TheDeathOfGwenStacy.class, GrizzlyBears.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("The Death of Gwen Stacy's loss after the discards asks the Mastery before any life is lost")
    void lossAfterTheDiscardsAsksTheMasteryBeforeAnyLifeIsLost() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheDeathOfGwenStacy());
        saga.setCounterCount(CounterType.LORE, 1);
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        Card discarded = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsOnlyOnce(discarded);

        harness.handleListChoice(player2, "Exile it");

        assertMasteryExiled(mastery);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsOnlyOnce(discarded);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed(ObNixilisTheAdversary.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("Ob Nixilis's loss of an opponent with no card asks the Mastery before any life is lost")
    void noCardToDiscardAsksTheMasteryBeforeAnyLifeIsLost() {
        Permanent obNixilis = harness.addToBattlefieldAndReturn(player1, new ObNixilisTheAdversary());
        obNixilis.setCounterCount(CounterType.LOYALTY, 3);
        obNixilis.setSummoningSick(false);
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        assertMasteryOnLibraryBottom(mastery);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({StrongarmTactics.class, WirewoodElf.class, Mountain.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Strongarm Tactics's loss after the discards asks the Mastery before any life is lost")
    void noncreatureDiscardLossAsksTheMasteryBeforeAnyLifeIsLost() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        Card elf = new WirewoodElf();
        Card mountain = new Mountain();
        harness.setHand(player1, new ArrayList<>(List.of(new StrongarmTactics(), elf)));
        harness.setHand(player2, new ArrayList<>(List.of(mountain)));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player2, 0);

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsOnlyOnce(elf);

        harness.handleListChoice(player2, "Exile it");

        assertMasteryExiled(mastery);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsOnlyOnce(elf);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({KroxaTitanOfDeathsHunger.class, Island.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Kroxa's loss after a land discard asks the Mastery before any life is lost")
    void landDiscardLossAsksTheMasteryBeforeAnyLifeIsLost() {
        addCreatureReady(player1, new KroxaTitanOfDeathsHunger());
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.setHand(player2, new ArrayList<>(List.of(new Island())));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        assertMasteryOnLibraryBottom(mastery);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({GraveConsequences.class, GrizzlyBears.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Grave Consequences's loss after the exile asks the Mastery before any life is lost or card drawn")
    void lossAfterTheGraveyardExileAsksTheMasteryBeforeAnyLifeIsLost() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        Card drawn = new GrizzlyBears();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(drawn));
        harness.castFromHand(player1, new GraveConsequences(), "{1}{B}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player2, List.of());

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawn);

        harness.handleListChoice(player2, "Exile it");

        assertMasteryExiled(mastery);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).containsOnlyOnce(drawn);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({TormentOfVenom.class, AirElemental.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Choosing Torment of Venom's life loss asks the Mastery before any life is lost")
    void chosenTormentLossAsksTheMasteryBeforeAnyLifeIsLost() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new TormentOfVenom()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.handleListChoice(player2, "Lose 3 life");

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        assertMasteryOnLibraryBottom(mastery);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Wheel of Sun and Moon");
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({ArchfiendOfSpite.class, Shock.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Choosing Archfiend of Spite's life loss asks the Mastery before any life is lost")
    void chosenArchfiendLossAsksTheMasteryBeforeAnyLifeIsLost() {
        harness.addToBattlefield(player1, new ArchfiendOfSpite());
        UUID archfiendId = harness.getPermanentId(player1, "Archfiend of Spite");
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, archfiendId);
        harness.passBothPriorities();

        harness.handleListChoice(player2, "Lose 2 life");

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Exile it");

        assertMasteryExiled(mastery);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Wheel of Sun and Moon");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({ArchfiendOfSpite.class, LightningStrike.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Archfiend of Spite's loss of a player without enough permanents asks the Mastery before any life is lost")
    void tooFewPermanentsArchfiendLossAsksTheMasteryBeforeAnyLifeIsLost() {
        harness.addToBattlefield(player1, new ArchfiendOfSpite());
        UUID archfiendId = harness.getPermanentId(player1, "Archfiend of Spite");
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, archfiendId);
        harness.passBothPriorities();

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        assertMasteryOnLibraryBottom(mastery);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed(BellowingMauler.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("Bellowing Mauler's loss after the sacrifices asks the Mastery before any life is lost")
    void lossAfterTheSacrificesAsksTheMasteryBeforeAnyLifeIsLost() {
        Permanent mauler = harness.addToBattlefieldAndReturn(player1, new BellowingMauler());
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.handleListChoice(player1, "Sacrifice a nontoken creature");

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mauler);

        harness.handleListChoice(player2, "Exile it");

        assertMasteryExiled(mastery);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsOnlyOnce(mauler.getCard());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed(IndulgentTormentor.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("Paying Indulgent Tormentor's life asks the Mastery before any life is paid")
    void lifePaymentAsksTheMasteryBeforeAnyLifeIsPaid() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCreatureReady(player1, new IndulgentTormentor());
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player2, ChoiceContext.IndulgentTormentorChoice.payLife(3));

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Exile it");

        assertMasteryExiled(mastery);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed(WernogRidersChaplain.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("Declining Wernog's investigate asks the Mastery before any life is lost or Clue made")
    void declinedInvestigateAsksTheMasteryBeforeAnyLifeIsLost() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.setHand(player1, List.of(new WernogRidersChaplain()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.handleMayAbilityChosen(player2, false);

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(countPermanents(player1, "Clue")).isZero();

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        assertMasteryOnLibraryBottom(mastery);
        harness.assertLife(player2, 20);
        assertThat(countPermanents(player1, "Clue")).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({ThisIsHowItEnds.class, GrizzlyBears.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("This Is How It Ends's chosen life loss asks the Mastery before any life is lost")
    void villainousLifeLossAsksTheMasteryBeforeAnyLifeIsLost() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new ThisIsHowItEnds()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.handleListChoice(player2, ShuffleTargetCreatureThenOwnerFacesVillainousChoiceEffect.LOSE_LIFE_OPTION);

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard());

        harness.handleListChoice(player2, "Exile it");

        assertMasteryExiled(mastery);
        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed(GenesisOfTheDaleks.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("Genesis of the Daleks's loss after the Daleks die asks the Mastery before any life is lost")
    void lossAfterTheDaleksDieAsksTheMasteryBeforeAnyLifeIsLost() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new GenesisOfTheDaleks());
        advanceToNextChapter();
        harness.passBothPriorities();
        Permanent dalek = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Dalek"))
                .findFirst().orElseThrow();
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        saga.setCounterCount(CounterType.LORE, 3);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, TargetOpponentFacesGenesisVillainousChoiceEffect.DESTROY_DALEKS);

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dalek);

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        assertMasteryOnLibraryBottom(mastery);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({ForbiddenRitual.class, Python.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Forbidden Ritual's chosen life loss asks the Mastery after the sacrifice and before any life is lost")
    void chosenRitualLossAsksTheMasteryBeforeAnyLifeIsLost() {
        Permanent python = harness.addToBattlefieldAndReturn(player1, new Python());
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new ForbiddenRitual()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, python.getId());

        harness.handleListChoice(player2, "Lose 2 life");

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsOnlyOnce(python.getCard());

        harness.handleListChoice(player2, "Exile it");

        assertMasteryExiled(mastery);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ForbiddenRitualRepeatChoice.class))
                .isNotNull();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Leyline of the Void");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({Mundungu.class, FallenAskari.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Paying Mundungu's mana and life asks the Mastery before any mana or life is paid")
    void counterUnlessPaysLifeAsksTheMasteryBeforeAnythingIsPaid() {
        addCreatureReady(player1, new Mundungu());
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.forceActivePlayer(player2);
        FallenAskari askari = new FallenAskari();
        harness.setHand(player2, List.of(askari));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, askari.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(1);

        harness.handleListChoice(player2, "Exile it");

        assertMasteryExiled(mastery);
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isZero();
        harness.assertNotInGraveyard(player2, "Fallen Askari");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({Tyrannize.class, GrizzlyBears.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Paying Tyrannize's life asks the Mastery before any life is paid")
    void discardHandUnlessPaysLifeAsksTheMasteryBeforeAnyLifeIsPaid() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Tyrannize()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.handleMayAbilityChosen(player2, true);

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        assertMasteryOnLibraryBottom(mastery);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({Sirocco.class, MemoryLapse.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Paying Sirocco's life asks the Mastery before any life is paid")
    void revealedCardLifePaymentAsksTheMasteryBeforeAnyLifeIsPaid() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        Card kept = new MemoryLapse();
        harness.setHand(player2, new ArrayList<>(List.of(kept)));
        harness.setHand(player1, List.of(new Sirocco()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.handleMayAbilityChosen(player2, true);

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Exile it");

        assertMasteryExiled(mastery);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(kept);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({GiantAlbatross.class, DwarvenTrader.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Paying Giant Albatross's life asks the Mastery before any life is paid")
    void damagingCreatureLifePaymentAsksTheMasteryBeforeAnyLifeIsPaid() {
        Permanent albatross = addCreatureReady(player1, new GiantAlbatross());
        albatross.setBlocking(true);
        albatross.addBlockingTarget(0);
        Card trader = new DwarvenTrader();
        trader.setPower(5);
        trader.setToughness(5);
        Permanent attacker = addCreatureReady(player2, trader);
        attacker.setAttacking(true);
        resolveCombat(player2);
        harness.passBothPriorities();
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.handleMayAbilityChosen(player2, true);

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        assertMasteryOnLibraryBottom(mastery);
        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({PrinceOfThralls.class, GrizzlyBears.class, Shock.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Paying Prince of Thralls's life asks the Mastery before any life is paid")
    void stealUnlessPaysLifeAsksTheMasteryBeforeAnyLifeIsPaid() {
        harness.addToBattlefield(player1, new PrinceOfThralls());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();

        harness.handleMayAbilityChosen(player2, true);

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Exile it");

        assertMasteryExiled(mastery);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed(VectisDominator.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("Paying Vectis Dominator's life asks the Mastery before any life is paid")
    void tapUnlessPaysLifeAsksTheMasteryBeforeAnyLifeIsPaid() {
        addCreatureReady(player1, new VectisDominator());
        Permanent target = addCreatureReady(player2, new VectisDominator());
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        assertMasteryOnLibraryBottom(mastery);
        harness.assertLife(player2, 20);
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({EssenceVortex.class, BalduvianBears.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Paying Essence Vortex's life asks the Mastery before any life is paid")
    void destroyUnlessPaysToughnessLifeAsksTheMasteryBeforeAnyLifeIsPaid() {
        Permanent target = addCreatureReady(player2, new BalduvianBears());
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.setHand(player1, List.of(new EssenceVortex()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.handleMayAbilityChosen(player2, true);

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Exile it");

        assertMasteryExiled(mastery);
        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({Umbilicus.class, GorillaWarrior.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Paying Umbilicus's life asks the Mastery before any life is paid")
    void forcedLifeCostAsksTheMasteryBeforeAnyLifeIsPaid() {
        harness.addToBattlefield(player1, new Umbilicus());
        harness.addToBattlefield(player2, new GorillaWarrior());
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        assertMasteryOnLibraryBottom(mastery);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Gorilla Warrior");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed(Carnophage.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("Paying Carnophage's upkeep life asks the Mastery before any life is paid")
    void forcedManaCostWithLifeAsksTheMasteryBeforeAnyLifeIsPaid() {
        Permanent carnophage = harness.addToBattlefieldAndReturn(player2, new Carnophage());
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Exile it");

        assertMasteryExiled(mastery);
        harness.assertLife(player2, 20);
        assertThat(carnophage.isTapped()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed(EmberwildeDjinn.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("Choosing to pay Emberwilde Djinn's life asks the Mastery before any life is paid")
    void manaOrLifeChoiceLifePaymentAsksTheMasteryBeforeAnyLifeIsPaid() {
        UUID djinnId = harness.addToBattlefieldAndReturn(player1, new EmberwildeDjinn()).getId();
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.RED, 2);
        harness.passBothPriorities();

        harness.handleListChoice(player2, "Pay 2 life");

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(djinnId));

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        assertMasteryOnLibraryBottom(mastery);
        harness.assertLife(player2, 20);
        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(djinnId));
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({Erosion.class, Oasis.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Choosing to pay Erosion's life asks the Mastery before any life is paid")
    void enchantedLandLifeChoiceAsksTheMasteryBeforeAnyLifeIsPaid() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Oasis());
        harness.addToBattlefieldAndReturn(player1, new Erosion()).setAttachedTo(land.getId());
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.passBothPriorities();

        harness.handleListChoice(player2, "Pay 1 life");

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Exile it");

        assertMasteryExiled(mastery);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Oasis");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({Erosion.class, Oasis.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Accepting Erosion's life payment asks the Mastery before any life is paid")
    void enchantedLandMayLifePaymentAsksTheMasteryBeforeAnyLifeIsPaid() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Oasis());
        harness.addToBattlefieldAndReturn(player1, new Erosion()).setAttachedTo(land.getId());
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        assertMasteryOnLibraryBottom(mastery);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Oasis");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({SpoilsOfTheVault.class, WallOfBlood.class, WrenchMind.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Spoils of the Vault's loss after the exile asks the Mastery before any life is lost")
    void lossAfterRevealUntilNamedAsksTheMasteryBeforeAnyLifeIsLost() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        Card miss = new WallOfBlood();
        Card hit = new WrenchMind();
        harness.setLibrary(player2, List.of(miss, hit));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new SpoilsOfTheVault(), "{B}");
        harness.passBothPriorities();

        harness.handleListChoice(player2, "Wrench Mind");

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(miss);
        assertThat(gd.playerHands.get(player2.getId())).containsOnlyOnce(hit);

        harness.handleListChoice(player2, "Exile it");

        assertMasteryExiled(mastery);
        harness.assertLife(player2, 20);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsOnlyOnce(miss);
        assertThat(gd.playerHands.get(player2.getId())).containsOnlyOnce(hit);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed(PhyrexianProcessor.class)
    @FailOnLegacyDepartureFallback
    @DisplayName("Paying life as Phyrexian Processor enters asks the Mastery before any life is paid")
    void lifePaidAsItEntersAsksTheMasteryBeforeAnyLifeIsPaid() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new PhyrexianProcessor(), "{4}");
        harness.passBothPriorities();

        harness.handleListChoice(player2, "5");

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        assertMasteryOnLibraryBottom(mastery);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Phyrexian Processor");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
    }

    private void assertMasteryAskedLibraryOrExile() {
        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.options())
                .containsExactlyInAnyOrder("Put it on the bottom of its owner's library", "Exile it");
        harness.assertOnBattlefield(player2, "Lich's Duel Mastery");
    }

    private void assertMasteryExiled(Permanent mastery) {
        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(mastery.getCard());
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
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent mastery = harness.addToBattlefieldAndReturn(player2, new LichsDuelMastery());
        harness.addToBattlefieldAndReturn(player2, new WheelOfSunAndMoon()).setAttachedTo(player2.getId());
        harness.addToBattlefield(player1, new LeylineOfTheVoid());
        return mastery;
    }
}
