package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GlacialStalker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeaverOfLies.class, GlacialStalker.class, GrizzlyBears.class})
class WeaverOfLiesTest extends BaseCardTest {

    @Test
    void turnsAnyNumberOfOtherMorphCreaturesFaceDown() {
        Permanent ownMorphCreature = harness.addToBattlefieldAndReturn(player1, new GlacialStalker());
        Permanent opposingMorphCreature = harness.addToBattlefieldAndReturn(player2, new GlacialStalker());
        Permanent ordinaryCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new WeaverOfLies()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent weaver = findPermanent(player1, "Weaver of Lies");
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(weaver));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownMorphCreature.getId(), opposingMorphCreature.getId())
                .doesNotContain(weaver.getId(), ordinaryCreature.getId());

        harness.handlePermanentChosen(player1, ownMorphCreature.getId());
        harness.handlePermanentChosen(player1, opposingMorphCreature.getId());
        harness.passBothPriorities();

        assertThat(ownMorphCreature.isFaceDown()).isTrue();
        assertThat(opposingMorphCreature.isFaceDown()).isTrue();
        assertThat(ordinaryCreature.isFaceDown()).isFalse();
        assertThat(weaver.isFaceDown()).isFalse();
        assertThat(ownMorphCreature.getEffectivePower()).isEqualTo(2);
        assertThat(ownMorphCreature.getEffectiveToughness()).isEqualTo(2);
    }
}
