package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.b.Biorhythm;
import com.github.laxika.magicalvibes.cards.b.BloodTyrant;
import com.github.laxika.magicalvibes.cards.b.BraidsArisenNightmare;
import com.github.laxika.magicalvibes.cards.b.BumpInTheNight;
import com.github.laxika.magicalvibes.cards.c.CarapaceForger;
import com.github.laxika.magicalvibes.cards.d.DarkConfidant;
import com.github.laxika.magicalvibes.cards.d.DeadlyTempest;
import com.github.laxika.magicalvibes.cards.d.DireTactics;
import com.github.laxika.magicalvibes.cards.d.DuskmantleSeer;
import com.github.laxika.magicalvibes.cards.f.FeedTheSwarm;
import com.github.laxika.magicalvibes.cards.f.FandanielTelophoroiAscian;
import com.github.laxika.magicalvibes.cards.f.FeralMaaka;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HideousEnd;
import com.github.laxika.magicalvibes.cards.k.KalitasTraitorOfGhet;
import com.github.laxika.magicalvibes.cards.m.MornsongAria;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.o.OrzhovGuildmage;
import com.github.laxika.magicalvibes.cards.p.PainfulQuandary;
import com.github.laxika.magicalvibes.cards.p.PalantirOfOrthanc;
import com.github.laxika.magicalvibes.cards.p.PriestOfForgottenGods;
import com.github.laxika.magicalvibes.cards.p.PsychicTransfer;
import com.github.laxika.magicalvibes.cards.r.RainOfDaggers;
import com.github.laxika.magicalvibes.cards.r.RelicOfLegends;
import com.github.laxika.magicalvibes.cards.r.ReverseTheSands;
import com.github.laxika.magicalvibes.cards.r.RhysticSyphon;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SingeMindOgre;
import com.github.laxika.magicalvibes.cards.s.SorinMarkov;
import com.github.laxika.magicalvibes.cards.s.SoulConduit;
import com.github.laxika.magicalvibes.cards.s.StabWound;
import com.github.laxika.magicalvibes.cards.t.TreeOfPerdition;
import com.github.laxika.magicalvibes.cards.w.WheelOfSunAndMoon;
import com.github.laxika.magicalvibes.cards.w.WillOfTheAbzan;
import com.github.laxika.magicalvibes.cards.x.XathridDemon;
import com.github.laxika.magicalvibes.cards.y.ThoughtStringAnalyst;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FailOnLegacyDepartureFallback;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LichsDuelMastery.class, GrizzlyBears.class, Shock.class})
class LichsDuelMasteryTest extends BaseCardTest {

    private UUID castMastery(int librarySize) {
        harness.setLibrary(player1, java.util.stream.Stream.generate(GrizzlyBears::new)
                .limit(librarySize)
                .map(Card.class::cast)
                .toList());
        harness.castFromHand(player1, new LichsDuelMastery(), "{3}{B}{B}{B}");
        harness.passBothPriorities(); // resolve the enchantment
        harness.passBothPriorities(); // resolve its ETB trigger
        return harness.getPermanentId(player1, "Lich's Duel Mastery");
    }

    @Test
    @DisplayName("Enters with the top five cards of its controller's library as face-down shields")
    void entersWithFiveShields() {
        UUID masteryId = castMastery(6);

        assertThat(gd.getCardsExiledByPermanent(masteryId)).hasSize(5);
        assertThat(gd.exiledCards).filteredOn(e -> masteryId.equals(e.sourcePermanentId()))
                .allMatch(ExiledCardEntry::faceDown);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Replaces life loss by returning one shield to its controller's hand")
    void lifeLossReturnsOneShieldToHand() {
        UUID masteryId = castMastery(5);
        List<UUID> shieldIds = gd.getCardsExiledByPermanent(masteryId).stream()
                .map(Card::getId).toList();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getLifeSupport()
                .applyLifeLoss(gd, player1.getId(), 3, "test"));

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsAnyElementsOf(shieldIds);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.getCardsExiledByPermanent(masteryId)).hasSize(4);
    }

    @Test
    @DisplayName("Sacrifices itself when no shields remain")
    void sacrificesWhenNoShieldsRemain() {
        castMastery(1);
        harness.setLife(player1, 20);

        harness.inMutationScope(() -> harness.getLifeSupport()
                .applyLifeLoss(gd, player1.getId(), 1, "first test"));
        harness.inMutationScope(() -> harness.getLifeSupport()
                .applyLifeLoss(gd, player1.getId(), 1, "second test"));

        harness.assertLife(player1, 20);
        harness.assertNotOnBattlefield(player1, "Lich's Duel Mastery");
        harness.assertInGraveyard(player1, "Lich's Duel Mastery");

        harness.passBothPriorities();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Only the controller's life loss is replaced")
    void onlyControllerLifeLossIsReplaced() {
        UUID masteryId = castMastery(5);
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> harness.getLifeSupport()
                .applyLifeLoss(gd, player2.getId(), 3, "opponent test"));

        harness.assertLife(player2, 17);
        assertThat(gd.getCardsExiledByPermanent(masteryId)).hasSize(5);
    }

    @Test
    @DisplayName("Damage replaces its life loss with one shield")
    void damageReturnsOneShieldInsteadOfLosingLife() {
        UUID masteryId = castMastery(5);
        harness.setLife(player1, 20);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 20);
        assertThat(gd.getCardsExiledByPermanent(masteryId)).hasSize(4);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Zero life loss does not consume a shield")
    void zeroLifeLossDoesNotConsumeShield() {
        UUID masteryId = castMastery(5);
        harness.setLife(player1, 20);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getLifeSupport()
                .applyLifeLoss(gd, player1.getId(), 0, "test"));

        harness.assertLife(player1, 20);
        assertThat(gd.getCardsExiledByPermanent(masteryId)).hasSize(5);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Leaving for the hand triggers a loss even with shields remaining")
    void leavingForHandLosesGameWithShieldsRemaining() {
        UUID masteryId = castMastery(5);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, gqs.findPermanentById(gd, masteryId)));

        harness.assertNotOnBattlefield(player1, "Lich's Duel Mastery");
        harness.assertInHand(player1, "Lich's Duel Mastery");
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
        harness.passBothPriorities();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @CardUsed({Opalescence.class, WheelOfSunAndMoon.class, KalitasTraitorOfGhet.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Shock's damage asks library-or-Kalitas for the animated, shieldless Mastery before any life changes")
    void spellDamageAsksTheDeathReplacementBeforeTheSacrifice() {
        Permanent mastery = animatedShieldlessMasteryAgainstKalitas();
        Card shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("Put it on the bottom of its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Lich's Duel Mastery");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).extracting(entry -> entry.getCard().getId()).containsExactly(shock.getId());

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getId()).isEqualTo(mastery.getCard().getId());
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Shock");
        assertThat(zombieTokens()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({Opalescence.class, WheelOfSunAndMoon.class, KalitasTraitorOfGhet.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Combat damage asks library-or-Kalitas for the animated, shieldless Mastery before any life changes")
    void combatDamageAsksTheDeathReplacementBeforeTheSacrifice() {
        Permanent mastery = animatedShieldlessMasteryAgainstKalitas();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        gs.declareBlockers(gd, player2, List.<BlockerAssignment>of());
        resolveCombat();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("Put it on the bottom of its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Lich's Duel Mastery");
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Kalitas, Traitor of Ghet");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.getCardsExiledByPermanent(mastery.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId()).contains(mastery.getCard().getId());
        harness.assertLife(player2, 20);
        assertThat(zombieTokens()).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({BumpInTheNight.class, Opalescence.class, WheelOfSunAndMoon.class, KalitasTraitorOfGhet.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("A spell's life loss asks library-or-Kalitas for the animated, shieldless Mastery before any life changes")
    void spellLifeLossAsksTheDeathReplacementBeforeTheSacrifice() {
        Permanent mastery = animatedShieldlessMasteryAgainstKalitas();
        Card bump = new BumpInTheNight();
        harness.setHand(player1, List.of(bump));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("Put it on the bottom of its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Lich's Duel Mastery");
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getId()).isEqualTo(mastery.getCard().getId());
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Bump in the Night");
        assertThat(zombieTokens(player1)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({OrzhovGuildmage.class, Opalescence.class, WheelOfSunAndMoon.class, KalitasTraitorOfGhet.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("\"Each player loses 1 life\" asks both players' Mastery sacrifices before either player's life changes")
    void eachPlayerLifeLossAsksEveryMasteryBeforeAnyLifeChanges() {
        harness.addToBattlefield(player1, new OrzhovGuildmage());
        Permanent ownMastery = harness.addToBattlefieldAndReturn(player1, new LichsDuelMastery());
        Permanent ownWheel = harness.addToBattlefieldAndReturn(player1, new WheelOfSunAndMoon());
        ownWheel.setAttachedTo(player1.getId());
        harness.addToBattlefield(player2, new KalitasTraitorOfGhet());
        Permanent opponentMastery = animatedShieldlessMasteryAgainstKalitas();
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("Put it on the bottom of its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player1, "Lich's Duel Mastery");
        harness.assertOnBattlefield(player2, "Lich's Duel Mastery");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.handleListChoice(player1, "Put it on the bottom of its owner's library");

        harness.assertOnBattlefield(player1, "Lich's Duel Mastery");
        harness.assertOnBattlefield(player2, "Lich's Duel Mastery");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Kalitas, Traitor of Ghet");

        harness.assertNotOnBattlefield(player1, "Lich's Duel Mastery");
        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.playerDecks.get(player1.getId()).getLast().getId()).isEqualTo(ownMastery.getCard().getId());
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId())
                .contains(opponentMastery.getCard().getId());
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(zombieTokens(player1)).isEqualTo(1);
        assertThat(zombieTokens(player2)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({DarkConfidant.class, Opalescence.class, WheelOfSunAndMoon.class, KalitasTraitorOfGhet.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Dark Confidant's life loss asks library-or-Kalitas after the reveal and before any life changes")
    void revealThenLifeLossAsksTheDeathReplacementBeforeTheSacrifice() {
        Permanent mastery = animatedShieldlessMasteryAgainstKalitas();
        harness.addToBattlefield(player2, new DarkConfidant());
        Card revealed = new GrizzlyBears();
        harness.setLibrary(player2, List.of(revealed));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("Put it on the bottom of its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getId).contains(revealed.getId());
        harness.assertOnBattlefield(player2, "Lich's Duel Mastery");
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.playerDecks.get(player2.getId())).extracting(Card::getId)
                .containsExactly(mastery.getCard().getId());
        assertThat(gd.playerHands.get(player2.getId()).stream().filter(card -> card.getId().equals(revealed.getId())))
                .hasSize(1);
        harness.assertLife(player2, 20);
        assertThat(zombieTokens()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({BloodTyrant.class, Opalescence.class, WheelOfSunAndMoon.class, KalitasTraitorOfGhet.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Blood Tyrant's \"each player loses 1 life\" asks the opponent's Mastery before either player loses life")
    void bloodTyrantAsksTheMasteryBeforeAnyLifeIsLost() {
        Permanent mastery = animatedShieldlessMasteryAgainstKalitas();
        Permanent tyrant = harness.addToBattlefieldAndReturn(player1, new BloodTyrant());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("Put it on the bottom of its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Lich's Duel Mastery");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Kalitas, Traitor of Ghet");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId()).contains(mastery.getCard().getId());
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        assertThat(tyrant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(zombieTokens()).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({DireTactics.class, Opalescence.class, WheelOfSunAndMoon.class, KalitasTraitorOfGhet.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Dire Tactics' life loss asks library-or-Kalitas after the exile and before any life changes")
    void exileThenLifeLossAsksTheDeathReplacementBeforeTheSacrifice() {
        Permanent mastery = animatedShieldlessMasteryAgainstKalitas();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card direTactics = new DireTactics();
        harness.setHand(player2, List.of(direTactics));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player2, 0, bears.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("Put it on the bottom of its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId()).contains(bears.getCard().getId());
        harness.assertOnBattlefield(player2, "Lich's Duel Mastery");
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        // Wheel of Sun and Moon also puts the resolved Dire Tactics on the bottom, after the Mastery.
        assertThat(gd.playerDecks.get(player2.getId())).extracting(Card::getId)
                .containsSubsequence(mastery.getCard().getId(), direTactics.getId());
        assertThat(gd.exiledCards.stream().filter(entry -> entry.card().getId().equals(bears.getCard().getId())))
                .hasSize(1);
        harness.assertLife(player2, 20);
        assertThat(zombieTokens()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({PriestOfForgottenGods.class, FeralMaaka.class, Opalescence.class, WheelOfSunAndMoon.class,
            KalitasTraitorOfGhet.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Priest of Forgotten Gods asks the target's Mastery before the life loss and the sacrifice")
    void loseLifeThenSacrificeAsksTheMasteryBeforeAnyLifeIsLost() {
        addCreatureReady(player1, new PriestOfForgottenGods());
        Permanent firstMaaka = addCreatureReady(player1, new FeralMaaka());
        Permanent secondMaaka = addCreatureReady(player1, new FeralMaaka());
        Permanent mastery = animatedShieldlessMasteryAgainstKalitas();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, firstMaaka.getId());
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, secondMaaka.getId());
        }
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("Put it on the bottom of its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Lich's Duel Mastery");
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Kalitas, Traitor of Ghet");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId()).contains(mastery.getCard().getId());
        harness.assertLife(player2, 20);
        assertThat(zombieTokens()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({WillOfTheAbzan.class, AvatarOfMight.class, Opalescence.class, WheelOfSunAndMoon.class,
            KalitasTraitorOfGhet.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Will of the Abzan asks the target's Mastery after the sacrifice and before any life is lost")
    void sacrificeThenLifeLossAsksTheMasteryBeforeAnyLifeIsLost() {
        Permanent mastery = animatedShieldlessMasteryAgainstKalitas();
        Permanent avatar = addCreatureReady(player2, new AvatarOfMight());
        harness.setHand(player1, List.of(new WillOfTheAbzan()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0}, List.of(player2.getId()), null);
        harness.passBothPriorities();

        // The 8/8 has the greatest power, so it is the sacrifice; its own replacements are asked first.
        harness.assertOnBattlefield(player2, "Avatar of Might");
        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("Put it on the bottom of its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertNotOnBattlefield(player2, "Avatar of Might");
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getId()).isEqualTo(avatar.getCard().getId());
        harness.assertOnBattlefield(player2, "Lich's Duel Mastery");
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getId()).isEqualTo(mastery.getCard().getId());
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Will of the Abzan");
        assertThat(zombieTokens()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({XathridDemon.class, Opalescence.class, WheelOfSunAndMoon.class, KalitasTraitorOfGhet.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Xathrid Demon's chosen sacrifice comes first, then the opponent's Mastery is asked before any life is lost")
    void xathridDemonSacrificeThenAsksTheMasteryBeforeAnyLifeIsLost() {
        Permanent mastery = animatedShieldlessMasteryAgainstKalitas();
        harness.addToBattlefield(player1, new XathridDemon());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("Put it on the bottom of its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Lich's Duel Mastery");
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Kalitas, Traitor of Ghet");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId()).contains(mastery.getCard().getId());
        harness.assertLife(player2, 20);
        assertThat(zombieTokens()).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({MornsongAria.class, Opalescence.class, WheelOfSunAndMoon.class, KalitasTraitorOfGhet.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Mornsong Aria's draw-step life loss asks library-or-Kalitas before any life changes, then the search follows")
    void loseLifeThenSearchAsksTheMasteryBeforeAnyLifeIsLost() {
        Permanent mastery = animatedShieldlessMasteryAgainstKalitas();
        harness.addToBattlefield(player1, new MornsongAria());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.passUntil(player2, TurnStep.DRAW);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("Put it on the bottom of its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Lich's Duel Mastery");
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Kalitas, Traitor of Ghet");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId()).contains(mastery.getCard().getId());
        harness.assertLife(player2, 20);
        assertThat(zombieTokens()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().playerId())
                .isEqualTo(player2.getId());
    }

    @Test
    @CardUsed({StabWound.class, Opalescence.class, WheelOfSunAndMoon.class, KalitasTraitorOfGhet.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Stab Wound's upkeep life loss asks library-or-Kalitas before any life changes")
    void enchantedCreatureControllerLifeLossAsksTheMasteryBeforeAnyLifeIsLost() {
        Permanent mastery = animatedShieldlessMasteryAgainstKalitas();
        Permanent stabWound = harness.addToBattlefieldAndReturn(player1, new StabWound());
        stabWound.setAttachedTo(mastery.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("Put it on the bottom of its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Lich's Duel Mastery");
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Kalitas, Traitor of Ghet");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId()).contains(mastery.getCard().getId());
        harness.assertLife(player2, 20);
        assertThat(zombieTokens()).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({DuskmantleSeer.class, Opalescence.class, WheelOfSunAndMoon.class, KalitasTraitorOfGhet.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Duskmantle Seer asks the opponent's Mastery before the reveal, then both lose life at once and take their cards")
    void revealThenSimultaneousLossesAskTheMasteryBeforeAnyLifeIsLost() {
        Permanent mastery = animatedShieldlessMasteryAgainstKalitas();
        harness.addToBattlefield(player1, new DuskmantleSeer());
        Card ownCard = new GrizzlyBears();
        Card opponentCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(ownCard));
        harness.setLibrary(player2, List.of(opponentCard));
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("Put it on the bottom of its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Lich's Duel Mastery");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player2.getId())).extracting(Card::getId)
                .containsExactly(mastery.getCard().getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(ownCard);
        assertThat(gd.playerHands.get(player2.getId())).contains(opponentCard);
        assertThat(zombieTokens()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({ThoughtStringAnalyst.class, Opalescence.class, WheelOfSunAndMoon.class, KalitasTraitorOfGhet.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Thought-String Analyst asks library-or-Kalitas before the exile, then exiles once and loses no life")
    void exileFromLibraryThenLifeLossAsksTheMasteryBeforeTheExile() {
        Permanent mastery = animatedShieldlessMasteryAgainstKalitas();
        harness.addToBattlefield(player2, new ThoughtStringAnalyst());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player2);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("Put it on the bottom of its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Lich's Duel Mastery");
        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);

        harness.handleListChoice(player2, "Kalitas, Traitor of Ghet");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId())
                .contains(mastery.getCard().getId(), topCard.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player2.getId());
        harness.assertLife(player2, 20);
        assertThat(zombieTokens()).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({SingeMindOgre.class, Opalescence.class, WheelOfSunAndMoon.class, KalitasTraitorOfGhet.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Singe-Mind Ogre's random reveal comes first, then library-or-Kalitas is asked before any life changes")
    void randomRevealThenLifeLossAsksTheMasteryBeforeAnyLifeIsLost() {
        Permanent mastery = animatedShieldlessMasteryAgainstKalitas();
        Card revealed = new GrizzlyBears();
        harness.setHand(player2, List.of(revealed));
        harness.setHand(player1, List.of(new SingeMindOgre()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("Put it on the bottom of its owner's library", "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Lich's Duel Mastery");
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getId()).isEqualTo(mastery.getCard().getId());
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(revealed);
        harness.assertLife(player2, 20);
        assertThat(zombieTokens()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({PalantirOfOrthanc.class, Opalescence.class, WheelOfSunAndMoon.class, KalitasTraitorOfGhet.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Palantir of Orthanc's mill comes first, then library-or-Kalitas is asked before any life changes")
    void millThenLifeLossAsksTheMasteryBeforeAnyLifeIsLost() {
        Permanent mastery = animatedShieldlessMasteryAgainstKalitas();
        harness.addToBattlefield(player1, new PalantirOfOrthanc());
        Card milled = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), milled));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("Put it on the bottom of its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(milled);
        harness.assertOnBattlefield(player2, "Lich's Duel Mastery");
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Kalitas, Traitor of Ghet");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId()).contains(mastery.getCard().getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(milled);
        harness.assertLife(player2, 20);
        assertThat(zombieTokens()).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({DeadlyTempest.class, Opalescence.class, WheelOfSunAndMoon.class, KalitasTraitorOfGhet.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Deadly Tempest asks the Mastery after the destruction and before either player loses life")
    void destroyAllThenSimultaneousLossesAskTheMasteryBeforeAnyLifeIsLost() {
        Permanent mastery = animatedShieldlessMasteryAgainstKalitas();
        mastery.setRegenerationShield(1);
        kalitas().setRegenerationShield(1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GrizzlyBears tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player2, tokenCard);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new DeadlyTempest()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("Put it on the bottom of its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(bears.getCard().getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(token);
        assertThat(mastery.isTapped()).isTrue();
        harness.assertOnBattlefield(player2, "Lich's Duel Mastery");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.playerDecks.get(player2.getId())).extracting(Card::getId)
                .contains(mastery.getCard().getId());
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        assertThat(zombieTokens()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({RainOfDaggers.class, Opalescence.class, WheelOfSunAndMoon.class, KalitasTraitorOfGhet.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Rain of Daggers' life loss asks the caster's Mastery after the destruction, destroying once")
    void destroyAllThenLifeLossAsksTheMasteryBeforeAnyLifeIsLost() {
        Permanent mastery = animatedShieldlessMasteryAgainstKalitas();
        kalitas().setRegenerationShield(1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new RainOfDaggers()));
        harness.addMana(player2, ManaColor.BLACK, 6);
        harness.forceActivePlayer(player2);

        harness.castAndResolveSorcery(player2, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).options())
                .containsExactlyInAnyOrder("Put it on the bottom of its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactly(bears.getCard().getId());
        harness.assertOnBattlefield(player1, "Kalitas, Traitor of Ghet");
        harness.assertOnBattlefield(player2, "Lich's Duel Mastery");
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Kalitas, Traitor of Ghet");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getId()).contains(mastery.getCard().getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactly(bears.getCard().getId());
        harness.assertLife(player2, 20);
        assertThat(zombieTokens()).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({BumpInTheNight.class, WheelOfSunAndMoon.class, LeylineOfTheVoid.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("A spell's life loss asks library-or-exile for the noncreature, shieldless Mastery before any life changes")
    void lifeLossAsksTheGraveyardReplacementForTheNoncreatureMastery() {
        harness.setLife(player2, 20);
        Permanent mastery = harness.addToBattlefieldAndReturn(player2, new LichsDuelMastery());
        harness.addToBattlefieldAndReturn(player2, new WheelOfSunAndMoon()).setAttachedTo(player2.getId());
        harness.addToBattlefield(player1, new LeylineOfTheVoid());
        harness.setHand(player1, List.of(new BumpInTheNight()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.options())
                .containsExactlyInAnyOrder("Put it on the bottom of its owner's library", "Exile it");
        harness.assertOnBattlefield(player2, "Lich's Duel Mastery");
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Exile it");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(mastery.getCard());
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(mastery.getCard());
        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed({HideousEnd.class, WheelOfSunAndMoon.class, LeylineOfTheVoid.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Hideous End asks the destroyed creature's controller's Mastery after the destruction and before any life is lost")
    void destroyThenTargetControllerLifeLossAsksTheMasteryBeforeAnyLifeIsLost() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        GrizzlyBears tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player2, tokenCard);
        harness.setHand(player1, List.of(new HideousEnd()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstant(player1, 0, token.getId());
        harness.passBothPriorities();

        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.options())
                .containsExactlyInAnyOrder("Put it on the bottom of its owner's library", "Exile it");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(token);
        harness.assertOnBattlefield(player2, "Lich's Duel Mastery");
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getId()).isEqualTo(mastery.getCard().getId());
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Hideous End");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({FeedTheSwarm.class, WheelOfSunAndMoon.class, LeylineOfTheVoid.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Feed the Swarm asks the caster's Mastery after the destruction, destroying once")
    void destroyThenCasterLifeLossAsksTheMasteryBeforeAnyLifeIsLost() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, List.of(new FeedTheSwarm()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);

        harness.castAndResolveSorcery(player2, 0, bears.getId());

        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.options())
                .containsExactlyInAnyOrder("Put it on the bottom of its owner's library", "Exile it");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactly(bears.getCard().getId());
        harness.assertOnBattlefield(player2, "Lich's Duel Mastery");
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Exile it");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(mastery.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactly(bears.getCard().getId());
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({SorinMarkov.class, WheelOfSunAndMoon.class, LeylineOfTheVoid.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("\"Life total becomes 10\" asks the Mastery's sacrifice before the life total changes")
    void setLifeTotalAsksTheMasteryBeforeTheLifeTotalChanges() {
        Permanent sorin = harness.addToBattlefieldAndReturn(player1, new SorinMarkov());
        sorin.setCounterCount(CounterType.LOYALTY, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Exile it");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(mastery.getCard());
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({Biorhythm.class, WheelOfSunAndMoon.class, LeylineOfTheVoid.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Biorhythm asks the Mastery's sacrifice before the first player's life total rises")
    void eachPlayerSetLifeTotalAsksTheMasteryBeforeAnyLifeTotalChanges() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 1);

        harness.castFromHand(player1, new Biorhythm(), "{6}{G}{G}");
        harness.passBothPriorities();

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player1, 1);
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getId()).isEqualTo(mastery.getCard().getId());
        harness.assertLife(player1, 2);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({SoulConduit.class, WheelOfSunAndMoon.class, LeylineOfTheVoid.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Exchanging life totals asks the losing player's Mastery before the gaining player's total rises")
    void exchangeLifeTotalsAsksTheMasteryBeforeEitherLifeTotalChanges() {
        harness.addToBattlefieldAndReturn(player1, new SoulConduit()).setSummoningSick(false);
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player1.getId(), player2.getId()));
        harness.passBothPriorities();

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Exile it");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(mastery.getCard());
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({PsychicTransfer.class, WheelOfSunAndMoon.class, LeylineOfTheVoid.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Psychic Transfer asks the target's Mastery before the caster's life total rises")
    void psychicTransferAsksTheMasteryBeforeEitherLifeTotalChanges() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.setLife(player1, 16);
        harness.setHand(player1, List.of(new PsychicTransfer()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getId()).isEqualTo(mastery.getCard().getId());
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({TreeOfPerdition.class, WheelOfSunAndMoon.class, LeylineOfTheVoid.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Tree of Perdition asks the Mastery's sacrifice before the life total or the toughness changes")
    void exchangeLifeTotalWithToughnessAsksTheMasteryBeforeAnythingChanges() {
        Permanent tree = harness.addToBattlefieldAndReturn(player1, new TreeOfPerdition());
        tree.setSummoningSick(false);
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(13);

        harness.handleListChoice(player2, "Exile it");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(mastery.getCard());
        harness.assertLife(player2, 20);
        assertThat(gqs.getEffectiveToughness(gd, tree)).isEqualTo(20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({ReverseTheSands.class, WheelOfSunAndMoon.class, LeylineOfTheVoid.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Reverse the Sands asks the Mastery's sacrifice before the first player's life total rises")
    void redistributeLifeTotalsAsksTheMasteryBeforeAnyLifeTotalChanges() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.setLife(player1, 5);
        String swap = gd.playerIdToName.get(player1.getId()) + ": 20; "
                + gd.playerIdToName.get(player2.getId()) + ": 5";

        harness.castFromHand(player1, new ReverseTheSands(), "{6}{W}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, swap);

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player1, 5);
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Exile it");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(mastery.getCard());
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({RhysticSyphon.class, WheelOfSunAndMoon.class, LeylineOfTheVoid.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Rhystic Syphon's loss of a target who can't pay asks the Mastery before any life changes")
    void unpaidLifeLossAsksTheMasteryBeforeAnyLifeChanges() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.setHand(player1, List.of(new RhysticSyphon()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Exile it");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(mastery.getCard());
        harness.assertLife(player1, 25);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({RhysticSyphon.class, WheelOfSunAndMoon.class, LeylineOfTheVoid.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Declining to pay for Rhystic Syphon asks the Mastery before any life changes")
    void declinedPaymentLifeLossAsksTheMasteryBeforeAnyLifeChanges() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.setHand(player1, List.of(new RhysticSyphon()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getId()).isEqualTo(mastery.getCard().getId());
        harness.assertLife(player1, 25);
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({PainfulQuandary.class, CarapaceForger.class, WheelOfSunAndMoon.class, LeylineOfTheVoid.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Painful Quandary's loss of a player with no cards to discard is a life loss the Mastery replaces")
    void emptyHandLifeLossAsksTheMasteryBeforeAnyLifeChanges() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.addToBattlefield(player1, new PainfulQuandary());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of());

        harness.castFromHand(player2, new CarapaceForger(), "{1}{G}");
        harness.passBothPriorities();

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Exile it");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(mastery.getCard());
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({PainfulQuandary.class, CarapaceForger.class, WheelOfSunAndMoon.class, LeylineOfTheVoid.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("Declining Painful Quandary's discard asks the Mastery before any life changes")
    void declinedDiscardLifeLossAsksTheMasteryBeforeAnyLifeChanges() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.addToBattlefield(player1, new PainfulQuandary());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Card kept = new GrizzlyBears();
        harness.setHand(player2, List.of(new CarapaceForger(), kept));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getId()).isEqualTo(mastery.getCard().getId());
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(kept);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({FandanielTelophoroiAscian.class, WheelOfSunAndMoon.class, LeylineOfTheVoid.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("An opponent declining Fandaniel's sacrifice is asked about the Mastery before any life is lost")
    void declinedSacrificeLifeLossAsksTheMasteryBeforeAnyLifeIsLost() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.addToBattlefield(player1, new FandanielTelophoroiAscian());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Shock(), new Shock()));

        passToEndStepAndResolve();
        harness.handleMayAbilityChosen(player2, false);

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Exile it");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(mastery.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({FandanielTelophoroiAscian.class, WheelOfSunAndMoon.class, LeylineOfTheVoid.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("An opponent without a nontoken creature for Fandaniel is asked about the Mastery before any life is lost")
    void noSacrificeLifeLossAsksTheMasteryBeforeAnyLifeIsLost() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        harness.addToBattlefield(player1, new FandanielTelophoroiAscian());
        harness.setGraveyard(player1, List.of(new Shock(), new Shock()));

        passToEndStepAndResolve();

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getId()).isEqualTo(mastery.getCard().getId());
        harness.assertLife(player2, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({BraidsArisenNightmare.class, RelicOfLegends.class, LightningStrike.class, WheelOfSunAndMoon.class,
            LeylineOfTheVoid.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("An opponent without a permanent for Braids is asked about the Mastery before the loss and the draw")
    void noMatchingPermanentAsksTheMasteryBeforeTheLossAndTheDraw() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        LightningStrike drawn = new LightningStrike();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.addToBattlefield(player1, new BraidsArisenNightmare());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new RelicOfLegends());

        passToEndStepAndResolve();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.handleListChoice(player2, "Exile it");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(mastery.getCard());
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @CardUsed({BraidsArisenNightmare.class, RelicOfLegends.class, LightningStrike.class, WheelOfSunAndMoon.class,
            LeylineOfTheVoid.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("An opponent declining Braids's sacrifice is asked about the Mastery before the loss and the draw")
    void declinedMatchingSacrificeAsksTheMasteryBeforeTheLossAndTheDraw() {
        Permanent mastery = noncreatureShieldlessMasteryBetweenWheelAndLeyline();
        LightningStrike drawn = new LightningStrike();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.addToBattlefield(player1, new BraidsArisenNightmare());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new RelicOfLegends());
        harness.addToBattlefield(player2, new RelicOfLegends());

        passToEndStepAndResolve();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertMasteryAskedLibraryOrExile();
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        harness.assertNotOnBattlefield(player2, "Lich's Duel Mastery");
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getId()).isEqualTo(mastery.getCard().getId());
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player2, "Relic of Legends");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    /** Moves player 1's turn to its end step and resolves the first trigger there. */
    private void passToEndStepAndResolve() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();
    }

    private void assertMasteryAskedLibraryOrExile() {
        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
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
    private Permanent noncreatureShieldlessMasteryBetweenWheelAndLeyline() {
        harness.setLife(player2, 20);
        Permanent mastery = harness.addToBattlefieldAndReturn(player2, new LichsDuelMastery());
        harness.addToBattlefieldAndReturn(player2, new WheelOfSunAndMoon()).setAttachedTo(player2.getId());
        harness.addToBattlefield(player1, new LeylineOfTheVoid());
        return mastery;
    }

    private Permanent kalitas() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof KalitasTraitorOfGhet)
                .findFirst().orElseThrow();
    }

    /**
     * Player 2 controls a Mastery without shields that Opalescence makes a creature and that Wheel of Sun and Moon's
     * enchanted player owns; player 1 controls Kalitas. Sacrificing the Mastery then needs its controller's choice
     * between the library and Kalitas's exile (CR 616.1).
     */
    private Permanent animatedShieldlessMasteryAgainstKalitas() {
        harness.setLife(player2, 20);
        Permanent mastery = harness.addToBattlefieldAndReturn(player2, new LichsDuelMastery());
        harness.addToBattlefield(player2, new Opalescence());
        Permanent wheel = harness.addToBattlefieldAndReturn(player2, new WheelOfSunAndMoon());
        wheel.setAttachedTo(player2.getId());
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        assertThat(gd.getCardsExiledByPermanent(mastery.getId())).isEmpty();
        return mastery;
    }

    private long zombieTokens() {
        return zombieTokens(player1);
    }

    private long zombieTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count();
    }
}
