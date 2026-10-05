package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MasterOfPearls.class, GrizzlyBears.class})
class MasterOfPearlsTest extends BaseCardTest {

    @Test
    void morphsFaceDownAndBoostsOwnCreaturesWhenTurnedFaceUp() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MasterOfPearls()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent master = findPermanent(player1, "Master of Pearls");
        assertThat(master.isFaceDown()).isTrue();
        assertThat(master.getEffectivePower()).isEqualTo(2);
        assertThat(master.getEffectiveToughness()).isEqualTo(2);

        harness.addMana(player1, ManaColor.WHITE, 5);
        int masterIndex = gd.playerBattlefields.get(player1.getId()).indexOf(master);
        harness.turnFaceUp(player1, masterIndex);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").getEffectivePower()).isEqualTo(4);
        assertThat(findPermanent(player1, "Grizzly Bears").getEffectiveToughness()).isEqualTo(4);
        assertThat(master.isFaceDown()).isFalse();
        assertThat(master.getEffectivePower()).isEqualTo(4);
        assertThat(master.getEffectiveToughness()).isEqualTo(4);
        assertThat(findPermanent(player2, "Grizzly Bears").getEffectivePower()).isEqualTo(2);
        assertThat(findPermanent(player2, "Grizzly Bears").getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void castingFaceUpDoesNotBoostCreatures() {
        Permanent other = harness.addToBattlefieldAndReturn(player1, new MasterOfPearls());
        harness.setHand(player1, List.of(new MasterOfPearls()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Master of Pearls")).hasSize(2);
        assertThat(other.getEffectivePower()).isEqualTo(2);
        assertThat(other.getEffectiveToughness()).isEqualTo(2);
        assertThat(findPermanents(player1, "Master of Pearls").get(1).getEffectivePower()).isEqualTo(2);
        assertThat(findPermanents(player1, "Master of Pearls").get(1).getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void boostAppliesAtResolutionAndExpiresAtEndOfTurn() {
        harness.setHand(player1, List.of(new MasterOfPearls()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        Permanent master = findPermanent(player1, "Master of Pearls");

        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(master));
        assertThat(master.isFaceDown()).isFalse();
        assertThat(master.getEffectivePower()).isEqualTo(2);
        assertThat(master.getEffectiveToughness()).isEqualTo(2);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new MasterOfPearls());
        resolveAllTriggers();

        assertThat(master.getEffectivePower()).isEqualTo(4);
        assertThat(master.getEffectiveToughness()).isEqualTo(4);
        assertThat(beforeResolution.getEffectivePower()).isEqualTo(4);
        assertThat(beforeResolution.getEffectiveToughness()).isEqualTo(4);
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new MasterOfPearls());
        assertThat(afterResolution.getEffectivePower()).isEqualTo(2);
        assertThat(afterResolution.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(master.getEffectivePower()).isEqualTo(2);
        assertThat(master.getEffectiveToughness()).isEqualTo(2);
        assertThat(beforeResolution.getEffectivePower()).isEqualTo(2);
        assertThat(beforeResolution.getEffectiveToughness()).isEqualTo(2);
    }
}
