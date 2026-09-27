package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.Clue;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InTooDeep.class, Clue.class, FountainOfYouth.class, GrizzlyBears.class, JaceBeleren.class})
class InTooDeepTest extends BaseCardTest {

    @Test
    @DisplayName("In Too Deep turns an enchanted creature into a colorless Clue artifact")
    void transformsCreatureIntoClue() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent aura = new Permanent(new InTooDeep());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectiveCardTypes(gd, bears)).containsExactly(CardType.ARTIFACT);
        assertThat(gqs.hasEffectiveSubtype(gd, bears, CardSubtype.CLUE)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, bears)).isEmpty();
        assertThat(gqs.isCreature(gd, bears)).isFalse();
    }

    @Test
    @DisplayName("In Too Deep grants the Clue draw ability and removes the creature's abilities")
    void grantsClueAbility() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = new Permanent(new InTooDeep());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player2.getId()).add(aura);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("In Too Deep can target a Clue")
    void targetsClue() {
        Permanent clue = harness.addToBattlefieldAndReturn(player2, new Clue());
        harness.setHand(player1, List.of(new InTooDeep()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, clue.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveCardTypes(gd, clue)).containsExactly(CardType.ARTIFACT);
        assertThat(gqs.hasEffectiveSubtype(gd, clue, CardSubtype.CLUE)).isTrue();
    }

    @Test
    @DisplayName("In Too Deep can target a planeswalker")
    void targetsPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new InTooDeep()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveCardTypes(gd, planeswalker)).containsExactly(CardType.ARTIFACT);
        assertThat(gqs.isPlaneswalker(gd, planeswalker)).isFalse();
    }

    @Test
    @DisplayName("In Too Deep cannot target a non-Clue artifact")
    void rejectsIllegalTarget() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new InTooDeep()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature, planeswalker, or Clue");
    }
}
