package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CopperCarapace;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.ExquisiteBlood;
import com.github.laxika.magicalvibes.cards.e.EverflowingChalice;
import com.github.laxika.magicalvibes.cards.i.IllustriousWanderglyph;
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

@CardUsed({RuinousIntrusion.class, CopperCarapace.class, GrizzlyBears.class,
        ExquisiteBlood.class, EverflowingChalice.class, IllustriousWanderglyph.class})
class RuinousIntrusionTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles an artifact and puts counters equal to its mana value on a creature you control")
    void exilesArtifactAndPutsManaValueCountersOnCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CopperCarapace());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castRuinousIntrusion(artifact, creature);

        harness.assertNotOnBattlefield(player2, "Copper Carapace");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Copper Carapace");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Exiles the permanent even when the creature target is illegal at resolution")
    void exilesPermanentWhenCreatureTargetLeaves() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CopperCarapace());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RuinousIntrusion()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, List.of(artifact.getId(), creature.getId()));

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Copper Carapace");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Copper Carapace");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
    }

    @Test
    @DisplayName("Requires an artifact or enchantment and a creature you control")
    void rejectsInvalidTargetTypes() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new CopperCarapace());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RuinousIntrusion()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(artifact.getId(), opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exilesEnchantmentAndAddsFiveCounters() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new ExquisiteBlood());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IllustriousWanderglyph());

        castRuinousIntrusion(enchantment, creature);

        harness.assertNotOnBattlefield(player2, "Exquisite Blood");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(enchantment.getCard());
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void addsNoCountersWhenExileTargetLeavesBeforeResolution() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new ExquisiteBlood());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IllustriousWanderglyph());
        harness.setHand(player1, List.of(new RuinousIntrusion()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, List.of(enchantment.getId(), creature.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(enchantment);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(enchantment.getCard());
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Ruinous Intrusion");
    }

    @Test
    void exilesZeroManaValueArtifactWithoutAddingCounters() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new EverflowingChalice());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IllustriousWanderglyph());

        castRuinousIntrusion(artifact, creature);

        harness.assertNotOnBattlefield(player2, "Everflowing Chalice");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(artifact.getCard());
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void canTargetSameArtifactCreatureForBothTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IllustriousWanderglyph());

        castRuinousIntrusion(creature, creature);

        harness.assertNotOnBattlefield(player1, "Illustrious Wanderglyph");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature.getCard());
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castRuinousIntrusion(Permanent permanent, Permanent creature) {
        harness.setHand(player1, List.of(new RuinousIntrusion()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0, List.of(permanent.getId(), creature.getId()));
    }
}
