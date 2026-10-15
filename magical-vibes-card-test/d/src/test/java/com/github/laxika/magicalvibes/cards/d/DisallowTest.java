package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImplementOfExamination;
import com.github.laxika.magicalvibes.cards.j.JhoiraWeatherlightCaptain;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Disallow.class, GrizzlyBears.class, JhoiraWeatherlightCaptain.class,
        RodOfRuin.class, Spellbook.class, ImplementOfExamination.class, Ornithopter.class})
class DisallowTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a target spell")
    void countersSpell() {
        GrizzlyBears bears = new GrizzlyBears();

        harness.setHand(player1, List.of(new Disallow()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, bears, "{1}{G}");
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Counters a target activated ability")
    void countersActivatedAbility() {
        RodOfRuin rod = new RodOfRuin();
        harness.addToBattlefield(player2, rod);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new Disallow()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passPriority(player2);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        harness.castAndResolveInstant(player1, 0, rod.getId());

        harness.assertLife(player1, lifeBefore);
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Counters a target triggered ability")
    void countersTriggeredAbility() {
        harness.addToBattlefield(player2, new JhoiraWeatherlightCaptain());
        harness.setHand(player1, List.of(new Disallow()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new Spellbook(), "{0}");
        StackEntry trigger = harness.getGameData().stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .findFirst()
                .orElseThrow();
        harness.passPriority(player2);

        harness.castAndResolveInstant(player1, 0, trigger.getCard().getId());

        assertThat(harness.getGameData().stack).noneMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new Disallow()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters the selected trigger without countering another ability from the same source")
    void countersOnlySelectedAbilityFromSameSource() {
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player2, new ImplementOfExamination());
        harness.setLibrary(player2, List.of(new Ornithopter(), new Ornithopter()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new Disallow()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, null);

        StackEntry activated = gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.ACTIVATED_ABILITY)
                .findFirst().orElseThrow();
        StackEntry triggered = gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .findFirst().orElseThrow();
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, triggered.getTargetableId());

        assertThat(gd.stack).containsExactly(activated);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Implement of Examination");
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }
}
