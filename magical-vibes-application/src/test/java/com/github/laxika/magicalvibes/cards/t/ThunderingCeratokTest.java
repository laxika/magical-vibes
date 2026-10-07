package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
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

@CardUsed({ThunderingCeratok.class, PrimordialWurm.class})
class ThunderingCeratokTest extends BaseCardTest {

    @Test
    @DisplayName("ETB grants other creatures you control trample until end of turn")
    void etbGrantsOtherOwnCreaturesTrampleUntilEndOfTurn() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());
        harness.setHand(player1, List.of(new ThunderingCeratok()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentBear, Keyword.TRAMPLE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Trample recipients are determined when the ETB trigger resolves")
    void grantsTrampleToCreaturesPresentAtResolutionOnly() {
        Permanent existingCreature = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        harness.setHand(player1, List.of(new ThunderingCeratok()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, existingCreature, Keyword.TRAMPLE)).isFalse();
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        harness.passBothPriorities();

        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        assertThat(gqs.hasKeyword(gd, existingCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("ETB resolves with no other creatures and does not grant trample to later arrivals")
    void resolvesWithoutOtherCreatures() {
        harness.setHand(player1, List.of(new ThunderingCeratok()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Thundering Ceratok");
        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.TRAMPLE)).isFalse();
    }
}
