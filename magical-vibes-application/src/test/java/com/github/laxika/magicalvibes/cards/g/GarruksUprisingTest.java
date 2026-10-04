package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.s.SkysovereignConsulFlagship;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GarruksUprising.class, AirElemental.class, GrizzlyBears.class})
class GarruksUprisingTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when it enters while its controller has a power-4 creature")
    void drawsOnEnterWithPower4Creature() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.setHand(player1, List.of(new GarruksUprising()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw on entry without a power-4 creature")
    void doesNotDrawOnEnterWithoutPower4Creature() {
        harness.setHand(player1, List.of(new GarruksUprising()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Gives creatures its controller controls trample")
    void grantsTrampleToOwnCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new AirElemental());
        Permanent opponentCreature = addCreatureReady(player2, new AirElemental());
        harness.addToBattlefield(player1, new GarruksUprising());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Draws when a power-4 creature enters under its controller's control")
    void drawsWhenPower4CreatureEnters() {
        harness.addToBattlefield(player1, new GarruksUprising());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when a creature with power less than 4 enters")
    void doesNotDrawWhenPowerIsLessThan4() {
        harness.addToBattlefield(player1, new GarruksUprising());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.stack).isEmpty();
        assertThat(gameData.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @CardUsed({SkysovereignConsulFlagship.class})
    void uncrewedVehicleDoesNotSatisfyEntryCondition() {
        harness.addToBattlefield(player1, new SkysovereignConsulFlagship());
        harness.setHand(player1, List.of(new GarruksUprising()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @CardUsed({Unsummon.class, SkysovereignConsulFlagship.class})
    void uncrewedVehicleDoesNotKeepEntryConditionTrueAtResolution() {
        Permanent creature = addCreatureReady(player1, new AirElemental());
        harness.addToBattlefield(player1, new SkysovereignConsulFlagship());
        harness.setHand(player1, List.of(new GarruksUprising(), new Unsummon()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature.getCard());
    }

    @Test
    @CardUsed({Unsummon.class})
    void entryDrawRequiresQualifyingCreatureAtResolution() {
        Permanent creature = addCreatureReady(player1, new AirElemental());
        harness.setHand(player1, List.of(new GarruksUprising(), new Unsummon()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature.getCard());
    }

    @Test
    @CardUsed({Unsummon.class})
    void creatureEntryDrawStillResolvesAfterCreatureLeaves() {
        harness.addToBattlefield(player1, new GarruksUprising());
        harness.setHand(player1, List.of(new AirElemental(), new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        Permanent creature = gd.playerBattlefields.get(player1.getId()).getLast();

        harness.castAndResolveInstant(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2).contains(creature.getCard());
    }

    @Test
    void opponentCreatureEntryDoesNotDraw() {
        harness.addToBattlefield(player1, new GarruksUprising());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new AirElemental()));
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void entryDrawsOnlyOnceWithMultipleQualifyingCreatures() {
        harness.addToBattlefield(player1, new AirElemental());
        harness.addToBattlefield(player1, new AirElemental());
        harness.setHand(player1, List.of(new GarruksUprising()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @CardUsed({GloriousAnthem.class})
    void creatureEntryUsesPowerIncludingStaticBonuses() {
        harness.addToBattlefield(player1, new GarruksUprising());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void trampleIsLostWhenUprisingLeaves() {
        Permanent creature = addCreatureReady(player1, new AirElemental());
        Permanent uprising = harness.addToBattlefieldAndReturn(player1, new GarruksUprising());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(uprising);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @CardUsed({Opalescence.class})
    void animatedUprisingGrantsItselfTrample() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent uprising = harness.addToBattlefieldAndReturn(player1, new GarruksUprising());

        assertThat(gqs.isCreature(gd, uprising)).isTrue();
        assertThat(gqs.hasKeyword(gd, uprising, Keyword.TRAMPLE)).isTrue();
    }
}
