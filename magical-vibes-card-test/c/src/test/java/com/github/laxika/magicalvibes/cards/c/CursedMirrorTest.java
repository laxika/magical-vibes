package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CursedMirror.class, GrizzlyBears.class, LlanowarElves.class, ElvishVisionary.class})
class CursedMirrorTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Cursed Mirror adds red mana")
    void tapsForRedMana() {
        Permanent mirror = harness.addToBattlefieldAndReturn(player1, new CursedMirror());

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(mirror.isTapped()).isTrue();
    }

    @Test
    @DisplayName("May enter as a hasty creature copy until end of turn")
    void copiesCreatureUntilEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new CursedMirror(), "{2}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());

        Permanent mirror = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Cursed Mirror"))
                .findFirst()
                .orElseThrow();
        assertThat(mirror.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(mirror.getCard().getPower()).isEqualTo(2);
        assertThat(mirror.getCard().getToughness()).isEqualTo(2);
        assertThat(mirror.getCard().getKeywords()).contains(Keyword.HASTE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mirror.getCard().getName()).isEqualTo("Cursed Mirror");
    }

    @Test
    @DisplayName("Declining the copy leaves Cursed Mirror as an artifact")
    void declinesCopy() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.castFromHand(player1, new CursedMirror(), "{2}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent mirror = findPermanent(player1, "Cursed Mirror");
        int mirrorIndex = gd.playerBattlefields.get(player1.getId()).indexOf(mirror);
        harness.tapPermanent(player1, mirrorIndex);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Resolves normally without creatures available to copy")
    void noCreaturesAvailable() {
        harness.castFromHand(player1, new CursedMirror(), "{2}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent mirror = findPermanent(player1, "Cursed Mirror");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.tapPermanent(player1, 0);
        assertThat(mirror.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Copies an opponent's mana creature and can tap immediately using haste")
    void copiesOpponentsManaCreature() {
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        elves.tap();
        elves.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.castFromHand(player1, new CursedMirror(), "{2}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, elves.getId());

        Permanent mirror = findPermanent(player1, "Llanowar Elves");
        assertThat(mirror.isTapped()).isFalse();
        assertThat(mirror.getPlusOnePlusOneCounters()).isZero();
        harness.tapPermanent(player1, 0);
        assertThat(mirror.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mirror.getCard().getName()).isEqualTo("Cursed Mirror");
        assertThat(mirror.getCard().getKeywords()).doesNotContain(Keyword.HASTE);
    }

    @Test
    @DisplayName("The copied creature's entry ability triggers for the Mirror's controller")
    void copiedEntryAbilityTriggers() {
        Permanent visionary = harness.addToBattlefieldAndReturn(player2, new ElvishVisionary());
        LlanowarElves drawnCard = new LlanowarElves();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.castFromHand(player1, new CursedMirror(), "{2}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, visionary.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(findPermanent(player1, "Elvish Visionary").getOriginalCard())
                .isInstanceOf(CursedMirror.class);
    }
}
