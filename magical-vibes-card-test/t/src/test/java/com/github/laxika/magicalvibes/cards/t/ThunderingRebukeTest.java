package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CanopyBaloth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.j.JaceMirrorMage;
import com.github.laxika.magicalvibes.cards.v.VeteranAdventurer;
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

@CardUsed({ThunderingRebuke.class, CanopyBaloth.class, Forest.class, IntoTheRoil.class,
        JaceMirrorMage.class, VeteranAdventurer.class})
class ThunderingRebukeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to a target creature")
    void dealsDamageToCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CanopyBaloth());
        harness.setHand(player1, List.of(new ThunderingRebuke()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Canopy Baloth");
    }

    @Test
    @DisplayName("Deals 4 damage to a target planeswalker")
    void dealsDamageToPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceMirrorMage());
        planeswalker.setCounterCount(CounterType.LOYALTY, 8);
        harness.setHand(player1, List.of(new ThunderingRebuke()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ThunderingRebuke()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Marks exactly four damage on a surviving creature")
    void marksExactlyFourDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new VeteranAdventurer());
        harness.setHand(player1, List.of(new ThunderingRebuke()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Veteran Adventurer");
        assertThat(creature.getMarkedDamage()).isEqualTo(4);
        harness.assertInGraveyard(player1, "Thundering Rebuke");
    }

    @Test
    @DisplayName("Can target a creature controlled by the caster")
    void canTargetOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CanopyBaloth());
        harness.setHand(player1, List.of(new ThunderingRebuke()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Canopy Baloth");
        harness.assertNotOnBattlefield(player1, "Canopy Baloth");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new ThunderingRebuke()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Lethal damage puts a planeswalker into its owner's graveyard")
    void killsPlaneswalkerAtFourLoyalty() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceMirrorMage());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new ThunderingRebuke()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, planeswalker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Jace, Mirror Mage");
        harness.assertNotOnBattlefield(player2, "Jace, Mirror Mage");
    }

    @Test
    @DisplayName("Deals no damage when the target returns to hand before resolution")
    void targetLeavesBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new VeteranAdventurer());
        harness.setHand(player1, List.of(new ThunderingRebuke()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorcery(player1, 0, creature.getId());

        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertInHand(player2, "Veteran Adventurer");
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertInHand(player2, "Veteran Adventurer");
        harness.assertNotInGraveyard(player2, "Veteran Adventurer");
        harness.assertInGraveyard(player1, "Thundering Rebuke");
        assertThat(gd.stack).isEmpty();
    }
}
