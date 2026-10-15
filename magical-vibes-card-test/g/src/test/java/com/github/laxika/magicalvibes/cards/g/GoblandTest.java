package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Gobland.class, GrizzlyBears.class})
class GoblandTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Gobland produces one red mana")
    void tappingProducesRedMana() {
        Permanent gobland = addCreatureReady(player1, new Gobland());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gobland.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Summoning-sick Gobland cannot tap for mana")
    void summoningSickCannotTap() {
        harness.addToBattlefield(player1, new Gobland());

        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Gobland cannot block")
    void cannotBlock() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent gobland = addCreatureReady(player2, new Gobland());

        assertThat(bls.canBlockAttacker(gd, gobland, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("Gobland is played directly as a land and cannot immediately tap for mana")
    void playedAsLandWithoutUsingStack() {
        harness.setHand(player1, List.of(new Gobland()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(findPermanent(player1, "Gobland").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Playing Gobland consumes the normal land play")
    void consumesLandPlay() {
        harness.setHand(player1, List.of(new Gobland(), new Gobland()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Gobland can tap for mana only after its controller's next turn begins")
    void canTapAfterControllersNextUntap() {
        harness.setHand(player1, List.of(new Gobland()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);

        harness.performUntapStep(player2);
        assertThatThrownBy(() -> harness.tapPermanent(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        harness.performUntapStep(player1);
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(findPermanent(player1, "Gobland").isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Gobland can attack and deal combat damage without producing mana")
    void attacksWithoutProducingMana() {
        addCreatureReady(player1, new Gobland());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 18);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Summoning-sick Gobland cannot attack")
    void summoningSickCannotAttack() {
        harness.addToBattlefield(player1, new Gobland());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player2, 20);
        assertThat(findPermanent(player1, "Gobland").isTapped()).isFalse();
    }
}
