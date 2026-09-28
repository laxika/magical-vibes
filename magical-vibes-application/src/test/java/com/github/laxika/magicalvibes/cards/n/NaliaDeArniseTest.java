package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BoggartBrute;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NaliaDeArnise.class, BoggartBrute.class, FugitiveWizard.class,
        GrizzlyBears.class, SoulWarden.class})
class NaliaDeArniseTest extends BaseCardTest {

    @Test
    @DisplayName("Casts Cleric, Rogue, Warrior, and Wizard spells from the top of the library")
    void castsPartySpellFromLibraryTop() {
        harness.addToBattlefield(player1, new NaliaDeArnise());
        Card wizard = new FugitiveWizard();
        gd.playerDecks.get(player1.getId()).addFirst(wizard);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castFromLibraryTop(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == wizard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(wizard);
    }

    @Test
    @DisplayName("Cannot cast a non-party creature from the top of the library")
    void cannotCastNonPartyCreatureFromLibraryTop() {
        harness.addToBattlefield(player1, new NaliaDeArnise());
        Card bears = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(bears);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(bears);
    }

    @Test
    @DisplayName("A full party puts counters on your creatures and grants deathtouch until end of turn")
    void fullPartyPutsCountersAndGrantsDeathtouch() {
        Permanent nalia = harness.addToBattlefieldAndReturn(player1, new NaliaDeArnise());
        Permanent cleric = harness.addToBattlefieldAndReturn(player1, new SoulWarden());
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new BoggartBrute());
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToCombat(player1);
        harness.passBothPriorities();

        for (Permanent creature : new Permanent[]{nalia, cleric, warrior, wizard}) {
            assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
        }
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Does not grant the combat bonus without a full party")
    void doesNotGrantCombatBonusWithoutFullParty() {
        Permanent nalia = harness.addToBattlefieldAndReturn(player1, new NaliaDeArnise());
        Permanent cleric = harness.addToBattlefieldAndReturn(player1, new SoulWarden());
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new BoggartBrute());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.passBothPriorities();

        for (Permanent creature : new Permanent[]{nalia, cleric, warrior, other}) {
            assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
            assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
        }
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
