package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.p.PouncingLynx;
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

@CardUsed({LoxodonSergeant.class, PouncingLynx.class})
class LoxodonSergeantTest extends BaseCardTest {

    @Test
    @DisplayName("Its ETB ability gives other creatures you control vigilance until end of turn")
    void givesOtherOwnCreaturesVigilance() {
        Permanent ownCreature = addCreatureReady(player1, new PouncingLynx());
        Permanent opponentCreature = addCreatureReady(player2, new PouncingLynx());

        castLoxodonSergeant();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Its ETB ability does not affect creatures entering later")
    void doesNotAffectLaterEnteringCreatures() {
        castLoxodonSergeant();

        harness.setHand(player1, List.of(new PouncingLynx()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent laterCreature = findPermanent(player1, "Pouncing Lynx");
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The granted vigilance wears off at end of turn")
    void vigilanceWearsOffAtEndOfTurn() {
        Permanent ownCreature = addCreatureReady(player1, new PouncingLynx());

        castLoxodonSergeant();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The grant is applied when the ETB trigger resolves")
    void grantsVigilanceToCreaturesPresentAtResolution() {
        Permanent existingCreature = addCreatureReady(player1, new PouncingLynx());
        harness.setHand(player1, List.of(new LoxodonSergeant()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, existingCreature, Keyword.VIGILANCE)).isFalse();
        Permanent arrivingCreature = addCreatureReady(player1, new PouncingLynx());
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, existingCreature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, arrivingCreature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Granting vigilance does not untap a tapped creature")
    void doesNotUntapCreatures() {
        Permanent creature = addCreatureReady(player1, new PouncingLynx());
        creature.setTapped(true);

        castLoxodonSergeant();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A creature granted vigilance attacks without tapping")
    void grantedVigilancePreventsTappingToAttack() {
        Permanent creature = addCreatureReady(player1, new PouncingLynx());
        castLoxodonSergeant();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));

        assertThat(creature.isTapped()).isFalse();
        assertThat(creature.isAttacking()).isTrue();
    }

    private void castLoxodonSergeant() {
        harness.setHand(player1, List.of(new LoxodonSergeant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
