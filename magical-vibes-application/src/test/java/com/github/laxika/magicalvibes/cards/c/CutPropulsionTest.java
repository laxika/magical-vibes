package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.PyromancersGauntlet;
import com.github.laxika.magicalvibes.cards.w.WallOfAir;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CutPropulsion.class, GiantSpider.class, WallOfAir.class, Plains.class, PyromancersGauntlet.class})
class CutPropulsionTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature deals damage to itself equal to its power")
    void dealsPowerDamageToNonFlyingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        cast(target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("A flying target deals twice its power as damage to itself")
    void dealsDoublePowerDamageToFlyingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WallOfAir());

        cast(target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target only a creature")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new CutPropulsion()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Spell-only damage bonuses do not increase the creature's self-damage")
    void ignoresSpellOnlyDamageBonus() {
        harness.addToBattlefield(player1, new PyromancersGauntlet());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        cast(target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Uses the creature's power when the spell resolves")
    void usesPowerAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new CutPropulsion()));
        addMana();
        harness.castInstant(player1, 0, target.getId());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Checks flying when the spell resolves")
    void checksFlyingAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WallOfAir());
        harness.setHand(player1, List.of(new CutPropulsion()));
        addMana();
        harness.castInstant(player1, 0, target.getId());
        target.getRemovedKeywords().add(Keyword.FLYING);

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new CutPropulsion()));
        addMana();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
