package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RalZarek;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TavernScoundrel.class, GrizzlyBears.class, RalZarek.class})
class TavernScoundrelTest extends BaseCardTest {

    @Test
    @DisplayName("The activated ability sacrifices another permanent and creates Treasures when the flip is won")
    void sacrificesAnotherPermanentAndRewardsAWin() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent scoundrel = addCreatureReady(player1, new TavernScoundrel());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        boolean won = gd.gameLog.stream().map(GameLogEntry::plainText)
                .anyMatch(log -> log.contains("wins the coin flip for Tavern Scoundrel"));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(scoundrel.isTapped()).isTrue();
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(won ? 2 : 0);
    }

    @Test
    @DisplayName("Cannot sacrifice itself or an opponent's permanent to pay the activation cost")
    void cannotActivateWithoutAnotherControlledPermanent() {
        Permanent scoundrel = addCreatureReady(player1, new TavernScoundrel());
        addCreatureReady(player2, new TavernScoundrel());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(scoundrel.isTapped()).isFalse();
        assertThat(countPermanents(player1, "Tavern Scoundrel")).isEqualTo(1);
        assertThat(countPermanents(player2, "Tavern Scoundrel")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The activated ability requires one mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new TavernScoundrel());
        Permanent scoundrel = addCreatureReady(player1, new TavernScoundrel());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(scoundrel.isTapped()).isFalse();
        assertThat(countPermanents(player1, "Tavern Scoundrel")).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A summoning-sick Scoundrel cannot activate its tap ability")
    void cannotActivateWhileSummoningSick() {
        addCreatureReady(player1, new TavernScoundrel());
        harness.addToBattlefield(player1, new TavernScoundrel());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(countPermanents(player1, "Tavern Scoundrel")).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each surviving Scoundrel rewards its controller's win, but opposing and sacrificed copies do not")
    void survivingCopiesRewardOnlyTheirControllersWin() {
        Permanent sacrificed = addCreatureReady(player1, new TavernScoundrel());
        addCreatureReady(player1, new TavernScoundrel());
        addCreatureReady(player1, new TavernScoundrel());
        addCreatureReady(player2, new TavernScoundrel());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 2, null, null);
        assertThat(countPermanents(player1, "Treasure")).isZero();
        harness.handlePermanentChosen(player1, sacrificed.getId());

        harness.assertInGraveyard(player1, "Tavern Scoundrel");
        assertThat(countPermanents(player1, "Tavern Scoundrel")).isEqualTo(2);

        resolveAllTriggers();

        boolean won = gd.gameLog.stream().map(GameLogEntry::plainText)
                .anyMatch(log -> log.contains("wins the coin flip for Tavern Scoundrel"));
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(won ? 4 : 0);
        assertThat(findPermanents(player1, "Treasure")).allMatch(permanent -> !permanent.isTapped());
        assertThat(countPermanents(player2, "Treasure")).isZero();
    }

    @Test
    @CardUsed({TavernScoundrel.class, RalZarek.class})
    @DisplayName("Heads-only coin flips have no winner and do not create Treasures")
    void headsOnlyFlipsDoNotCreateTreasures() {
        addCreatureReady(player1, new TavernScoundrel());
        Permanent ral = harness.addToBattlefieldAndReturn(player1, new RalZarek());
        ral.setCounterCount(CounterType.LOYALTY, 7);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 1, 2, null, null);
        resolveAllTriggers();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("flips 5 coins for Ral Zarek"));
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }
}
