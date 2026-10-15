package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.Agoraphobia;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PredatorsRapport.class, GrizzlyBears.class, HillGiant.class,
        Agoraphobia.class, GiantGrowth.class, Unsummon.class})
class PredatorsRapportTest extends BaseCardTest {

    private void prepareCast() {
        harness.setHand(player1, List.of(new PredatorsRapport()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Gains life equal to the target creature's power plus its toughness")
    void gainsLifeEqualToPowerPlusToughness() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        prepareCast();

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Reads the specific chosen creature's stats")
    void readsChosenCreatureStats() {
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setLife(player1, 20);
        prepareCast();

        harness.castAndResolveInstant(player1, 0, giant.getId());

        harness.assertLife(player1, 26);
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Negative power reduces a positive power-plus-toughness sum")
    void negativePowerReducesLifeGain() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Agoraphobia());
        aura.setAttachedTo(giant.getId());
        harness.setLife(player1, 20);
        prepareCast();

        harness.castAndResolveInstant(player1, 0, giant.getId());

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("A negative power-plus-toughness sum gains no life")
    void negativeSumGainsNoLife() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Agoraphobia());
        aura.setAttachedTo(bears.getId());
        harness.setLife(player1, 20);
        prepareCast();

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Uses power and toughness after a response changes them")
    void usesStatsAtResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new PredatorsRapport(), new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, bears.getId());
        harness.castAndResolveInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 30);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Gains no life if the target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        prepareCast();
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Predator's Rapport");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent you control")
    void cannotTargetNoncreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Agoraphobia());
        aura.setAttachedTo(bears.getId());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, aura.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
