package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
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

@CardUsed({MyojinOfCrypticDreams.class, GrizzlyBears.class, CounselOfTheSoratami.class, Pacifism.class})
class MyojinOfCrypticDreamsTest extends BaseCardTest {

    @Test
    @DisplayName("Cast from hand enters with an indestructible counter and indestructible")
    void castFromHandEntersWithIndestructibleCounter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new MyojinOfCrypticDreams(), "{5}{U}{U}{U}");
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
        harness.castFromHand(player1, bears, "{1}{G}");
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
        Permanent myojin = addReadyMyojin();
        myojin.setCounterCount(CounterType.INDESTRUCTIBLE, 1);
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.castFromHand(player1, counsel, "{2}{U}");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, counsel.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Putting Myojin onto the battlefield without casting grants no counter")
    void entersWithoutCastingHasNoIndestructibleCounter() {
        Permanent myojin = new Permanent(new MyojinOfCrypticDreams());
        harness.getBattlefieldEntryService().putPermanentOntoBattlefield(gd, player1.getId(), myojin);

        assertThat(myojin.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("The last counter is paid immediately and removes indestructible")
    void counterIsPaidBeforeAbilityResolves() {
        Permanent myojin = addReadyMyojin();
        myojin.setCounterCount(CounterType.INDESTRUCTIBLE, 1);
        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player1, bears, "{1}{G}");

        harness.activateAbility(player1, 0, null, bears.getId());

        assertThat(myojin.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
        assertThat(gqs.hasKeyword(gd, myojin, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gd.stack).filteredOn(StackEntry::isCopy).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot copy an opponent's permanent spell")
    void cannotTargetOpponentsPermanentSpell() {
        Permanent myojin = addReadyMyojin();
        myojin.setCounterCount(CounterType.INDESTRUCTIBLE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player2, bears, "{1}{G}");
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(myojin.getCounterCount(CounterType.INDESTRUCTIBLE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Aura copies keep their original target without offering new targets")
    void auraCopiesKeepOriginalTarget() {
        Permanent myojin = addReadyMyojin();
        myojin.setCounterCount(CounterType.INDESTRUCTIBLE, 1);
        Permanent enchanted = addCreatureReady(player2, new GrizzlyBears());
        Pacifism pacifism = new Pacifism();
        harness.setHand(player1, List.of(pacifism));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.activateAbility(player1, 0, null, pacifism.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(3)
                .allSatisfy(copy -> assertThat(copy.getTargetId()).isEqualTo(enchanted.getId()));

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Pacifism")).hasSize(4)
                .allSatisfy(aura -> assertThat(aura.getAttachedTo()).isEqualTo(enchanted.getId()));
        assertThat(findPermanents(player1, "Pacifism"))
                .filteredOn(aura -> aura.getCard().isToken()).hasSize(3);
    }

    @Test
    @DisplayName("A spell copy of Myojin was not cast and enters without a counter")
    void copiedMyojinDoesNotReceiveIndestructibleCounter() {
        Permanent source = addReadyMyojin();
        source.setCounterCount(CounterType.INDESTRUCTIBLE, 1);
        MyojinOfCrypticDreams spell = new MyojinOfCrypticDreams();
        harness.castFromHand(player1, spell, "{5}{U}{U}{U}");
        harness.activateAbility(player1, 0, null, spell.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Myojin of Cryptic Dreams"))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1)
                .allSatisfy(copy -> {
                    assertThat(copy.getCounterCount(CounterType.INDESTRUCTIBLE)).isZero();
                    assertThat(gqs.hasKeyword(gd, copy, Keyword.INDESTRUCTIBLE)).isFalse();
                });
    }

    private Permanent addReadyMyojin() {
        return addCreatureReady(player1, new MyojinOfCrypticDreams());
    }
}
