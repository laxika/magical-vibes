package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GryffRider;
import com.github.laxika.magicalvibes.cards.u.UlvenwaldBehemoth;
import com.github.laxika.magicalvibes.cards.u.UlvenwaldOddity;
import com.github.laxika.magicalvibes.cards.w.WitchsWeb;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HamletVanguard.class, GryffRider.class, UlvenwaldOddity.class,
        UlvenwaldBehemoth.class, WitchsWeb.class})
class HamletVanguardTest extends BaseCardTest {

    @Test
    void entersWithTwoCountersForEachOtherNontokenHumanYouControl() {
        addHuman(player1, false);
        addHuman(player1, false);
        addHuman(player1, true);
        addHuman(player2, false);

        Permanent vanguard = castVanguard();

        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void entersWithoutCountersWhenYouControlNoNontokenHumans() {
        Permanent vanguard = castVanguard();

        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void nonhumanCreaturesDoNotContributeCounters() {
        harness.addToBattlefield(player1, new UlvenwaldOddity());

        Permanent vanguard = castVanguard();

        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void countsHumansPresentAtResolutionRatherThanAtCasting() {
        harness.setHand(player1, List.of(new HamletVanguard()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.addToBattlefield(player1, new GryffRider());

        harness.passBothPriorities();

        Permanent vanguard = findPermanent(player1, "Hamlet Vanguard");
        assertThat(vanguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void existingVanguardCountsAsAnotherNontokenHuman() {
        Permanent first = castVanguard();

        Permanent second = castVanguard();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void wardCountersAnOpponentsSpellWhenTheyCannotPayTwoMana() {
        Permanent vanguard = castVanguard();
        harness.setHand(player2, List.of(new WitchsWeb()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, vanguard.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(1);
        harness.assertInGraveyard(player2, "Witch's Web");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void payingTwoManaForWardAllowsTheOpponentsSpellToResolve() {
        Permanent vanguard = castVanguard();
        harness.setHand(player2, List.of(new WitchsWeb()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castInstant(player2, 0, vanguard.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player2, "Witch's Web");
    }

    @Test
    void decliningWardPaymentCountersTheOpponentsSpell() {
        Permanent vanguard = castVanguard();
        harness.setHand(player2, List.of(new WitchsWeb()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castInstant(player2, 0, vanguard.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Witch's Web");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void wardDoesNotTriggerForItsControllersSpell() {
        Permanent vanguard = castVanguard();
        harness.setHand(player1, List.of(new WitchsWeb()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, vanguard.getId());

        assertThat(gqs.getEffectivePower(gd, vanguard)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent castVanguard() {
        harness.setHand(player1, List.of(new HamletVanguard()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanents(player1, "Hamlet Vanguard").getLast();
    }

    private Permanent addHuman(com.github.laxika.magicalvibes.model.Player player, boolean token) {
        GryffRider card = new GryffRider();
        card.setToken(token);
        return addCreatureReady(player, card);
    }
}
