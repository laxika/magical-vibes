package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodsoakedChampion.class, GrizzlyBears.class})
class BloodsoakedChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Bloodsoaked Champion can't block")
    void cantBlock() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new BloodsoakedChampion());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Raid returns Bloodsoaked Champion from the graveyard to the battlefield")
    void raidReturnsFromGraveyard() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new BloodsoakedChampion()));

        declareAttackers(List.of(0));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bloodsoaked Champion");
    }

    @Test
    void raidReturnsOnlyTheActivatingCopyDuringCombat() {
        addCreatureReady(player1, new BloodsoakedChampion());
        BloodsoakedChampion source = new BloodsoakedChampion();
        BloodsoakedChampion other = new BloodsoakedChampion();
        harness.setGraveyard(player1, List.of(source, other));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        harness.activateGraveyardAbility(player1, 0);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(source, other);
        assertThat(countPermanents(player1, "Bloodsoaked Champion")).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(findPermanents(player1, "Bloodsoaked Champion"))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(source);
                    assertThat(permanent.isTapped()).isFalse();
                    assertThat(permanent.isAttacking()).isFalse();
                });
    }

    @Test
    void opponentsAttackDoesNotEnableRaid() {
        addCreatureReady(player1, new BloodsoakedChampion());
        harness.setGraveyard(player2, List.of(new BloodsoakedChampion()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        harness.ensurePriority(player2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked this turn");
        harness.assertInGraveyard(player2, "Bloodsoaked Champion");
    }

    @Test
    void raidRequiresBlackManaEvenAfterAttacking() {
        addCreatureReady(player1, new BloodsoakedChampion());
        harness.setGraveyard(player1, List.of(new BloodsoakedChampion()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Bloodsoaked Champion");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void declaringNoAttackersDoesNotEnableRaid() {
        addCreatureReady(player1, new BloodsoakedChampion());
        harness.setGraveyard(player1, List.of(new BloodsoakedChampion()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of()));

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked this turn");
    }

    @Test
    @DisplayName("Bloodsoaked Champion can't use its raid ability without attacking")
    void raidRequiresAttacking() {
        harness.setGraveyard(player1, List.of(new BloodsoakedChampion()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked this turn");
    }
}
