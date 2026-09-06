package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DonRaphHardScience.class, GrizzlyBears.class, MindStone.class})
class DonRaphHardScienceTest extends BaseCardTest {

    @Test
    void nextNoncreatureSpellGetsAffinityForArtifacts() {
        harness.addToBattlefield(player1, new MindStone());
        var donRaph = harness.addToBattlefieldAndReturn(player1, new DonRaphHardScience());
        donRaph.setSummoningSick(false);
        harness.setHand(player1, List.of(new MindStone()));

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new MindStone());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
    }

    @Test
    void creatureSpellDoesNotConsumeAffinity() {
        harness.addToBattlefield(player1, new MindStone());
        var donRaph = harness.addToBattlefieldAndReturn(player1, new DonRaphHardScience());
        donRaph.setSummoningSick(false);
        harness.setHand(player1, List.of(new GrizzlyBears(), new MindStone()));

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.castArtifact(player1, 0);
    }

    @Test
    void noAffinityBeforeDonRaphAttacks() {
        harness.addToBattlefield(player1, new MindStone());
        harness.addToBattlefield(player1, new DonRaphHardScience());
        harness.setHand(player1, List.of(new MindStone()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
