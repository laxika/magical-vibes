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
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Abzan Advantage")
@CardUsed({AbzanAdvantage.class, GrizzlyBears.class, GroundSeal.class, GiantSpider.class})
class AbzanAdvantageTest extends BaseCardTest {

    @Test
    @DisplayName("Target player sacrifices an enchantment and bolster puts a counter on the least-tough creature")
    void sacrificesEnchantmentAndBolstersLeastToughCreature() {
        harness.addToBattlefield(player2, new GroundSeal());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());

        castAbzanAdvantage(player2.getId());

        harness.assertInGraveyard(player2, "Ground Seal");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bolster lets the controller choose among creatures tied for least toughness")
    void choosesAmongLeastToughnessCreatures() {
        harness.addToBattlefield(player2, new GroundSeal());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castAbzanAdvantage(player2.getId());

        PendingInteraction.MultiPermanentChoice choice =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(choice.context()).isEqualTo(
                new MultiPermanentChoiceContext.OwnPermanentCounterPlacement(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, true));

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

    @Test
    @DisplayName("Bolster still happens when the target player has no enchantments")
    void bolstersWithoutAnEnchantmentToSacrifice() {
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAbzanAdvantage(player2.getId());

        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Abzan Advantage");
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The caster can target themselves and sacrifice their own enchantment")
    void canTargetCaster() {
        harness.addToBattlefield(player1, new GroundSeal());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castAbzanAdvantage(player1.getId());

        harness.assertInGraveyard(player1, "Ground Seal");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The target chooses one enchantment before the caster bolsters")
    void targetChoosesEnchantmentBeforeBolster() {
        Permanent firstSeal = harness.addToBattlefieldAndReturn(player2, new GroundSeal());
        Permanent secondSeal = harness.addToBattlefieldAndReturn(player2, new GroundSeal());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingSpider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        castAbzanAdvantage(player2.getId());

        PendingInteraction.MultiPermanentChoice choice =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstSeal.getId(), secondSeal.getId());
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.handleMultiplePermanentsChosen(player2, List.of(secondSeal.getId()));

        assertThat(harness.getGameData().playerBattlefields.get(player2.getId()))
                .contains(firstSeal, opposingSpider).doesNotContain(secondSeal);
        assertThat(harness.getGameData().playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Ground Seal")).hasSize(1);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Bolster compares current toughness including counters")
    void usesCurrentToughnessForBolster() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());

        castAbzanAdvantage(player2.getId());

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void castAbzanAdvantage(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new AbzanAdvantage()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.castAndResolveInstant(player1, 0, targetPlayerId);
    }
}
