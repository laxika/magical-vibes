package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SphinxOfNewPrahvTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's spell targeting Sphinx of New Prahv costs {2} more")
    void opponentSpellTargetingSphinxCostsMore() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new SphinxOfNewPrahv());
        prepareOpponentCast(new LightningBolt(), ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, sphinx.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana to pay targeting tax");
    }

    @Test
    @DisplayName("Sphinx of New Prahv does not tax a spell targeting another permanent")
    void spellTargetingAnotherPermanentIsNotTaxed() {
        harness.addToBattlefield(player1, new SphinxOfNewPrahv());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareOpponentCast(new LightningBolt(), ManaColor.RED, 1);

        harness.castInstant(player2, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Sphinx of New Prahv does not tax its controller's spell")
    void ownSpellTargetingSphinxIsNotTaxed() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new SphinxOfNewPrahv());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, sphinx.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Opponent's activated ability targeting Sphinx of New Prahv is not taxed")
    void opponentActivatedAbilityTargetingSphinxIsNotTaxed() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new SphinxOfNewPrahv());
        Permanent spellcaster = new Permanent(new ZuranSpellcaster());
        spellcaster.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(spellcaster);
        harness.forceActivePlayer(player2);
        harness.forceStep(gd.currentStep);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, sphinx.getId());

        assertThat(gd.stack).hasSize(1);
    }

    private void prepareOpponentCast(LightningBolt spell, ManaColor color, int amount) {
        harness.forceActivePlayer(player2);
        harness.forceStep(gd.currentStep);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, color, amount);
    }
}
