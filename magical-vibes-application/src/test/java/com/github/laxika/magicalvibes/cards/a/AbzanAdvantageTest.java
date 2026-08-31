package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GroundSeal;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Abzan Advantage")
class AbzanAdvantageTest extends BaseCardTest {

    @Test
    @DisplayName("Target player sacrifices an enchantment and bolster puts a counter on the least-tough creature")
    void sacrificesEnchantmentAndBolstersLeastToughCreature() {
        harness.addToBattlefield(player2, new GroundSeal());
        Permanent bears = new Permanent(new GrizzlyBears());
        Permanent spider = new Permanent(new GiantSpider());
        harness.getGameData().playerBattlefields.get(player1.getId()).addAll(List.of(bears, spider));

        castAbzanAdvantage(player2.getId());

        harness.assertInGraveyard(player2, "Ground Seal");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bolster lets the controller choose among creatures tied for least toughness")
    void choosesAmongLeastToughnessCreatures() {
        harness.addToBattlefield(player2, new GroundSeal());
        Permanent first = new Permanent(new GrizzlyBears());
        Permanent second = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player1.getId()).addAll(List.of(first, second));

        castAbzanAdvantage(player2.getId());

        PendingInteraction.MultiPermanentChoice choice =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(choice.context()).isEqualTo(
                new MultiPermanentChoiceContext.OwnPermanentCounterPlacement(
                        CounterType.PLUS_ONE_PLUS_ONE, 1));

        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bolster does nothing when its controller has no creatures")
    void bolsterDoesNothingWithoutCreatures() {
        harness.addToBattlefield(player2, new GroundSeal());

        castAbzanAdvantage(player2.getId());

        harness.assertInGraveyard(player2, "Ground Seal");
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    private void castAbzanAdvantage(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new AbzanAdvantage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.castInstant(player1, 0, targetPlayerId);
        harness.passBothPriorities();
    }
}
