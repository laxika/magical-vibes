package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CloudkinSeer;
import com.github.laxika.magicalvibes.cards.b.BoneToAsh;
import com.github.laxika.magicalvibes.cards.k.KioraBehemothBeckoner;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.n.Negate;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.m.MawOfTheMire;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.r.RedElementalBlast;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VeilOfSummer.class, Forest.class, CloudkinSeer.class, MawOfTheMire.class, MindRot.class,
        RedElementalBlast.class, BoneToAsh.class, KioraBehemothBeckoner.class, Murder.class, Negate.class,
        Shock.class, Unsummon.class})
class VeilOfSummerTest extends BaseCardTest {

    @Test
    void drawsOnlyAfterAnOpponentCastsBlueOrBlackSpell() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new VeilOfSummer()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.setHand(player2, List.of(new CloudkinSeer()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new VeilOfSummer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void protectsControllerAndAllControlledPermanentsFromBlackTargeting() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new VeilOfSummer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0);

        harness.setHand(player2, List.of(new MawOfTheMire()));
        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");

        harness.setHand(player2, List.of(new MindRot()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    void makesAllControlledSpellsUncounterable() {
        harness.setHand(player1, List.of(new VeilOfSummer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0);

        CloudkinSeer seer = new CloudkinSeer();
        harness.setHand(player1, List.of(seer));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.setHand(player2, List.of(new RedElementalBlast()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, seer.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cloudkin Seer");
    }

    @Test
    void drawsAfterAnOpponentCastsBlackSpellEvenIfItsTargetBecomesIllegal() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CloudkinSeer());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new VeilOfSummer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, creature.getId());
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Cloudkin Seer");
        harness.assertInGraveyard(player2, "Murder");
    }

    @Test
    void doesNotDrawForOwnBlueSpell() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CloudkinSeer());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Unsummon(), new VeilOfSummer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature.getCard());
    }

    @Test
    void doesNotDrawForOpponentsRedSpell() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.setHand(player1, List.of(new VeilOfSummer()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 18);
    }

    @Test
    void canBeCounteredBeforeItResolves() {
        harness.setHand(player1, List.of(new VeilOfSummer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new Negate()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        VeilOfSummer veil = (VeilOfSummer) gd.playerHands.get(player1.getId()).getFirst();
        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, veil.getId());

        harness.assertInGraveyard(player1, "Veil of Summer");
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CloudkinSeer());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.assertNotOnBattlefield(player1, "Cloudkin Seer");
    }

    @Test
    void protectsAnAlreadyCastSpellButCounterspellsStillDrawCards() {
        CloudkinSeer creature = new CloudkinSeer();
        harness.setHand(player1, List.of(creature, new VeilOfSummer()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new BoneToAsh()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, creature.getId());
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cloudkin Seer");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    void blocksBlueTargetingButAllowsRedAndControllerTargeting() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CloudkinSeer());
        harness.setHand(player1, List.of(new VeilOfSummer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0);
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 18);

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.assertInHand(player1, "Cloudkin Seer");
    }

    @Test
    void doesNotProtectPermanentsEnteringAfterResolution() {
        harness.setHand(player1, List.of(new VeilOfSummer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CloudkinSeer());
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.assertInGraveyard(player1, "Cloudkin Seer");
    }

    @Test
    void hexproofAndUncounterabilityExpireAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CloudkinSeer());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new VeilOfSummer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player1, 0);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new VeilOfSummer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new Murder(), new Negate()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.assertInGraveyard(player1, "Cloudkin Seer");
        VeilOfSummer veil = (VeilOfSummer) gd.playerHands.get(player1.getId()).getFirst();
        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, veil.getId());

        harness.assertInGraveyard(player1, "Veil of Summer");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void blocksAbilitiesFromMulticoloredBlueSources() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.tap();
        Permanent kiora = harness.addToBattlefieldAndReturn(player2, new KioraBehemothBeckoner());
        kiora.setCounterCount(CounterType.LOYALTY, 7);
        harness.setHand(player1, List.of(new VeilOfSummer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    void drawsOnlyOneCardForMultipleQualifyingSpells() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new CloudkinSeer());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new CloudkinSeer());
        harness.setHand(player2, List.of(new Unsummon(), new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.castAndResolveInstant(player2, 0, first.getId());
        harness.castAndResolveInstant(player2, 0, second.getId());
        harness.setHand(player1, List.of(new VeilOfSummer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotDrawForQualifyingSpellCastOnPreviousTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CloudkinSeer());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.setHand(player1, List.of(new VeilOfSummer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new VeilOfSummer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
