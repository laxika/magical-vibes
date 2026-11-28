package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BodyOfKnowledge;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CompassionateHealer;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.k.KarlovWatchdog;
import com.github.laxika.magicalvibes.cards.p.PhyrexianCrusader;
import com.github.laxika.magicalvibes.cards.p.PressurePoint;
import com.github.laxika.magicalvibes.cards.p.PunctureBlast;
import com.github.laxika.magicalvibes.cards.s.SphereOfResistance;
import com.github.laxika.magicalvibes.cards.u.UnableToScream;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IllusionaryMask.class, GrizzlyBears.class, CrawWurm.class,
        LightningBolt.class, SphereOfResistance.class, UnableToScream.class, KarlovWatchdog.class,
        PunctureBlast.class, PhyrexianCrusader.class, BodyOfKnowledge.class,
        CompassionateHealer.class, PressurePoint.class})
class IllusionaryMaskTest extends BaseCardTest {

    @Test
    void castsAQualifyingCreatureFaceDown() {
        harness.addToBattlefield(player1, new IllusionaryMask());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.isFaceDown()).isTrue();
        assertThat(bears.getFaceDownPower()).isEqualTo(2);
        assertThat(bears.getFaceDownToughness()).isEqualTo(2);

        bears.addMarkedDamage(null, 1);
        assertThat(bears.isFaceDown()).isFalse();
    }

    @Test
    void doesNotOfferAColoredCostTheSpentManaCannotPay() {
        harness.addToBattlefield(player1, new IllusionaryMask());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void mayDeclineCastingTheCreature() {
        harness.addToBattlefield(player1, new IllusionaryMask());
        GrizzlyBears card = new GrizzlyBears();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void turnsFaceUpBeforeAssigningCombatDamageAsABlocker() {
        harness.addToBattlefield(player1, new IllusionaryMask());
        harness.setHand(player1, List.of(new CrawWurm()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.activateAbility(player1, 0, 6, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        Permanent blocker = findPermanent(player1, "Craw Wurm");
        Permanent attacker = addCreatureReady(player2, new CrawWurm());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));
        resolveCombat(player2);

        assertThat(blocker.isFaceDown()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(blocker.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(attacker.getCard());
    }

    @Test
    void damageStillTurnsTheCreatureFaceUpAfterCleanup() {
        harness.addToBattlefield(player1, new IllusionaryMask());
        harness.setHand(player1, List.of(new CrawWurm()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.activateAbility(player1, 0, 6, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        Permanent wurm = findPermanent(player1, "Craw Wurm");

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, wurm.getId());

        assertThat(wurm.isFaceDown()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wurm);
        assertThat(wurm.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void paysSpellCostIncreasesWhenCastingFaceDown() {
        harness.addToBattlefield(player1, new IllusionaryMask());
        harness.addToBattlefield(player2, new SphereOfResistance());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Grizzly Bears").isFaceDown()).isTrue();
    }

    @Test
    @DisplayName("A masked creature that can't be turned face up becomes tapped and stays face down")
    void tappedMaskedCreatureThatCantTurnFaceUpStaysFaceDown() {
        Permanent masked = addMaskedCreature(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new UnableToScream());
        aura.setAttachedTo(masked.getId());

        declareAttackers(player1, List.of(0));

        assertThat(masked.isTapped()).isTrue();
        assertThat(masked.isFaceDown()).isTrue();
        assertThat(gd.playersWhoTurnedPermanentsFaceUpThisTurn).doesNotContain(player1.getId());
    }

    @Test
    @DisplayName("A masked creature that can't be turned face up is dealt damage as the face-down 2/2")
    void damagedMaskedCreatureThatCantTurnFaceUpStaysFaceDown() {
        Permanent masked = addMaskedCreature(player1, new CrawWurm());
        harness.addToBattlefield(player2, new KarlovWatchdog());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, masked.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(masked);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(masked.getCard());
    }

    @Test
    @DisplayName("A masked blocker that can't be turned face up assigns combat damage as the face-down 2/2")
    void maskedBlockerThatCantTurnFaceUpDealsFaceDownDamage() {
        Permanent masked = addMaskedCreature(player1, new CrawWurm());
        harness.addToBattlefield(player2, new KarlovWatchdog());
        Permanent attacker = addCreatureReady(player2, new CrawWurm());

        declareAttackersAndPrepareBlockers(player2, List.of(1));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 1)));
        resolveCombat(player2);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(masked.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("A masked creature dealt wither damage is turned face up and gets the -1/-1 counters")
    void witherDamageTurnsMaskedCreatureFaceUp() {
        Permanent masked = addMaskedCreature(player1, new CrawWurm());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new PunctureBlast()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player2, 0, masked.getId());

        assertThat(masked.isFaceDown()).isFalse();
        assertThat(masked.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(masked);
    }

    @Test
    @DisplayName("A masked blocker dealt first-strike infect damage is turned face up before it deals its own")
    void infectCombatDamageTurnsMaskedBlockerFaceUp() {
        Permanent masked = addMaskedCreature(player1, new CrawWurm());
        Permanent attacker = addCreatureReady(player2, new PhyrexianCrusader());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        assertThat(masked.isFaceDown()).isFalse();
        assertThat(masked.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(masked);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(attacker.getCard());
    }

    @Test
    @DisplayName("A masked creature that can't be turned face up doesn't trigger its face-up card's damage ability")
    void maskedCreatureThatCantTurnFaceUpDoesNotTriggerItsHiddenAbilities() {
        Permanent masked = addMaskedCreature(player1, new BodyOfKnowledge());
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player2, new KarlovWatchdog());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, masked.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(masked.getCard());
    }

    @Test
    @DisplayName("A masked creature that can't be turned face up doesn't trigger its face-up card's tap ability")
    void tappedMaskedCreatureThatCantTurnFaceUpDoesNotTriggerItsHiddenAbilities() {
        Permanent masked = addMaskedCreature(player1, new CompassionateHealer());
        harness.addToBattlefield(player2, new KarlovWatchdog());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new PressurePoint()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player2, 0, masked.getId());

        assertThat(masked.isTapped()).isTrue();
        assertThat(masked.isFaceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    private Permanent addMaskedCreature(Player player, Card card) {
        Permanent permanent = addCreatureReady(player, card);
        permanent.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        permanent.setTurnsFaceUpOnDamageOrTap(true);
        return permanent;
    }
}
