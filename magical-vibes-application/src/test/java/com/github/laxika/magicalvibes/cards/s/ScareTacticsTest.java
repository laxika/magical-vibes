package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScareTactics.class, RagingGoblin.class, Spellbook.class})
class ScareTacticsTest extends BaseCardTest {

    @Test
    void boostsCreaturesYouControl() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        harness.setHand(player1, List.of(new ScareTactics()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(firstCreature.getEffectivePower()).isEqualTo(2);
        assertThat(firstCreature.getEffectiveToughness()).isEqualTo(1);
        assertThat(secondCreature.getEffectivePower()).isEqualTo(2);
        assertThat(secondCreature.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void doesNotBoostOpponentsCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());
        harness.setHand(player1, List.of(new ScareTactics()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(ownCreature.getEffectivePower()).isEqualTo(2);
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(1);
    }

    @Test
    void boostExpiresAtCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        harness.setHand(player1, List.of(new ScareTactics()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(creature.getEffectivePower()).isEqualTo(1);
        assertThat(creature.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void doesNotBoostNoncreaturesYouControl() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        harness.setHand(player1, List.of(new ScareTactics()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(noncreature.getPowerModifier()).isZero();
    }

    @Test
    void doesNotBoostCreaturesEnteringAfterResolution() {
        Permanent creatureBeforeResolution = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        harness.setHand(player1, List.of(new ScareTactics()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);
        Permanent creatureAfterResolution = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());

        assertThat(creatureBeforeResolution.getEffectivePower()).isEqualTo(2);
        assertThat(creatureAfterResolution.getEffectivePower()).isEqualTo(1);
    }
}
