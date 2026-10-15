package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AlpineWatchdog;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JolraelMwonvuliRecluse.class, AlpineWatchdog.class})
class JolraelMwonvuliRecluseTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing the second card each turn creates one 2/2 green Cat")
    void secondDrawCreatesCatOnlyOnce() {
        harness.addToBattlefield(player1, new JolraelMwonvuliRecluse());
        harness.setLibrary(player1, List.of(new AlpineWatchdog(), new AlpineWatchdog(), new AlpineWatchdog()));

        drawCard(player1);
        assertThat(gd.stack).isEmpty();

        drawCard(player1);
        assertThat(gd.stack).hasSize(1);

        drawCard(player1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        List<Permanent> cats = findPermanents(player1, "Cat");
        assertThat(cats).hasSize(1);
        assertThat(cats.get(0).getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(cats.get(0).getCard().getSubtypes()).containsExactly(CardSubtype.CAT);
        assertThat(gqs.getEffectivePower(gd, cats.get(0))).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cats.get(0))).isEqualTo(2);
    }

    @Test
    @DisplayName("The activated ability sets your creatures to your hand size")
    void activatedAbilitySetsOwnCreaturesToHandSize() {
        Permanent jolrael = harness.addToBattlefieldAndReturn(player1, new JolraelMwonvuliRecluse());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new AlpineWatchdog());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());
        harness.setHand(player1, List.of(new AlpineWatchdog(), new AlpineWatchdog(), new AlpineWatchdog()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, jolrael)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, jolrael)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("A first draw before Jolrael enters still counts toward the second draw")
    void firstDrawBeforeEnteringCounts() {
        harness.setLibrary(player1, List.of(new AlpineWatchdog(), new AlpineWatchdog()));
        drawCard(player1);
        harness.addToBattlefield(player1, new JolraelMwonvuliRecluse());

        drawCard(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Cat")).isEqualTo(1);
    }

    @Test
    @DisplayName("Entering after the second draw does not trigger on the third draw")
    void enteringAfterSecondDrawDoesNotTrigger() {
        harness.setLibrary(player1, List.of(new AlpineWatchdog(), new AlpineWatchdog(), new AlpineWatchdog()));
        drawCard(player1);
        drawCard(player1);
        harness.addToBattlefield(player1, new JolraelMwonvuliRecluse());

        drawCard(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Cat")).isZero();
    }

    @Test
    @DisplayName("The draw count resets each turn and Jolrael triggers during an opponent's turn")
    void triggersAgainOnOpponentsTurnButNotForOpponentsDraws() {
        harness.addToBattlefield(player1, new JolraelMwonvuliRecluse());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new AlpineWatchdog(), new AlpineWatchdog(),
                new AlpineWatchdog(), new AlpineWatchdog()));
        harness.setLibrary(player2, List.of(new AlpineWatchdog(), new AlpineWatchdog()));
        drawCard(player1);
        drawCard(player1);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Cat")).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        drawCard(player2);
        drawCard(player2);
        assertThat(gd.stack).isEmpty();

        drawCard(player1);
        assertThat(gd.stack).isEmpty();
        drawCard(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Cat")).isEqualTo(2);
        assertThat(countPermanents(player2, "Cat")).isZero();
    }

    @Test
    @DisplayName("Hand size is determined at resolution and stays fixed after more cards are drawn")
    void handSizeIsLockedAtResolution() {
        Permanent jolrael = harness.addToBattlefieldAndReturn(player1, new JolraelMwonvuliRecluse());
        harness.setHand(player1, List.of(new AlpineWatchdog()));
        harness.setLibrary(player1, List.of(new AlpineWatchdog(), new AlpineWatchdog()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);

        drawCard(player1);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, jolrael)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, jolrael)).isEqualTo(2);

        drawCard(player1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gqs.getEffectivePower(gd, jolrael)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, jolrael)).isEqualTo(2);
    }

    @Test
    @DisplayName("Later creatures are unaffected and the base power and toughness change expires")
    void affectedCreaturesAreLockedAndEffectExpires() {
        Permanent jolrael = harness.addToBattlefieldAndReturn(player1, new JolraelMwonvuliRecluse());
        harness.setHand(player1, List.of(new AlpineWatchdog(), new AlpineWatchdog(), new AlpineWatchdog()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent lateCreature = harness.enterBattlefieldAndReturn(player1, new AlpineWatchdog());
        assertThat(gqs.getEffectivePower(gd, jolrael)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, jolrael)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, lateCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lateCreature)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, jolrael)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, jolrael)).isEqualTo(2);
    }

    @Test
    @DisplayName("An empty hand sets creatures to zero toughness and they die")
    void emptyHandKillsOwnCreatures() {
        harness.addToBattlefield(player1, new JolraelMwonvuliRecluse());
        harness.addToBattlefield(player1, new AlpineWatchdog());
        harness.addToBattlefield(player2, new AlpineWatchdog());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Jolrael, Mwonvuli Recluse");
        harness.assertNotOnBattlefield(player1, "Alpine Watchdog");
        harness.assertInGraveyard(player1, "Jolrael, Mwonvuli Recluse");
        harness.assertInGraveyard(player1, "Alpine Watchdog");
        harness.assertOnBattlefield(player2, "Alpine Watchdog");
    }

    @Test
    @DisplayName("Counters apply above the new base power and toughness")
    void countersApplyAfterBaseChange() {
        Permanent jolrael = harness.addToBattlefieldAndReturn(player1, new JolraelMwonvuliRecluse());
        jolrael.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new AlpineWatchdog(), new AlpineWatchdog(), new AlpineWatchdog()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, jolrael)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, jolrael)).isEqualTo(5);
    }

    private void drawCard(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
