package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InBolassClutches;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BenalishMarshal.class, GrizzlyBears.class, InBolassClutches.class})
class BenalishMarshalTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new BenalishMarshal(), "{W}{W}{W}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Benalish Marshal");
    }

    @Test
    @DisplayName("Resolving puts Benalish Marshal onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.castFromHand(player1, new BenalishMarshal(), "{W}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Benalish Marshal");
    }

    @Test
    @DisplayName("Other creatures you control get +1/+1")
    void buffsOtherOwnCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new BenalishMarshal());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff itself")
    void doesNotBuffItself() {
        Permanent marshal = harness.addToBattlefieldAndReturn(player1, new BenalishMarshal());

        assertThat(gqs.getEffectivePower(gd, marshal)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, marshal)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff opponent's creatures")
    void doesNotBuffOpponentCreatures() {
        harness.addToBattlefield(player1, new BenalishMarshal());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two Benalish Marshals buff each other")
    void twoMarshalsBuffEachOther() {
        harness.addToBattlefield(player1, new BenalishMarshal());
        harness.addToBattlefield(player1, new BenalishMarshal());

        List<Permanent> marshals = findPermanents(player1, "Benalish Marshal");

        assertThat(marshals).hasSize(2);
        for (Permanent marshal : marshals) {
            assertThat(gqs.getEffectivePower(gd, marshal)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, marshal)).isEqualTo(4);
        }
    }

    @Test
    @DisplayName("Two Benalish Marshals give +2/+2 to other creatures")
    void twoMarshalsStackBonuses() {
        harness.addToBattlefield(player1, new BenalishMarshal());
        harness.addToBattlefield(player1, new BenalishMarshal());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Bonus is removed when Benalish Marshal leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new BenalishMarshal());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Benalish Marshal"));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bonus applies when Benalish Marshal resolves onto battlefield")
    void bonusAppliesOnResolve() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        harness.castFromHand(player1, new BenalishMarshal(), "{W}{W}{W}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Static bonus survives end-of-turn modifier reset")
    void staticBonusSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new BenalishMarshal());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        bears.setPowerModifier(bears.getPowerModifier() + 3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6); // 2 base + 3 spell + 1 static

        bears.resetModifiers();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3); // 2 base + 1 static
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Creatures entering after Benalish Marshal receive the bonus immediately")
    void buffsCreaturesEnteringLater() {
        harness.addToBattlefield(player1, new BenalishMarshal());

        Permanent creature = harness.enterBattlefieldAndReturn(player1, new BenalishMarshal());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Benalish Marshal does not grant its bonus while on the stack")
    void doesNotBuffWhileOnStack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BenalishMarshal());

        harness.castFromHand(player1, new BenalishMarshal(), "{W}{W}{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Changing control of Benalish Marshal moves the bonus to its new controller")
    void bonusFollowsSourceController() {
        Permanent marshal = harness.addToBattlefieldAndReturn(player2, new BenalishMarshal());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new BenalishMarshal());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new BenalishMarshal());

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(4);

        harness.setHand(player1, List.of(new InBolassClutches()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castEnchantment(player1, 0, marshal.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(marshal);
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, marshal)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, marshal)).isEqualTo(4);
    }
}
