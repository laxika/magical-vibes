package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.p.PlatedSliver;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CryptSliver.class, PlatedSliver.class, FugitiveWizard.class})
class CryptSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Crypt Sliver can regenerate itself")
    void regeneratesItself() {
        Permanent cryptSliver = addCreatureReady(player1, new CryptSliver());

        activateRegeneration(player1, 0, cryptSliver);

        assertThat(cryptSliver.getRegenerationShield()).isEqualTo(1);
        assertThat(cryptSliver.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Crypt Sliver grants its tap ability to Slivers on either battlefield")
    void grantsAbilityToSliversOnEitherBattlefield() {
        Permanent cryptSliver = addCreatureReady(player1, new CryptSliver());
        Permanent opposingSliver = addCreatureReady(player2, new PlatedSliver());

        activateRegeneration(player2, 0, cryptSliver);

        assertThat(cryptSliver.getRegenerationShield()).isEqualTo(1);
        assertThat(opposingSliver.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Another Sliver I control can use Crypt Sliver's ability")
    void grantsAbilityToAnotherOwnSliver() {
        Permanent cryptSliver = addCreatureReady(player1, new CryptSliver());
        Permanent ownSliver = addCreatureReady(player1, new PlatedSliver());

        activateRegeneration(player1, 1, cryptSliver);

        assertThat(cryptSliver.getRegenerationShield()).isEqualTo(1);
        assertThat(ownSliver.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Slivers lose Crypt Sliver's granted ability when it leaves the battlefield")
    void losesGrantedAbilityWhenSourceLeaves() {
        Permanent cryptSliver = addCreatureReady(player1, new CryptSliver());
        Permanent ownSliver = addCreatureReady(player1, new PlatedSliver());

        assertThat(gs.getEffectiveActivatedAbilities(gd, ownSliver)).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(cryptSliver);

        assertThat(gs.getEffectiveActivatedAbilities(gd, ownSliver)).isEmpty();
    }

    @Test
    @DisplayName("Crypt Sliver's ability cannot target a non-Sliver")
    void cannotTargetNonSliver() {
        addCreatureReady(player1, new CryptSliver());
        Permanent wizard = addCreatureReady(player2, new FugitiveWizard());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, wizard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void activateRegeneration(com.github.laxika.magicalvibes.model.Player player,
                                      int sourceIndex, Permanent target) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player, sourceIndex, null, target.getId());
        harness.passBothPriorities();
    }
}
