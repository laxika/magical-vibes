package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FieryImpulse;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.RunedServitor;
import com.github.laxika.magicalvibes.cards.w.WildInstincts;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SendToSleep.class, GrizzlyBears.class, LightningBolt.class, Shock.class, Mountain.class,
        RunedServitor.class, FieryImpulse.class, WildInstincts.class, Disperse.class})
class SendToSleepTest extends BaseCardTest {

    private void castSendToSleep(List<UUID> targets) {
        harness.setHand(player1, List.of(new SendToSleep()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, targets);
    }

    private void fillGraveyardWithSpells() {
        harness.setGraveyard(player1, List.of(new LightningBolt(), new Shock()));
    }

    @Test
    @DisplayName("Taps both target creatures")
    void tapsTwoCreatures() {
        Permanent a = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent b = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSendToSleep(List.of(a.getId(), b.getId()));

        assertThat(a.isTapped()).isTrue();
        assertThat(b.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Without spell mastery the tapped creatures untap normally")
    void noUntapLockWithoutSpellMastery() {
        Permanent a = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent b = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSendToSleep(List.of(a.getId(), b.getId()));

        assertThat(a.getSkipUntapCount()).isZero();
        assertThat(b.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Spell mastery locks both creatures out of their next untap step")
    void spellMasteryLocksUntap() {
        fillGraveyardWithSpells();
        Permanent a = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent b = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSendToSleep(List.of(a.getId(), b.getId()));

        assertThat(a.isTapped()).isTrue();
        assertThat(b.isTapped()).isTrue();
        assertThat(a.getSkipUntapCount()).isEqualTo(1);
        assertThat(b.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("A single instant in the graveyard is not enough for spell mastery")
    void oneInstantIsNotSpellMastery() {
        harness.setGraveyard(player1, List.of(new LightningBolt()));
        Permanent a = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSendToSleep(List.of(a.getId()));

        assertThat(a.isTapped()).isTrue();
        assertThat(a.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("May target only a single creature (up to two)")
    void tapsOneCreature() {
        fillGraveyardWithSpells();
        Permanent a = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSendToSleep(List.of(a.getId()));

        assertThat(a.isTapped()).isTrue();
        assertThat(a.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetLand() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new SendToSleep()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID mountainId = mountain.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(mountainId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canChooseNoTargetsWithSpellMastery() {
        harness.setGraveyard(player1, List.of(new FieryImpulse(), new WildInstincts()));
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RunedServitor());

        castSendToSleep(List.of());

        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.getSkipUntapCount()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof SendToSleep);
    }

    @Test
    void twoSorceriesEnableSpellMasteryForAlreadyTappedCreature() {
        harness.setGraveyard(player1, List.of(new WildInstincts(), new WildInstincts()));
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RunedServitor());
        creature.tap();

        castSendToSleep(List.of(creature.getId()));
        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void eachTargetsControllerSkipsOnlyTheirNextUntapStep() {
        harness.setGraveyard(player1, List.of(new FieryImpulse(), new WildInstincts()));
        Permanent own = harness.addToBattlefieldAndReturn(player1, new RunedServitor());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new RunedServitor());

        castSendToSleep(List.of(own.getId(), opposing.getId()));
        harness.performUntapStep(player2);
        assertThat(opposing.isTapped()).isTrue();
        assertThat(own.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(opposing.isTapped()).isFalse();
        assertThat(own.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(own.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(own.isTapped()).isFalse();
    }

    @Test
    void otherCardTypesAndOpponentsGraveyardDoNotEnableSpellMastery() {
        harness.setGraveyard(player1, List.of(new FieryImpulse(), new RunedServitor(), new Mountain()));
        harness.setGraveyard(player2, List.of(new FieryImpulse(), new WildInstincts()));
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RunedServitor());

        castSendToSleep(List.of(creature.getId()));
        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void spellMasteryIsCheckedAtResolutionRatherThanCasting() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RunedServitor());
        harness.setHand(player1, List.of(new SendToSleep()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, List.of(creature.getId()));

        harness.setGraveyard(player1, List.of(new FieryImpulse(), new WildInstincts()));
        harness.passBothPriorities();
        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void losingSpellMasteryBeforeResolutionAllowsNormalUntap() {
        harness.setGraveyard(player1, List.of(new FieryImpulse(), new WildInstincts()));
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RunedServitor());
        harness.setHand(player1, List.of(new SendToSleep()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, List.of(creature.getId()));

        harness.setGraveyard(player1, List.of(new FieryImpulse()));
        harness.passBothPriorities();
        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void repeatedSpellMasterySpellsDoNotSkipTwoUntapSteps() {
        harness.setGraveyard(player1, List.of(new FieryImpulse(), new WildInstincts()));
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RunedServitor());

        castSendToSleep(List.of(creature.getId()));
        castSendToSleep(List.of(creature.getId()));
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void remainingLegalTargetIsTappedAndLockedWhenOtherTargetLeaves() {
        harness.setGraveyard(player1, List.of(new FieryImpulse(), new WildInstincts()));
        Permanent returned = harness.addToBattlefieldAndReturn(player2, new RunedServitor());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new RunedServitor());
        harness.setHand(player1, List.of(new SendToSleep()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, List.of(returned.getId(), remaining.getId()));

        harness.setHand(player2, List.of(new Disperse()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, returned.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(returned);
        assertThat(remaining.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(remaining.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(remaining.isTapped()).isFalse();
    }
}
