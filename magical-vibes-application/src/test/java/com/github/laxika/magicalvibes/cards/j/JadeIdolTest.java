package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.CallousDeceiver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.ReachThroughMists;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JadeIdolTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a Spirit spell animates the idol as a 4/4 Spirit artifact creature")
    void spiritSpellAnimatesIdol() {
        Permanent idol = addIdol();
        prepareMainPhase();
        harness.setHand(player1, List.of(new CallousDeceiver()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(idol.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, idol)).isTrue();
        assertThat(gqs.isArtifact(idol)).isTrue();
        assertThat(gqs.getEffectivePower(gd, idol)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, idol)).isEqualTo(4);
        assertThat(idol.getTransientSubtypes()).contains(CardSubtype.SPIRIT);
    }

    @Test
    @DisplayName("Casting an Arcane spell animates the idol")
    void arcaneSpellAnimatesIdol() {
        Permanent idol = addIdol();
        prepareMainPhase();
        harness.setHand(player1, List.of(new ReachThroughMists()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(idol.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, idol)).isTrue();
    }

    @Test
    @DisplayName("Casting a spell that is neither Spirit nor Arcane does not animate the idol")
    void unrelatedSpellDoesNotAnimateIdol() {
        Permanent idol = addIdol();
        prepareMainPhase();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Jade Idol"));
        assertThat(idol.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, idol)).isFalse();
    }

    @Test
    @DisplayName("Animation wears off at end of turn")
    void animationWearsOff() {
        Permanent idol = addIdol();
        prepareMainPhase();
        harness.setHand(player1, List.of(new ReachThroughMists()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        assertThat(idol.isAnimatedUntilEndOfTurn()).isTrue();

        idol.resetModifiers();

        assertThat(idol.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, idol)).isFalse();
    }

    private Permanent addIdol() {
        return harness.addToBattlefieldAndReturn(player1, new JadeIdol());
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
