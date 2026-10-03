package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SnappingGnarlid;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ClutchOfCurrents.class, Forest.class, SnappingGnarlid.class})
class ClutchOfCurrentsTest extends BaseCardTest {

    @Test
    void returnsTargetCreatureToItsOwnersHand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SnappingGnarlid());
        harness.setHand(player1, List.of(new ClutchOfCurrents()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player2, "Snapping Gnarlid");
        harness.assertInHand(player2, "Snapping Gnarlid");
    }

    @Test
    void alternateCastReturnsCreatureAndAwakensTargetLand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SnappingGnarlid());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new ClutchOfCurrents()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null,
                List.of(creature.getId(), land.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Snapping Gnarlid");
        harness.assertInHand(player2, "Snapping Gnarlid");
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);
        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.ELEMENTAL)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(land.getCard().hasType(CardType.LAND)).isTrue();
    }

    @Test
    void alternateCastRequiresAwakenTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SnappingGnarlid());
        harness.setHand(player1, List.of(new ClutchOfCurrents()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> gs.playCardWithAlternateCost(
                gd, player1, 0, 0, creature.getId(), null, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("additional targets");
    }

    @Test
    void normalCastCannotChooseAnAwakenTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SnappingGnarlid());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new ClutchOfCurrents()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(creature.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void awakenedLandCanBeBothTargetsOfAnotherAwakenSpell() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SnappingGnarlid());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new ClutchOfCurrents(), new ClutchOfCurrents()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null,
                List.of(creature.getId(), land.getId()));
        harness.passBothPriorities();
        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null,
                List.of(land.getId(), land.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void stillAwakensLandWhenCreatureTargetLeavesBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SnappingGnarlid());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new ClutchOfCurrents()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null,
                List.of(creature.getId(), land.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        harness.assertNotInHand(player2, "Snapping Gnarlid");
    }

    @Test
    void stillReturnsCreatureWhenAwakenTargetLeavesBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SnappingGnarlid());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new ClutchOfCurrents()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null,
                List.of(creature.getId(), land.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(land);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Snapping Gnarlid");
        harness.assertInHand(player2, "Snapping Gnarlid");
    }
}
