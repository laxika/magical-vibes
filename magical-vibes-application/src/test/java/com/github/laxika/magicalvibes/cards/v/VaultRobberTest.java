package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DemonBolt;
import com.github.laxika.magicalvibes.cards.f.FearlessPup;
import com.github.laxika.magicalvibes.cards.f.FrostBite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VaultRobber.class, FearlessPup.class, FrostBite.class, DemonBolt.class})
class VaultRobberTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature card and creates a Treasure token")
    void exilesCreatureAndCreatesTreasure() {
        Permanent robber = addCreatureReady(player1, new VaultRobber());
        FearlessPup creatureCard = new FearlessPup();
        harness.setGraveyard(player1, List.of(creatureCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int robberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(robber);
        harness.activateAbility(player1, robberIndex, null, null);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.GraveyardExileCostChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(creatureCard.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(creatureCard.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(robber.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot activate without a creature card in the graveyard")
    void cannotActivateWithoutCreatureCard() {
        Permanent robber = addCreatureReady(player1, new VaultRobber());
        harness.setGraveyard(player1, List.of(new FrostBite()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int robberIndex = gd.playerBattlefields.get(player1.getId()).indexOf(robber);
        assertThatThrownBy(() -> harness.activateAbility(player1, robberIndex, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Costs are paid before the Treasure ability resolves")
    void paysCostsBeforeResolution() {
        Permanent robber = addCreatureReady(player1, new VaultRobber());
        FearlessPup creature = new FearlessPup();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(robber.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanent(player1, "Treasure").isTapped()).isFalse();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Cannot exile a noncreature when a creature is also available")
    void rejectsNoncreatureChoice() {
        addCreatureReady(player1, new VaultRobber());
        FrostBite instant = new FrostBite();
        FearlessPup creature = new FearlessPup();
        harness.setGraveyard(player1, List.of(instant, creature));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(instant, creature);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("An opponent's creature graveyard cannot pay the cost")
    void cannotUseOpponentsGraveyard() {
        addCreatureReady(player1, new VaultRobber());
        harness.setGraveyard(player1, List.of());
        FearlessPup creature = new FearlessPup();
        harness.setGraveyard(player2, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
    }

    @Test
    @DisplayName("The Treasure ability resolves after Vault Robber is destroyed")
    void resolvesAfterSourceIsDestroyed() {
        Permanent robber = addCreatureReady(player1, new VaultRobber());
        harness.setGraveyard(player1, List.of(new FearlessPup()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.setHand(player2, List.of(new DemonBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, robber.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vault Robber");
        harness.assertInGraveyard(player1, "Vault Robber");
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("The created Treasure sacrifices for one mana of any color")
    void treasureProducesChosenColor(ManaColor color) {
        addCreatureReady(player1, new VaultRobber());
        harness.setGraveyard(player1, List.of(new FearlessPup()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent treasure = findPermanent(player1, "Treasure");
        int treasureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(treasure);
        harness.activateAbility(player1, treasureIndex, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Vault Robber cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new VaultRobber());
        harness.setGraveyard(player1, List.of(new FearlessPup()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Vault Robber cannot activate without the generic mana payment")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new VaultRobber());
        harness.setGraveyard(player1, List.of(new FearlessPup()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }
}
