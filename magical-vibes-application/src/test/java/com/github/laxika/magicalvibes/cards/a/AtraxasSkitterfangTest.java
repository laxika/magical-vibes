package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CrawlingChorus;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AtraxasSkitterfang.class, CrawlingChorus.class})
class AtraxasSkitterfangTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three oil counters")
    void entersWithOilCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AtraxasSkitterfang()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent skitterfang = findPermanent(player1, "Atraxa's Skitterfang");

        assertThat(skitterfang.getCounterCount(CounterType.OIL)).isEqualTo(3);
    }

    @Test
    @DisplayName("Accepting the combat trigger removes an oil counter and grants the chosen keyword")
    void acceptsCombatTriggerAndGrantsChosenKeyword() {
        Permanent skitterfang = addSkitterfang(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CrawlingChorus());
        advanceToCombat(player1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "DEATHTOUCH");

        assertThat(skitterfang.getCounterCount(CounterType.OIL)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("The combat trigger only offers creatures controlled by its controller")
    void onlyOffersControlledCreatures() {
        addSkitterfang(player1);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new CrawlingChorus());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new CrawlingChorus());
        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).contains(ownCreature.getId()).doesNotContain(opposingCreature.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opposingCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Declining the combat trigger leaves the oil counters and keywords unchanged")
    void decliningCombatTriggerDoesNothing() {
        Permanent skitterfang = addSkitterfang(player1);
        advanceToCombat(player1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(skitterfang.getCounterCount(CounterType.OIL)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, skitterfang, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The chosen keyword wears off at end of turn")
    void chosenKeywordWearsOffAtEndOfTurn() {
        addSkitterfang(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CrawlingChorus());
        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "FLYING");

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The trigger does not fire during an opponent's combat")
    void doesNotTriggerDuringOpponentsCombat() {
        addSkitterfang(player1);
        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = Keyword.class, names = {"FLYING", "VIGILANCE", "DEATHTOUCH", "LIFELINK"})
    @DisplayName("Each offered keyword can be granted to Skitterfang itself")
    void grantsEachKeywordToItself(Keyword keyword) {
        Permanent skitterfang = addSkitterfang(player1);
        skitterfang.setCounterCount(CounterType.OIL, 1);
        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(skitterfang.getCounterCount(CounterType.OIL)).isZero();
        assertThat(gqs.hasKeyword(gd, skitterfang, keyword)).isFalse();

        harness.handlePermanentChosen(player1, skitterfang.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.hasKeyword(gd, skitterfang, keyword)).isFalse();

        harness.passBothPriorities();
        harness.handleListChoice(player1, keyword.name());

        assertThat(gqs.hasKeyword(gd, skitterfang, keyword)).isTrue();
        assertThat(skitterfang.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    @DisplayName("No oil counters means no reflexive trigger or keyword grant")
    void cannotGrantKeywordWithoutOil() {
        Permanent skitterfang = addSkitterfang(player1);
        skitterfang.setCounterCount(CounterType.OIL, 0);
        advanceToCombat(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(skitterfang.getCounterCount(CounterType.OIL)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gqs.hasKeyword(gd, skitterfang, Keyword.FLYING)).isFalse();
    }

    private Permanent addSkitterfang(Player player) {
        Permanent skitterfang = harness.addToBattlefieldAndReturn(player, new AtraxasSkitterfang());
        skitterfang.setCounterCount(CounterType.OIL, 3);
        return skitterfang;
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
    }
}
