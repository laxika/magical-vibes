package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.Concentrate;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.m.MerfolkSpy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NamorTheSubMariner.class, Concentrate.class, GiantGrowth.class, MerfolkSpy.class})
class NamorTheSubMarinerTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of Merfolk controlled and toughness remains four")
    void powerCountsControlledMerfolk() {
        Permanent namor = harness.addToBattlefieldAndReturn(player1, new NamorTheSubMariner());
        harness.addToBattlefield(player1, new MerfolkSpy());
        harness.addToBattlefield(player2, new MerfolkSpy());

        assertThat(gqs.getEffectivePower(gd, namor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, namor)).isEqualTo(4);
    }

    @Test
    @DisplayName("A noncreature spell creates one Merfolk token per blue mana symbol")
    void createsTokensForBlueManaSymbols() {
        Permanent namor = harness.addToBattlefieldAndReturn(player1, new NamorTheSubMariner());
        harness.setHand(player1, List.of(new Concentrate()));
        harness.setLibrary(player1, List.of(new MerfolkSpy(), new MerfolkSpy(), new MerfolkSpy()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, List.of());

        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Namor the Sub-Mariner"));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Merfolk")))
                .hasSize(2);
        assertThat(gqs.getEffectivePower(gd, namor)).isEqualTo(3);

        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Creature and non-blue noncreature spells do not trigger")
    void ignoresCreatureAndNonBlueSpells() {
        harness.addToBattlefield(player1, new NamorTheSubMariner());
        harness.addToBattlefield(player1, new MerfolkSpy());

        harness.setHand(player1, List.of(new MerfolkSpy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        assertThat(namorTriggers()).isZero();
        harness.passBothPriorities();

        Permanent target = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Namor the Sub-Mariner"))
                .findFirst()
                .orElseThrow();
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());

        assertThat(namorTriggers()).isZero();
        harness.passBothPriorities();
    }

    private long namorTriggers() {
        return gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .filter(entry -> entry.getCard().getName().equals("Namor the Sub-Mariner"))
                .count();
    }
}
