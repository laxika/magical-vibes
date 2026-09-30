package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardPowerToughnessModifier;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GyoxBrutalCarnivora.class, GrizzlyBears.class, Shock.class, Island.class})
class GyoxBrutalCarnivoraTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of your end step, it puts an oil counter on a target creature")
    void putsOilCounterOnTargetCreatureAtEndStep() {
        harness.addToBattlefield(player1, new GyoxBrutalCarnivora());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    @DisplayName("A nontoken creature with oil counters dying conjures scaled perpetual duplicates into the library")
    void conjuresDuplicatePerOilCounterWithPerpetualBonus() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent gyox = harness.addToBattlefieldAndReturn(player1, new GyoxBrutalCarnivora());
        Permanent dying = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        dying.setCounterCount(CounterType.OIL, 2);
        harness.setLibrary(player1, List.of(new Island()));

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, dying.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Card> duplicates = gd.playerDecks.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Grizzly Bears"))
                .toList();
        assertThat(duplicates).hasSize(2);
        assertThat(duplicates).extracting(Card::getId).doesNotHaveDuplicates();
        assertThat(duplicates).allSatisfy(duplicate -> {
            assertThat(duplicate.isToken()).isTrue();
            assertThat(duplicate.isTokenCard()).isTrue();
            assertThat(gd.perpetualCardPowerToughnessModifiers)
                    .containsEntry(duplicate.getId(), new CardPowerToughnessModifier(2, 2));
        });
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dying);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(gyox);
    }

    @Test
    @DisplayName("A token creature with oil counters dying does not trigger the ability")
    void tokenCreatureDeathDoesNotTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new GyoxBrutalCarnivora());
        GrizzlyBears tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);
        token.setCounterCount(CounterType.OIL, 2);
        harness.setLibrary(player1, List.of(new Island()));

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, token.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }
}
