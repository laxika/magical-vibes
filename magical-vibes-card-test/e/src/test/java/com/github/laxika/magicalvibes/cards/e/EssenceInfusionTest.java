package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.l.LetterOfAcceptance;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EssenceInfusion.class, EagerFirstYear.class, LetterOfAcceptance.class, Expel.class})
class EssenceInfusionTest extends BaseCardTest {

    @Test
    @DisplayName("Puts two +1/+1 counters on and grants lifelink to target creature")
    void putsCountersAndGrantsLifelink() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EagerFirstYear());
        harness.setHand(player1, List.of(new EssenceInfusion()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Counters persist while lifelink wears off at cleanup")
    void countersPersistAndLifelinkExpires() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EagerFirstYear());
        harness.setHand(player1, List.of(new EssenceInfusion()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LetterOfAcceptance());
        harness.setHand(player1, List.of(new EssenceInfusion()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Lifelink gains life for the creature's controller, even when an opponent cast the spell")
    void lifelinkBenefitsCreatureController() {
        Permanent target = addCreatureReady(player2, new EagerFirstYear());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new EssenceInfusion()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, target.getId());
        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 24);
    }

    @Test
    @DisplayName("Does nothing when the target is exiled before resolution")
    void targetExiledInResponse() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EagerFirstYear());
        target.tap();
        harness.setHand(player1, List.of(new EssenceInfusion(), new Expel()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Eager First-Year");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isFalse();
        harness.assertInGraveyard(player1, "Essence Infusion");
        assertThat(gd.stack).isEmpty();
    }
}
