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
        harness.setLibrary(player1, List.of(new MerfolkSpy(), new MerfolkSpy(), new MerfolkSpy()));
        harness.castFromHand(player1, new Concentrate(), "{2}{U}{U}");

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

        harness.castFromHand(player1, new MerfolkSpy(), "{U}");
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

    @Test
    @DisplayName("An opponent's blue noncreature spell does not trigger Namor")
    void ignoresOpponentsSpell() {
        harness.addToBattlefield(player2, new NamorTheSubMariner());
        harness.setLibrary(player1, List.of(new MerfolkSpy(), new MerfolkSpy(), new MerfolkSpy()));

        harness.castFromHand(player1, new Concentrate(), "{2}{U}{U}");

        assertThat(namorTriggers()).isZero();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Namor does not trigger on its own cast and counts itself after entering")
    void doesNotTriggerOnOwnCast() {
        harness.castFromHand(player1, new NamorTheSubMariner(), "{1}{U}{U}");

        assertThat(namorTriggers()).isZero();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent namor = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, namor)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, namor)).isEqualTo(4);
    }

    @Test
    @DisplayName("Power and toughness boosts apply after Namor's characteristic ability")
    void boostAppliesAfterMerfolkCount() {
        Permanent namor = harness.addToBattlefieldAndReturn(player1, new NamorTheSubMariner());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, namor.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, namor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, namor)).isEqualTo(7);

        harness.addToBattlefield(player1, new MerfolkSpy());

        assertThat(gqs.getEffectivePower(gd, namor)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, namor)).isEqualTo(7);
    }

    private long namorTriggers() {
        return gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .filter(entry -> entry.getCard().getName().equals("Namor the Sub-Mariner"))
                .count();
    }
}
