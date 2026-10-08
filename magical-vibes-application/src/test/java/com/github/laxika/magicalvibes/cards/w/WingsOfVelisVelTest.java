package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CloudgoatRanger;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.Lignify;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WingsOfVelisVel.class, CloudgoatRanger.class, WizenedCenn.class, Island.class, Lignify.class})
class WingsOfVelisVelTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Wings of Velis Vel puts it on the stack with target creature")
    void castingPutsOnStack() {
        harness.addToBattlefield(player1, new CloudgoatRanger());
        harness.setHand(player1, List.of(new WingsOfVelisVel()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID rangerId = harness.getPermanentId(player1, "Cloudgoat Ranger");
        harness.castInstant(player1, 0, rangerId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(rangerId);
    }

    @Test
    @DisplayName("Resolving sets base P/T to 4/4 and grants flying to target creature")
    void setsBasePowerToughnessAndGrantsFlying() {
        harness.addToBattlefield(player1, new CloudgoatRanger());
        harness.setHand(player1, List.of(new WingsOfVelisVel()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID rangerId = harness.getPermanentId(player1, "Cloudgoat Ranger");
        harness.castAndResolveInstant(player1, 0, rangerId);

        Permanent ranger = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(ranger.isBasePowerToughnessOverriddenUntilEndOfTurn()).isTrue();
        assertThat(ranger.getEffectivePower()).isEqualTo(4);
        assertThat(ranger.getEffectiveToughness()).isEqualTo(4);
        // The flying grant is a floating CR 613 layer-6 effect, visible through the query layer.
        assertThat(gqs.hasKeyword(gd, ranger, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentsCreature() {
        harness.addToBattlefield(player2, new CloudgoatRanger());
        harness.setHand(player1, List.of(new WingsOfVelisVel()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID rangerId = harness.getPermanentId(player2, "Cloudgoat Ranger");
        harness.castAndResolveInstant(player1, 0, rangerId);

        Permanent ranger = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, ranger)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ranger)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ranger, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Target gains all creature types, so Wizened Cenn buffs it")
    void gainsAllCreatureTypes() {
        harness.addToBattlefield(player1, new WizenedCenn());
        harness.addToBattlefield(player1, new CloudgoatRanger());
        harness.setHand(player1, List.of(new WingsOfVelisVel()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        Permanent ranger = findPermanent(player1, "Cloudgoat Ranger");
        assertThat(gqs.getEffectivePower(gd, ranger)).isEqualTo(3); // not a Kithkin yet

        UUID rangerId = harness.getPermanentId(player1, "Cloudgoat Ranger");
        harness.castAndResolveInstant(player1, 0, rangerId);

        // Base 4/4 as a Kithkin (changeling) + Wizened Cenn's +1/+1
        assertThat(gqs.getEffectivePower(gd, ranger)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ranger)).isEqualTo(5);
    }

    @Test
    @DisplayName("Effects wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new CloudgoatRanger());
        harness.setHand(player1, List.of(new WingsOfVelisVel()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID rangerId = harness.getPermanentId(player1, "Cloudgoat Ranger");
        harness.castAndResolveInstant(player1, 0, rangerId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent ranger = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(ranger.isBasePowerToughnessOverriddenUntilEndOfTurn()).isFalse();
        assertThat(ranger.getEffectivePower()).isEqualTo(3);
        assertThat(ranger.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ranger, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        // A creature must exist so the spell has a legal target and is castable (CR 601.2c);
        // targeting the noncreature is then rejected by the spell's target-type validation.
        harness.addToBattlefield(player1, new CloudgoatRanger());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new WingsOfVelisVel()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID targetId = harness.getPermanentId(player1, "Island");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Wings applied after Lignify restores all creature types, flying, and base 4/4")
    void appliedAfterLignify() {
        Permanent ranger = harness.addToBattlefieldAndReturn(player1, new CloudgoatRanger());
        harness.setHand(player1, List.of(new Lignify(), new WingsOfVelisVel()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, ranger.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, ranger.getId());

        assertThat(gqs.hasEffectiveSubtype(gd, ranger, CardSubtype.KITHKIN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, ranger, CardSubtype.GIANT)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ranger)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ranger)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ranger, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasEffectiveSubtype(gd, ranger, CardSubtype.KITHKIN)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, ranger, CardSubtype.TREEFOLK)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ranger)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, ranger)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ranger, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Lignify applied after Wings replaces the granted creature types, abilities, and base power")
    void lignifyAppliedAfterWings() {
        Permanent ranger = harness.addToBattlefieldAndReturn(player1, new CloudgoatRanger());
        harness.setHand(player1, List.of(new WingsOfVelisVel(), new Lignify()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, ranger.getId());
        harness.castEnchantment(player1, 0, ranger.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasEffectiveSubtype(gd, ranger, CardSubtype.KITHKIN)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, ranger, CardSubtype.GIANT)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, ranger, CardSubtype.TREEFOLK)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ranger)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, ranger)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ranger, Keyword.FLYING)).isFalse();
    }
}
