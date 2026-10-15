package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.c.Clue;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
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

@CardUsed({InTooDeep.class, Clue.class, FountainOfYouth.class, GrizzlyBears.class, JaceBeleren.class,
        SwordsToPlowshares.class})
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

    @Test
    void splitSecondPreventsSpellsAndNonManaAbilities() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new InTooDeep()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(new SwordsToPlowshares()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player2, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("split second");

        harness.passBothPriorities();
        harness.activateAbility(player2, 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(21);
    }

    @Test
    void transformedPlaneswalkerHasOnlyClueAbilityAndRetainsCounters() {
        Permanent jace = harness.addToBattlefieldAndReturn(player1, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player2, List.of(new InTooDeep()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castEnchantment(player2, 0, jace.getId());
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Jace Beleren");
        harness.assertInGraveyard(player2, "In Too Deep");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    void transformedPlaneswalkerLosesPlaneswalkerSubtype() {
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new InTooDeep()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, jace.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasEffectiveSubtype(gd, jace, CardSubtype.CLUE)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, jace, CardSubtype.JACE)).isFalse();
    }

    @Test
    void clueAbilityRequiresTwoMana() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new InTooDeep()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "In Too Deep");
    }

    @Test
    void auraFizzlesWhenTargetLeavesBeforeResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new InTooDeep()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, bears.getId());
        gd.playerBattlefields.get(player2.getId()).remove(bears);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "In Too Deep");
        harness.assertNotOnBattlefield(player1, "In Too Deep");
    }
}
