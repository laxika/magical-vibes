package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NetherSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Offers to return when it is the only creature card in the graveyard")
    void triggersWhenItIsTheOnlyCreatureCard() {
        NetherSpirit spirit = new NetherSpirit();
        harness.setGraveyard(player1, List.of(spirit, new Shock()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.pendingMayAbilities.getFirst().sourceCard().getId()).isEqualTo(spirit.getId());
    }

    @Test
    @DisplayName("Does not trigger when another creature card is in the graveyard")
    void doesNotTriggerWithAnotherCreatureCard() {
        harness.setGraveyard(player1, List.of(new NetherSpirit(), new GrizzlyBears()));

        advanceToUpkeep(player1);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Accepting the trigger returns Nether Spirit to the battlefield")
    void acceptingReturnsItToBattlefield() {
        NetherSpirit spirit = new NetherSpirit();
        harness.setGraveyard(player1, List.of(spirit));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(spirit.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(spirit.getId()));
    }

    @Test
    @DisplayName("Declining the trigger keeps Nether Spirit in the graveyard")
    void decliningKeepsItInGraveyard() {
        NetherSpirit spirit = new NetherSpirit();
        harness.setGraveyard(player1, List.of(spirit));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(spirit.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(spirit.getId()));
    }
}
