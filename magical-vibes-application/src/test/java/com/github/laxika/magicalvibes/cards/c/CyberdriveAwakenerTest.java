package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
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

@CardUsed({CyberdriveAwakener.class, AccordersShield.class, Memnite.class, GrizzlyBears.class})
class CyberdriveAwakenerTest extends BaseCardTest {

    @Test
    @DisplayName("Gives other artifact creatures you control flying")
    void givesOtherArtifactCreaturesFlying() {
        harness.addToBattlefield(player1, new CyberdriveAwakener());
        Permanent ownArtifactCreature = addCreatureReady(player1, new Memnite());
        Permanent opponentArtifactCreature = addCreatureReady(player2, new Memnite());
        Permanent ownNonArtifactCreature = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, ownArtifactCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentArtifactCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownNonArtifactCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Enters by animating each noncreature artifact you control as a 4/4")
    void animatesOwnNoncreatureArtifacts() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        Permanent ownCreature = addCreatureReady(player1, new Memnite());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new AccordersShield());

        castCyberdrive();

        assertThat(gqs.isCreature(gd, ownArtifact)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownArtifact)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownArtifact)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, opponentArtifact)).isFalse();
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("The artifact animation wears off at end of turn")
    void animationWearsOffAtEndOfTurn() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new AccordersShield());

        castCyberdrive();
        assertThat(gqs.isCreature(gd, ownArtifact)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, ownArtifact)).isFalse();
    }

    private void castCyberdrive() {
        harness.setHand(player1, List.of(new CyberdriveAwakener()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
