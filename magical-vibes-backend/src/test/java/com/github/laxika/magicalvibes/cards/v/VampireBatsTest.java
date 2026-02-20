package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VampireBatsTest {

    private GameTestHarness harness;
    private Player player1;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        harness.skipMulligan();
        harness.clearMessages();
    }

    @Test
    @DisplayName("Vampire Bats has correct card properties and activated ability")
    void hasCorrectProperties() {
        VampireBats card = new VampireBats();

        assertThat(card.getName()).isEqualTo("Vampire Bats");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.getPower()).isEqualTo(0);
        assertThat(card.getToughness()).isEqualTo(1);
        assertThat(card.getKeywords()).containsExactly(Keyword.FLYING);

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{B}");
        assertThat(card.getActivatedAbilities().getFirst().getMaxActivationsPerTurn()).isEqualTo(2);
        assertThat(card.getActivatedAbilities().getFirst().getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getEffects().getFirst()).isInstanceOf(BoostSelfEffect.class);
    }

    @Test
    @DisplayName("Ability can be activated twice in one turn")
    void canActivateTwiceInOneTurn() {
        Permanent bats = addReadyVampireBats(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bats.getEffectivePower()).isEqualTo(2);
        assertThat(bats.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Third activation in same turn is rejected")
    void thirdActivationInSameTurnIsRejected() {
        addReadyVampireBats(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no more than 2 times each turn");
    }

    @Test
    @DisplayName("Activation limit resets on a new turn")
    void activationLimitResetsOnNewTurn() {
        addReadyVampireBats(player1);
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    private Permanent addReadyVampireBats(Player player) {
        VampireBats card = new VampireBats();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
