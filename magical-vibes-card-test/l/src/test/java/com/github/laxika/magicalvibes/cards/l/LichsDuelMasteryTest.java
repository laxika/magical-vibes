package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BumpInTheNight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KalitasTraitorOfGhet;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.o.OrzhovGuildmage;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WheelOfSunAndMoon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
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
