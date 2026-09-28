package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RexCyberHound.class, DrudgeSkeletons.class, GrizzlyBears.class, RodOfRuin.class})
class RexCyberHoundTest extends BaseCardTest {

    @Test
    void millsDamagedPlayerAndGivesControllerTwoEnergy() {
        Permanent rex = addCreatureReady(player1, new RexCyberHound());
        rex.setAttacking(true);
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void exilesTargetCreatureWithBrainCounter() {
        addCreatureReady(player1, new RexCyberHound());
        Card skeletons = new DrudgeSkeletons();
        harness.setGraveyard(player2, List.of(skeletons));
        gd.playerEnergyCounters.put(player1.getId(), 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(skeletons.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(skeletons);
        assertThat(gd.exiledCardsWithBrainCounters).containsExactly(skeletons.getId());
    }

    @Test
    void gainsActivatedAbilityFromBrainCounterCard() {
        addCreatureReady(player1, new RexCyberHound());
        Card skeletons = new DrudgeSkeletons();
        harness.setExile(player2, List.of(skeletons));
        gd.exiledCardsWithBrainCounters.add(skeletons.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void cannotTargetNoncreatureCard() {
        addCreatureReady(player1, new RexCyberHound());
        Card rodOfRuin = new RodOfRuin();
        harness.setGraveyard(player2, List.of(rodOfRuin));
        gd.playerEnergyCounters.put(player1.getId(), 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(rodOfRuin.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
