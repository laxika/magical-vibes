package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.Cankerbloom;
import com.github.laxika.magicalvibes.cards.c.CopperLonglegs;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NissaAscendedAnimist;
import com.github.laxika.magicalvibes.cards.t.TerramorphicExpanse;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Ossification.class, Forest.class, CopperLonglegs.class, NissaAscendedAnimist.class,
        TerramorphicExpanse.class, Cankerbloom.class})
class OssificationTest extends BaseCardTest {

    private void castAndResolve(UUID landId, UUID targetId) {
        prepareCast();

        harness.castEnchantment(player1, 0, List.of(landId, targetId));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB exiles an opposing creature")
    void etbExilesOpposingCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());

        castAndResolve(forest.getId(), creature.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(creature.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Copper Longlegs"));
    }

    @Test
    @DisplayName("ETB exiles an opposing planeswalker")
    void etbExilesOpposingPlaneswalker() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new NissaAscendedAnimist());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);

        castAndResolve(forest.getId(), planeswalker.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(planeswalker.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Nissa, Ascended Animist"));
    }

    @Test
    @DisplayName("Exiled permanent returns when Ossification leaves")
    void exiledPermanentReturnsWhenAuraLeaves() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());
        castAndResolve(forest.getId(), creature.getId());

        harness.addToBattlefield(player2, new Cankerbloom());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        UUID ossificationId = harness.getPermanentId(player1, "Ossification");
        harness.activateAbility(player2, 0, 1, null, ossificationId);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Copper Longlegs");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Copper Longlegs"));
    }

    @Test
    @DisplayName("Cannot enchant a land without the basic supertype")
    void cannotEnchantNonbasicLand() {
        Permanent nonbasicLand = harness.addToBattlefieldAndReturn(player1, new TerramorphicExpanse());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());
        harness.setHand(player1, List.of(new Ossification()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0,
                List.of(nonbasicLand.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot exile a creature controlled by the caster")
    void cannotExileOwnCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        harness.setHand(player1, List.of(new Ossification()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0,
                List.of(forest.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or planeswalker an opponent controls");
    }

    @Test
    void cannotEnchantOpponentsBasicLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());
        prepareCast();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0,
                List.of(land.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotEnchantOwnCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new CopperLonglegs());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());
        prepareCast();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0,
                List.of(ownCreature.getId(), opposingCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotExileOpponentsLand() {
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareCast();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0,
                List.of(ownLand.getId(), opposingLand.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canResolveWithoutAnOpposingCreatureOrPlaneswalker() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        prepareCast();

        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ossification");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void choosesExileTargetAfterAuraResolves() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        prepareCast();
        harness.castEnchantment(player1, 0, land.getId());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ossification");
        harness.assertNotOnBattlefield(player2, "Copper Longlegs");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(creature.getCard().getId()));
    }

    @Test
    void doesNotExileWhenAuraLeavesBeforeTriggerResolves() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());
        prepareCast();
        harness.castEnchantment(player1, 0, List.of(land.getId(), creature.getId()));
        harness.passBothPriorities();

        harness.addToBattlefield(player1, new Cankerbloom());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 2, 1, null,
                harness.getPermanentId(player1, "Ossification"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ossification");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(creature.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void targetLeavingBeforeTriggerResolvesDoesNotRemoveAura() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CopperLonglegs());
        prepareCast();
        harness.castEnchantment(player1, 0, List.of(land.getId(), creature.getId()));
        harness.passBothPriorities();

        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Copper Longlegs");
        harness.assertOnBattlefield(player1, "Ossification");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void returnedPlaneswalkerIsANewPermanentWithStartingLoyalty() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new NissaAscendedAnimist());
        planeswalker.setCounterCount(CounterType.LOYALTY, 1);
        castAndResolve(land.getId(), planeswalker.getId());

        harness.addToBattlefield(player2, new Cankerbloom());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, 1, null,
                harness.getPermanentId(player1, "Ossification"));
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(planeswalker.getCard().getId()))
                .findFirst().orElseThrow();
        assertThat(returned.getId()).isNotEqualTo(planeswalker.getId());
        assertThat(returned.getCounterCount(CounterType.LOYALTY)).isEqualTo(7);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void cannotExileOwnPlaneswalker() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new NissaAscendedAnimist());
        planeswalker.setCounterCount(CounterType.LOYALTY, 7);
        prepareCast();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0,
                List.of(land.getId(), planeswalker.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Ossification()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
