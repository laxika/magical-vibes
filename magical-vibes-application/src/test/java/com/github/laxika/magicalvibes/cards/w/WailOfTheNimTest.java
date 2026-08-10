package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WailOfTheNimTest extends BaseCardTest {

    @Test
    @DisplayName("Regeneration mode gives shields to your creatures only")
    void regeneratesOwnCreaturesOnly() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(new int[]{0}, 1);

        assertThat(ownCreature.getRegenerationShield()).isEqualTo(1);
        assertThat(opponentCreature.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Damage mode damages each creature and each player")
    void damagesEachCreatureAndPlayer() {
        Permanent survivingCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent dyingCreature = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        cast(new int[]{1}, 1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(survivingCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(dyingCreature);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Entwine pays an additional black mana and resolves both modes")
    void entwineResolvesBothModes() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new FugitiveWizard());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        cast(new int[]{0, 1}, 2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Entwine requires the additional black mana")
    void entwineRequiresAdditionalMana() {
        harness.setHand(player1, List.of(new WailOfTheNim()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modes, int blackMana) {
        harness.setHand(player1, List.of(new WailOfTheNim()));
        harness.addMana(player1, ManaColor.BLACK, blackMana);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castModalInstantWithModes(player1, 0, 1, 2, modes, List.of());
        harness.passBothPriorities();
    }
}
