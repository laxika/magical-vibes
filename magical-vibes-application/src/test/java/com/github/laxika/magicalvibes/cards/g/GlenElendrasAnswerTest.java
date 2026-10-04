package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BelenonWarAnthem;
import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.cards.i.InvasionOfBelenon;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.Panopticon;
import com.github.laxika.magicalvibes.cards.s.SpiderPunk;
import com.github.laxika.magicalvibes.cards.v.VolcanicFallout;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarDieResult;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlenElendrasAnswer.class, GrizzlyBears.class, IcyManipulator.class,
        LightningBolt.class, VolcanicFallout.class, Counterspell.class, ElvishVisionary.class,
        InvasionOfBelenon.class, BelenonWarAnthem.class, SpiderPunk.class, Panopticon.class})
class GlenElendrasAnswerTest extends BaseCardTest {

    @Test
    @DisplayName("Counters every opponent spell and creates one Faerie per countered spell")
    void countersOpponentSpellsAndCreatesFaeries() {
        GrizzlyBears ownSpell = new GrizzlyBears();
        harness.setHand(player1, List.of(ownSpell, new GlenElendrasAnswer()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);

        LightningBolt firstOpponentSpell = new LightningBolt();
        LightningBolt secondOpponentSpell = new LightningBolt();
        harness.setHand(player2, List.of(firstOpponentSpell, secondOpponentSpell));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, player1.getId());
        harness.castInstant(player2, 0, player1.getId());

        harness.castAndResolveInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Lightning Bolt", "Lightning Bolt");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(ownSpell.getId()));
        assertThat(faerieTokens(player1)).hasSize(2);
    }

    @Test
    @DisplayName("Counters opponent activated abilities and creates a Faerie")
    void countersOpponentAbility() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new IcyManipulator());
        Permanent icyManipulator = findPermanent(player2, "Icy Manipulator");
        icyManipulator.setSummoningSick(false);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(icyManipulator), null,
                harness.getPermanentId(player1, "Grizzly Bears"));

        harness.setHand(player1, List.of(new GlenElendrasAnswer()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player1, 0);

        assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isFalse();
        assertThat(faerieTokens(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Does not count an uncounterable spell")
    void doesNotCountUncounterableSpell() {
        harness.setHand(player2, List.of(new VolcanicFallout()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castInstant(player2, 0);

        harness.setHand(player1, List.of(new GlenElendrasAnswer()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(faerieTokens(player1)).isEmpty();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    void countersBattleSpells() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new InvasionOfBelenon()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.castSorcery(player2, 0);
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new GlenElendrasAnswer()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Invasion of Belenon");
        assertThat(faerieTokens(player1)).hasSize(1);
    }

    @Test
    void doesNotCounterAbilitiesProtectedBySpiderPunk() {
        harness.addToBattlefield(player1, new SpiderPunk());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new IcyManipulator());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, bear.getId());

        harness.setHand(player1, List.of(new GlenElendrasAnswer()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(faerieTokens(player1)).isEmpty();
        harness.passBothPriorities();
        assertThat(bear.isTapped()).isTrue();
    }

    @Test
    void countersOpponentTriggeredAbility() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new ElvishVisionary()));
        harness.setLibrary(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new GlenElendrasAnswer()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        harness.assertOnBattlefield(player2, "Elvish Visionary");
        assertThat(faerieTokens(player1)).hasSize(1);
    }

    @Test
    void preservesOwnTriggeredAbility() {
        harness.setHand(player1, List.of(new ElvishVisionary(), new GlenElendrasAnswer()));
        harness.setLibrary(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(faerieTokens(player1)).isEmpty();
        harness.passBothPriorities();
        harness.assertInHand(player1, "Lightning Bolt");
    }

    @Test
    void cannotBeCountered() {
        GlenElendrasAnswer answer = new GlenElendrasAnswer();
        harness.setHand(player1, List.of(answer));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castInstant(player1, 0);
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player2, 0, answer.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(answer.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(faerieTokens(player1)).isEmpty();
    }

    @Test
    void countsSpellsAndAbilitiesTogetherAndCreatesCorrectTokens() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new IcyManipulator());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, bear.getId());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.setHand(player1, List.of(new GlenElendrasAnswer()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(bear.isTapped()).isFalse();
        harness.assertLife(player1, 20);
        assertThat(faerieTokens(player1)).hasSize(2).allSatisfy(token -> {
            assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLUE, CardColor.BLACK);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.FAERIE);
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        });
    }

    @Test
    void countersOpponentPlaneswalkingTriggerWithoutACardSource() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gd.planechase = new PlanechaseState();
        PlanarObject plane = new PlanarObject(new Panopticon(), gd.nextTimestamp());
        gd.planechase.faceUp.add(plane);
        gd.planechase.deck.add(new Panopticon());
        PlanechaseService planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        harness.inMutationScope(() -> planar.completeRoll(gd, player2.getId(), PlanarDieResult.PLANESWALKER));
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new GlenElendrasAnswer()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(faerieTokens(player1)).hasSize(1);
        assertThat(gd.planechase.faceUp).containsExactly(plane);
    }

    @Test
    void countersOpponentChaosAbilityUsingItsStackIdentity() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player2.getId();
        PlanarObject plane = new PlanarObject(new Panopticon(), gd.nextTimestamp());
        gd.planechase.faceUp.add(plane);
        PlanechaseService planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        harness.inMutationScope(() -> planar.trigger(gd, plane, EffectSlot.CHAOS_TRIGGERED, player2.getId()));
        assertThat(gd.stack).hasSize(1);
        int handSize = gd.playerHands.get(player2.getId()).size();

        harness.setHand(player1, List.of(new GlenElendrasAnswer()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSize);
        assertThat(faerieTokens(player1)).hasSize(1);
    }

    private List<Permanent> faerieTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Faerie"))
                .toList();
    }
}
