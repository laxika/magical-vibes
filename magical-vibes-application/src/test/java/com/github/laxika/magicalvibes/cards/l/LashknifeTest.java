package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.k.KorHaven;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SilkenfistFighter;
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

@CardUsed({Lashknife.class, SilkenfistFighter.class, Plains.class, KorHaven.class})
class LashknifeTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has first strike")
    void enchantedCreatureHasFirstStrike() {
        Permanent creature = addCreatureReady(player1, new SilkenfistFighter());
        harness.setHand(player1, List.of(new Lashknife()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("May pay the normal mana cost when the alternate cost is available")
    void mayPayNormalManaCostWhenAlternateCostIsAvailable() {
        harness.addToBattlefield(player1, new Plains());
        Permanent target = addCreatureReady(player1, new SilkenfistFighter());
        Permanent costCreature = addCreatureReady(player1, new SilkenfistFighter());
        harness.setHand(player1, List.of(new Lashknife()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(costCreature.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Can enchant a creature an opponent controls")
    void canEnchantOpponentCreature() {
        Permanent creature = addCreatureReady(player2, new SilkenfistFighter());
        harness.setHand(player1, List.of(new Lashknife()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("May cast for the alternate cost by tapping an untapped creature with a Plains")
    void castsForAlternateCost() {
        harness.addToBattlefield(player1, new Plains());
        Permanent target = addCreatureReady(player1, new SilkenfistFighter());
        Permanent costCreature = addCreatureReady(player1, new SilkenfistFighter());
        harness.setHand(player1, List.of(new Lashknife()));

        harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of(costCreature.getId()));
        harness.passBothPriorities();

        assertThat(costCreature.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Alternate cost requires an untapped creature")
    void alternateCostRequiresUntappedCreature() {
        harness.addToBattlefield(player1, new Plains());
        Permanent creature = addCreatureReady(player1, new SilkenfistFighter());
        creature.tap();
        harness.setHand(player1, List.of(new Lashknife()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(
                player1, 0, creature.getId(), List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Alternate cost requires a creature to tap")
    void alternateCostRequiresCreature() {
        harness.addToBattlefield(player1, new Plains());
        Permanent target = addCreatureReady(player1, new SilkenfistFighter());
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player1, new KorHaven());
        harness.setHand(player1, List.of(new Lashknife()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(
                player1, 0, target.getId(), List.of(nonCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Alternate cost requires control of a Plains")
    void alternateCostRequiresPlains() {
        harness.addToBattlefield(player2, new Plains());
        Permanent target = addCreatureReady(player1, new SilkenfistFighter());
        Permanent costCreature = addCreatureReady(player1, new SilkenfistFighter());
        harness.setHand(player1, List.of(new Lashknife()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(
                player1, 0, target.getId(), List.of(costCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("condition is not met");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new KorHaven());
        harness.setHand(player1, List.of(new Lashknife()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent nonCreature = findPermanent(player1, "Kor Haven");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
