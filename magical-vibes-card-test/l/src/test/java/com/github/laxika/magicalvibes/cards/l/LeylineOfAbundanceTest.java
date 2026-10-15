package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.ElvishMystic;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeylineOfAbundance.class, ElvishMystic.class, Forest.class,
        GiantSpider.class, GrizzlyBears.class, LeafkinDruid.class})
class LeylineOfAbundanceTest extends BaseCardTest {

    @Test
    @DisplayName("Leyline in opening hand may begin the game on the battlefield")
    void leylineInOpeningHandMayStartOnBattlefield() {
        GameTestHarness openingHarness = new GameTestHarness();
        openingHarness.setHand(openingHarness.getPlayer1(), List.of(new LeylineOfAbundance()));
        openingHarness.skipMulligan();

        assertThat(openingHarness.getGameData().interaction.isAwaitingInput()).isTrue();

        openingHarness.handleMayAbilityChosen(openingHarness.getPlayer1(), true);

        assertThat(openingHarness.getGameData().playerBattlefields
                .get(openingHarness.getPlayer1().getId()))
                .anyMatch(p -> p.getCard().getName().equals("Leyline of Abundance"));
    }

    @Test
    @DisplayName("Tapping a creature for mana adds an additional green mana")
    void creatureTapProducesAdditionalGreen() {
        harness.addToBattlefield(player1, new LeylineOfAbundance());
        addCreatureReady(player1, new ElvishMystic());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Leyline only rewards its controller's creature taps")
    void onlyControllerCreatureTapProducesAdditionalMana() {
        harness.addToBattlefield(player1, new LeylineOfAbundance());
        addCreatureReady(player2, new ElvishMystic());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Tapping a noncreature for mana does not add the bonus mana")
    void noncreatureTapDoesNotProduceBonus() {
        harness.addToBattlefield(player1, new LeylineOfAbundance());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activated creature mana abilities also receive the bonus")
    void activatedCreatureManaAbilityProducesAdditionalGreen() {
        harness.addToBattlefield(player1, new LeylineOfAbundance());
        addCreatureReady(player1, new LeafkinDruid());

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activation puts a +1/+1 counter on each creature you control")
    void activationCountersOwnCreatures() {
        harness.addToBattlefield(player1, new LeylineOfAbundance());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent spider = addCreatureReady(player1, new GiantSpider());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Declining the opening-hand choice leaves Leyline in hand")
    void mayDeclineStartingOnBattlefield() {
        GameTestHarness openingHarness = new GameTestHarness();
        LeylineOfAbundance leyline = new LeylineOfAbundance();
        openingHarness.setHand(openingHarness.getPlayer1(), List.of(leyline));
        openingHarness.skipMulligan();

        openingHarness.handleMayAbilityChosen(openingHarness.getPlayer1(), false);

        assertThat(openingHarness.getGameData().playerHands
                .get(openingHarness.getPlayer1().getId())).contains(leyline);
        assertThat(openingHarness.getGameData().playerBattlefields
                .get(openingHarness.getPlayer1().getId())).isEmpty();
    }

    @Test
    @DisplayName("Each Leyline adds its own green mana without using the stack")
    void multipleLeylinesEachAddManaImmediately() {
        harness.addToBattlefield(player1, new LeylineOfAbundance());
        harness.addToBattlefield(player1, new LeylineOfAbundance());
        addCreatureReady(player1, new LeafkinDruid());

        harness.activateAbility(player1, 2, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature producing two mana still receives only one additional green")
    void bonusIsPerActivationRatherThanPerManaProduced() {
        harness.addToBattlefield(player1, new LeylineOfAbundance());
        for (int i = 0; i < 4; i++) {
            addCreatureReady(player1, new LeafkinDruid());
        }

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The counter ability affects creatures present on resolution and excludes noncreatures")
    void countersUseBattlefieldAtResolution() {
        Permanent leyline = harness.addToBattlefieldAndReturn(player1, new LeylineOfAbundance());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent firstCreature = addCreatureReady(player1, new LeafkinDruid());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(firstCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new LeafkinDruid());
        harness.passBothPriorities();

        assertThat(firstCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(laterCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(leyline.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(forest.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
