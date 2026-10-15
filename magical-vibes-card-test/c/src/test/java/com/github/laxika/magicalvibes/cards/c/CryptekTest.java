package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Cryptek.class, Ornithopter.class, LightningBolt.class, GrizzlyBears.class, Unsummon.class, HowlingMine.class})
class CryptekTest extends BaseCardTest {

    @Test
    void returnsAnotherArtifactCreatureTappedWhenItDiesThisTurn() {
        Permanent cryptek = addCreatureReady(player1, new Cryptek());
        Permanent ornithopter = addCreatureReady(player1, new Ornithopter());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.activateAbility(player1, 0, null, ornithopter.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, ornithopter.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Ornithopter"))
                .singleElement()
                .matches(Permanent::isTapped);
        assertThat(cryptek.isTapped()).isTrue();
    }

    @Test
    void cannotTargetSelfOrNonArtifactCreature() {
        Permanent cryptek = addCreatureReady(player1, new Cryptek());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, cryptek.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetOpponentsArtifactCreature() {
        addCreatureReady(player1, new Cryptek());
        Permanent target = addCreatureReady(player2, new Ornithopter());
        prepareActivation();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetNoncreatureArtifact() {
        addCreatureReady(player1, new Cryptek());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HowlingMine());
        prepareActivation();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsCreatureEvenAfterCryptekDies() {
        Permanent cryptek = addCreatureReady(player1, new Cryptek());
        Permanent target = addCreatureReady(player1, new Ornithopter());
        prepareActivation();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        bolt(cryptek);
        bolt(target);

        assertThat(findPermanents(player1, "Cryptek")).isEmpty();
        assertThat(findPermanent(player1, "Ornithopter").isTapped()).isTrue();
    }

    @Test
    void returnsOpponentsOwnedCreatureUnderAbilityControllersControl() {
        addCreatureReady(player1, new Cryptek());
        Ornithopter card = new Ornithopter();
        card.setOwnerId(player2.getId());
        Permanent target = addCreatureReady(player1, card);
        gd.stolenCreatures.put(target.getId(), player2.getId());
        recordControlEffect(target, player1);
        prepareActivation();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        bolt(target);

        assertThat(findPermanent(player1, "Ornithopter").isTapped()).isTrue();
        assertThat(findPermanents(player2, "Ornithopter")).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(card);
    }

    @Test
    void doesNotReturnCreatureOnSecondDeathWithoutAnotherActivation() {
        addCreatureReady(player1, new Cryptek());
        Permanent target = addCreatureReady(player1, new Ornithopter());
        prepareActivation();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        bolt(target);
        Permanent returned = findPermanent(player1, "Ornithopter");
        bolt(returned);

        assertThat(findPermanents(player1, "Ornithopter")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(returned.getCard());
    }

    @Test
    void doesNotProtectNewObjectAfterCreatureIsBouncedAndRecast() {
        addCreatureReady(player1, new Cryptek());
        Permanent target = addCreatureReady(player1, new Ornithopter());
        prepareActivation();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).contains(target.getCard());
        harness.castCreature(player1, gd.playerHands.get(player1.getId()).indexOf(target.getCard()));
        resolveAllTriggers();
        Permanent recast = findPermanent(player1, "Ornithopter");

        bolt(recast);

        assertThat(findPermanents(player1, "Ornithopter")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(recast.getCard());
    }

    @Test
    void doesNotReturnTargetThatDiesBeforeActivationResolves() {
        addCreatureReady(player1, new Cryptek());
        Permanent target = addCreatureReady(player1, new Ornithopter());
        prepareActivation();
        harness.activateAbility(player1, 0, null, target.getId());

        bolt(target);

        assertThat(findPermanents(player1, "Ornithopter")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target.getCard());
    }

    @Test
    void protectionExpiresAtEndOfTurn() {
        addCreatureReady(player1, new Cryptek());
        Permanent target = addCreatureReady(player1, new Ornithopter());
        prepareActivation();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        bolt(target);

        assertThat(findPermanents(player1, "Ornithopter")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target.getCard());
    }

    private void prepareActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 2);
    }

    private void bolt(Permanent target) {
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();
    }
    private void recordControlEffect(Permanent permanent, com.github.laxika.magicalvibes.model.Player controller) {
        gd.addFloatingEffect(new FloatingContinuousEffect(
                java.util.UUID.randomUUID(), "Control setup", null, controller.getId(),
                new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                permanent.getId(), null, null, EffectDuration.PERMANENT, 0));
    }
}
