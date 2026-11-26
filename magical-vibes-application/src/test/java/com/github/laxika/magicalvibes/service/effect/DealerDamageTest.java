package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.cards.a.AjanisPridemate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.j.JusticeStrike;
import com.github.laxika.magicalvibes.cards.m.MoonlightHunt;
import com.github.laxika.magicalvibes.cards.n.NibelheimAflame;
import com.github.laxika.magicalvibes.cards.p.PlagueStinger;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.r.RabidBite;
import com.github.laxika.magicalvibes.cards.s.SilverKnight;
import com.github.laxika.magicalvibes.cards.t.TimberpackWolf;
import com.github.laxika.magicalvibes.cards.v.VampireNighthawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Damage dealt by a permanent other than the resolving spell: each dealer is the source of its own damage (CR 120.1;
 * "it's the object ... that does it", CR 608.2h), so its lifelink (CR 702.15b), deathtouch (CR 702.2b) and infect
 * apply, each dealer's damage is its own; every prevention and protection is an option of the recipient's damage pool,
 * ordered by its controller (CR 616.1); a face-down dealer is named by no card.
 */
@CardUsed({AjanisPridemate.class, GrizzlyBears.class, HillGiant.class, JusticeStrike.class,
        MoonlightHunt.class, NibelheimAflame.class, PlagueStinger.class, ProdigalPyromancer.class, RabidBite.class,
        SilverKnight.class, TimberpackWolf.class, VampireNighthawk.class})
class DealerDamageTest extends BaseCardTest {

    @BeforeEach
    void setUpTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
    }

    /** Casts {@code card} from player 1's hand with {@code targets}, leaving it on the stack. */
    private void cast(Card card, UUID... targets) {
        harness.setHand(player1, List.of(card));
        for (ManaColor color : List.of(ManaColor.WHITE, ManaColor.RED, ManaColor.GREEN)) {
            harness.addMana(player1, color, 4);
        }
        if (card.hasType(CardType.INSTANT)) {
            harness.castInstant(player1, 0, targets[0]);
        } else {
            harness.castSorcery(player1, 0, List.of(targets));
        }
    }

    private void castAndResolve(Card card, UUID... targets) {
        cast(card, targets);
        harness.passBothPriorities();
        resolveAllTriggers();
    }

    @Nested
    @DisplayName("The dealer's keywords apply to its damage")
    class DealerKeywords {

        @Test
        @DisplayName("A bite by a lifelink deathtouch creature gains its controller life once and destroys the recipient")
        void lifelinkAndDeathtouch() {
            Permanent nighthawk = addCreatureReady(player1, new VampireNighthawk());
            Permanent pridemate = addCreatureReady(player1, new AjanisPridemate());
            Permanent giant = addCreatureReady(player2, new HillGiant());

            castAndResolve(new RabidBite(), nighthawk.getId(), giant.getId());

            harness.assertLife(player1, 22);
            assertThat(pridemate.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
            harness.assertNotOnBattlefield(player2, "Hill Giant");
        }

        @Test
        @DisplayName("A bite by an infect creature puts -1/-1 counters on the recipient")
        void infect() {
            Permanent stinger = addCreatureReady(player1, new PlagueStinger());
            Permanent giant = addCreatureReady(player2, new HillGiant());

            castAndResolve(new RabidBite(), stinger.getId(), giant.getId());

            assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
            assertThat(giant.getMarkedDamage()).isZero();
        }

        @Test
        @DisplayName("A creature dealing damage to itself is both dealer and recipient: its lifelink and deathtouch apply")
        void dealerIsRecipient() {
            Permanent nighthawk = addCreatureReady(player1, new VampireNighthawk());

            castAndResolve(new JusticeStrike(), nighthawk.getId());

            harness.assertLife(player1, 22);
            harness.assertNotOnBattlefield(player1, "Vampire Nighthawk");
        }

        @Test
        @DisplayName("A target creature dealing damage to each other creature: its lifelink and deathtouch apply to each")
        void eachOtherCreatureLifelinkOnce() {
            Permanent nighthawk = addCreatureReady(player1, new VampireNighthawk());
            addCreatureReady(player2, new GrizzlyBears());
            addCreatureReady(player2, new HillGiant());

            castAndResolve(new NibelheimAflame(), nighthawk.getId());

            // Two other creatures dealt 2 each by the Nighthawk; its deathtouch destroys both.
            harness.assertLife(player1, 24);
            assertThat(nighthawk.getMarkedDamage()).isZero();
            harness.assertNotOnBattlefield(player2, "Hill Giant");
        }
    }

    @Nested
    @DisplayName("Each of a set deals its own damage")
    class EachOfASet {

        @Test
        @DisplayName("Each Wolf deals damage equal to its power to the target creature; another creature deals none")
        void eachWolfDealsItsPower() {
            addCreatureReady(player1, new TimberpackWolf());
            addCreatureReady(player1, new GrizzlyBears());
            Permanent giant = addCreatureReady(player2, new HillGiant());

            cast(new MoonlightHunt(), giant.getId());
            harness.passBothPriorities();

            assertThat(giant.getMarkedDamage()).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("A face-down dealer is named by no card in the log")
    void faceDownDealer() {
        Permanent hidden = addCreatureReady(player1, new HillGiant());
        hidden.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        gd.gameLog.clear();

        castAndResolve(new RabidBite(), hidden.getId(), bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).noneMatch(line -> line.contains("Hill Giant"));
    }

    @Nested
    @DisplayName("Protection from the dealer is an option of the recipient's damage pool")
    class ProtectionInThePool {

        @Test
        @DisplayName("Protection from red prevents a red creature's bite")
        void protectionPrevents() {
            Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
            Permanent knight = addCreatureReady(player2, new SilverKnight());

            castAndResolve(new RabidBite(), pyromancer.getId(), knight.getId());

            assertThat(knight.getMarkedDamage()).isZero();
        }

        @Test
        @DisplayName("Protection and a shield on the recipient: its controller orders them (CR 616.1)")
        void protectionAndShieldOrdered() {
            Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
            Permanent knight = addCreatureReady(player2, new SilverKnight());
            gd.addPermanentDamagePreventionShield(knight.getId(), 5);

            cast(new RabidBite(), pyromancer.getId(), knight.getId());
            harness.passBothPriorities();

            PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
            assertThat(choice).as("the recipient's controller is asked to order the preventions").isNotNull();
            assertThat(choice.context()).isInstanceOf(ChoiceContext.ReplacementEffectOrder.class);
            assertThat(choice.playerId()).isEqualTo(player2.getId());
            harness.handleListChoice(player2, choice.options().stream()
                    .filter(option -> option.startsWith("Prevent the next damage to it"))
                    .findFirst().orElseThrow());
            assertThat(gd.permanentDamagePreventionShield(knight.getId())).isEqualTo(4);
            assertThat(knight.getMarkedDamage()).isZero();
        }
    }
}
