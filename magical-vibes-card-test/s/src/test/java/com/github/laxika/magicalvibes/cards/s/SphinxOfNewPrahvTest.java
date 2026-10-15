package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SphinxOfNewPrahv.class, GrizzlyBears.class, LightningBolt.class, ZuranSpellcaster.class,
        TurnToFrog.class})
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
        Permanent spellcaster = harness.addToBattlefieldAndReturn(player2, new ZuranSpellcaster());
        spellcaster.setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.forceStep(gd.currentStep);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, sphinx.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Opponent can pay the surcharge and resolve a spell targeting the Sphinx")
    void opponentCanPayTargetingSurcharge() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new SphinxOfNewPrahv());
        harness.addToBattlefield(player1, new SphinxOfNewPrahv());
        prepareOpponentCast(new LightningBolt(), ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, sphinx.getId());

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1)
                .noneMatch(permanent -> permanent.getId().equals(sphinx.getId()));
        harness.assertInGraveyard(player1, "Sphinx of New Prahv");
    }

    @Test
    @DisplayName("Sphinx's targeting surcharge stops applying when it loses all abilities")
    void losingAllAbilitiesRemovesTargetingSurcharge() {
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new SphinxOfNewPrahv());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, sphinx.getId());
        prepareOpponentCast(new LightningBolt(), ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, sphinx.getId());

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        harness.assertNotOnBattlefield(player1, "Sphinx of New Prahv");
        harness.assertInGraveyard(player1, "Sphinx of New Prahv");
    }

    private void prepareOpponentCast(LightningBolt spell, ManaColor color, int amount) {
        harness.forceActivePlayer(player2);
        harness.forceStep(gd.currentStep);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(spell));
        harness.addMana(player2, color, amount);
    }
}
