package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NecroticSliver.class, MetallicSliver.class, GrizzlyBears.class})
class NecroticSliverTest extends BaseCardTest {

    @Test
    @DisplayName("All Slivers gain the ability, including Necrotic Sliver and opposing Slivers")
    void grantsAbilityToAllSlivers() {
        Permanent necroticSliver = addCreatureReady(player1, new NecroticSliver());
        Permanent ownSliver = addCreatureReady(player1, new MetallicSliver());
        Permanent opposingSliver = addCreatureReady(player2, new MetallicSliver());

        assertThat(gs.getEffectiveActivatedAbilities(gd, necroticSliver)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, ownSliver)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, opposingSliver)).hasSize(1);
    }

    @Test
    @DisplayName("A Sliver can sacrifice itself to destroy a target permanent")
    void sacrificesSliverAndDestroysTargetPermanent() {
        addCreatureReady(player1, new NecroticSliver());
        Permanent ownSliver = addCreatureReady(player1, new MetallicSliver());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownSliver);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownSliver.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("Non-Sliver creatures do not gain the ability")
    void doesNotGrantAbilityToNonSlivers() {
        addCreatureReady(player1, new NecroticSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gs.getEffectiveActivatedAbilities(gd, bears)).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
