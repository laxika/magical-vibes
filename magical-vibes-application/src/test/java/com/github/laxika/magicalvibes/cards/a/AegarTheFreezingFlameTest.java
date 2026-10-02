package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.ExquisiteFirecraft;
import com.github.laxika.magicalvibes.cards.b.BasaltRavager;
import com.github.laxika.magicalvibes.cards.d.DemonBolt;
import com.github.laxika.magicalvibes.cards.f.FearlessPup;
import com.github.laxika.magicalvibes.cards.f.FrostBite;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.n.NikoAris;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AegarTheFreezingFlame.class, ExquisiteFirecraft.class, GrizzlyBears.class,
        HillGiant.class, Shock.class, BasaltRavager.class, DemonBolt.class,
        FearlessPup.class, FrostBite.class, NikoAris.class})
class AegarTheFreezingFlameTest extends BaseCardTest {

    @Test
    void drawsWhenSpellDealsExcessDamageToOpponentCreature() {
        harness.addToBattlefield(player1, new AegarTheFreezingFlame());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new ExquisiteFirecraft()));
        harness.addMana(player1, ManaColor.RED, 3);
        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castAndResolveSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore - 1);
    }

    @Test
    void doesNotDrawWhenNoExcessDamageIsDealt() {
        harness.addToBattlefield(player1, new AegarTheFreezingFlame());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore);
    }

    @Test
    void drawsWhenGiantDealsExcessCombatDamageToOpponentCreature() {
        harness.addToBattlefield(player1, new AegarTheFreezingFlame());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        giant.setSummoningSick(false);
        giant.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setSummoningSick(false);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore - 1);
    }

    @Test
    void drawsWhenGiantWizardDealsExcessNoncombatDamage() {
        harness.addToBattlefield(player1, new AegarTheFreezingFlame());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FearlessPup());
        harness.setLibrary(player1, List.of(new FearlessPup()));
        harness.setHand(player1, List.of(new BasaltRavager()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fearless Pup");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotTriggerForOpponentSpellWithoutEarlierQualifyingDamage() {
        harness.addToBattlefield(player1, new AegarTheFreezingFlame());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FearlessPup());
        harness.setLibrary(player1, List.of(new FearlessPup()));
        harness.setHand(player2, List.of(new FrostBite()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, target.getId());

        harness.assertInGraveyard(player2, "Fearless Pup");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void earlierSpellDamageQualifiesWhenOpponentLaterDealsExcessDamage() {
        harness.addToBattlefield(player1, new AegarTheFreezingFlame());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AegarTheFreezingFlame());
        harness.setLibrary(player1, List.of(new FearlessPup()));
        harness.setHand(player1, List.of(new FrostBite()));
        harness.setHand(player2, List.of(new FrostBite()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gd.stack).isEmpty();
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotDrawForExcessDamageToOwnCreature() {
        harness.addToBattlefield(player1, new AegarTheFreezingFlame());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new FearlessPup());
        harness.setLibrary(player1, List.of(new FearlessPup()));
        harness.setHand(player1, List.of(new FrostBite()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void exactlyLethalDamageDoesNotTrigger() {
        harness.addToBattlefield(player1, new AegarTheFreezingFlame());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BasaltRavager());
        harness.setLibrary(player1, List.of(new FearlessPup()));
        harness.setHand(player1, List.of(new FrostBite()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Basalt Ravager");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void stillDrawsIfAegarLeavesBeforeAbilityResolves() {
        Permanent aegar = harness.addToBattlefieldAndReturn(player1, new AegarTheFreezingFlame());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FearlessPup());
        harness.setLibrary(player1, List.of(new FearlessPup()));
        harness.setHand(player1, List.of(new FrostBite()));
        harness.setHand(player2, List.of(new DemonBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, aegar.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Aegar, the Freezing Flame");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void drawsWhenAnyTargetSpellDealsExcessDamageToPlaneswalker() {
        harness.addToBattlefield(player1, new AegarTheFreezingFlame());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NikoAris());
        target.setCounterCount(CounterType.LOYALTY, 3);
        harness.setLibrary(player1, List.of(new FearlessPup()));
        harness.setHand(player1, List.of(new ExquisiteFirecraft()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, target.getId());
        harness.inMutationScope(() -> {
            harness.getStackResolutionService().resolveTopOfStack(gd);
            harness.getStackResolutionService().resolveTopOfStack(gd);
        });

        harness.assertInGraveyard(player2, "Niko Aris");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void drawsWhenCreatureOrPlaneswalkerSpellDealsExcessDamageToPlaneswalker() {
        harness.addToBattlefield(player1, new AegarTheFreezingFlame());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NikoAris());
        target.setCounterCount(CounterType.LOYALTY, 3);
        harness.setLibrary(player1, List.of(new FearlessPup()));
        harness.setHand(player1, List.of(new DemonBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Niko Aris");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
