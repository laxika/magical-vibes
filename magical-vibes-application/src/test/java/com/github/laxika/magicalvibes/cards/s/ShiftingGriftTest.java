package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShiftingGrift.class, GrizzlyBears.class, HillGiant.class, FountainOfYouth.class,
        DarksteelCitadel.class, AngelicChorus.class, GloriousAnthem.class})
class ShiftingGriftTest extends BaseCardTest {

    @Test
    @DisplayName("Exchanges control for each selected permanent type")
    void exchangesControlForEachSelectedType() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new DarksteelCitadel());
        Permanent ownEnchantment = harness.addToBattlefieldAndReturn(player1, new AngelicChorus());
        Permanent opponentEnchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        cast(new int[]{0, 1, 2}, List.of(
                ownCreature.getId(), opponentCreature.getId(),
                ownArtifact.getId(), opponentArtifact.getId(),
                ownEnchantment.getId(), opponentEnchantment.getId()), 6);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(opponentCreature, opponentArtifact, opponentEnchantment);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(ownCreature, ownArtifact, ownEnchantment);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Rejects choosing the same permanent twice within one exchange mode")
    void rejectsDuplicateTargetsWithinOneMode() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> cast(new int[]{0}, List.of(creature.getId(), creature.getId()), 4))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, List<java.util.UUID> targets, int totalMana) {
        harness.setHand(player1, List.of(new ShiftingGrift()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, totalMana - 2);
        harness.castModalSorceryWithModes(player1, 0, 1, 3, modes, targets, null);
        harness.passBothPriorities();
    }
}
