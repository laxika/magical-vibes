package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HerosBlade;
import com.github.laxika.magicalvibes.cards.w.WardscaleDragon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OjutaiSoulOfWinter.class, Forest.class, GrizzlyBears.class, HerosBlade.class, WardscaleDragon.class})
class OjutaiSoulOfWinterTest extends BaseCardTest {

    @Test
    @DisplayName("An attacking Dragon taps and locks an opponent's nonland permanent")
    void attackingDragonTapsAndLocksTarget() {
        addCreatureReady(player1, new OjutaiSoulOfWinter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("A non-Dragon attacker does not trigger Ojutai")
    void nonDragonDoesNotTrigger() {
        addCreatureReady(player1, new OjutaiSoulOfWinter());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        assertThat(target.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("An opponent's land cannot be chosen as the target")
    void opponentLandIsNotALegalTarget() {
        addCreatureReady(player1, new OjutaiSoulOfWinter());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(land.isTapped()).isFalse();
        assertThat(land.getSkipUntapCount()).isZero();
    }

    @Test
    void anotherDragonTriggersWhileOjutaiStaysBack() {
        Permanent ojutai = addCreatureReady(player1, new OjutaiSoulOfWinter());
        addCreatureReady(player1, new WardscaleDragon());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HerosBlade());

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(ojutai.isAttacking()).isFalse();
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    void eachAttackingDragonCanTapADifferentPermanent() {
        addCreatureReady(player1, new OjutaiSoulOfWinter());
        addCreatureReady(player1, new WardscaleDragon());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new HerosBlade());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HerosBlade());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(first.getSkipUntapCount()).isEqualTo(1);
        assertThat(second.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    void multipleTriggersOnSameTargetOnlySkipOneUntapStep() {
        addCreatureReady(player1, new OjutaiSoulOfWinter());
        addCreatureReady(player1, new WardscaleDragon());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HerosBlade());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void alreadyTappedPermanentIsLockedForItsControllersNextUntapOnly() {
        addCreatureReady(player1, new OjutaiSoulOfWinter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HerosBlade());
        target.setTapped(true);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isEqualTo(1);
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getSkipUntapCount()).isZero();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void opponentsDragonDoesNotTriggerOjutai() {
        addCreatureReady(player1, new OjutaiSoulOfWinter());
        addCreatureReady(player2, new WardscaleDragon());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HerosBlade());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(target.getSkipUntapCount()).isZero();
    }

    @Test
    void ownNonlandPermanentIsNotALegalTarget() {
        addCreatureReady(player1, new OjutaiSoulOfWinter());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new HerosBlade());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(artifact.isTapped()).isFalse();
        assertThat(artifact.getSkipUntapCount()).isZero();
    }

    @Test
    void triggerResolvesAfterOjutaiLeavesBattlefield() {
        Permanent ojutai = addCreatureReady(player1, new OjutaiSoulOfWinter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HerosBlade());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(ojutai);
        gd.playerGraveyards.get(player1.getId()).add(ojutai.getCard());
        resolveAllTriggers();

        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void targetBecomingControlledByOjutaisControllerIsIllegalAtResolution() {
        addCreatureReady(player1, new OjutaiSoulOfWinter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HerosBlade());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        resolveAllTriggers();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getSkipUntapCount()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
