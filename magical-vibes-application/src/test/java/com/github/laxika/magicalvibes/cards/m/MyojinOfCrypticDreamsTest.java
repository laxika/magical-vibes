package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MyojinOfCrypticDreams.class, GrizzlyBears.class, CounselOfTheSoratami.class})
class MyojinOfCrypticDreamsTest extends BaseCardTest {

    @Test
    @DisplayName("Cast from hand enters with an indestructible counter and indestructible")
    void castFromHandEntersWithIndestructibleCounter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MyojinOfCrypticDreams()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent myojin = findPermanent(player1, "Myojin of Cryptic Dreams");
        assertThat(myojin.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Removing the counter copies a permanent spell three times as tokens")
    void copiesPermanentSpellThreeTimesAsTokens() {
        Permanent myojin = addReadyMyojin();
        myojin.setCounterCount(CounterType.INDESTRUCTIBLE, 1);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(myojin.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(3);

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(4);
        assertThat(findPermanents(player1, "Grizzly Bears"))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(3);
    }

    @Test
    @DisplayName("Cannot target a nonpermanent spell")
    void cannotTargetNonpermanentSpell() {
        addReadyMyojin();
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player1, List.of(counsel));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, counsel.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyMyojin() {
        return addCreatureReady(player1, new MyojinOfCrypticDreams());
    }
}
