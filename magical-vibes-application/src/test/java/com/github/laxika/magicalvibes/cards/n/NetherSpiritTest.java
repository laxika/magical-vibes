package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.s.ShockTroops;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NetherSpirit.class, ShockTroops.class, Swamp.class})
class NetherSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Offers to return when it is the only creature card in the graveyard")
    void triggersWhenItIsTheOnlyCreatureCard() {
        NetherSpirit spirit = new NetherSpirit();
        harness.setGraveyard(player1, List.of(spirit, new Swamp()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.pendingMayAbilities.getFirst().sourceCard().getId()).isEqualTo(spirit.getId());
    }

    @Test
    @DisplayName("Does not trigger when another creature card is in the graveyard")
    void doesNotTriggerWithAnotherCreatureCard() {
        harness.setGraveyard(player1, List.of(new NetherSpirit(), new ShockTroops()));

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

    @Test
    @DisplayName("A creature card in an opponent's graveyard does not prevent the trigger")
    void ignoresOpponentsCreatureCards() {
        NetherSpirit spirit = new NetherSpirit();
        harness.setGraveyard(player1, List.of(spirit));
        harness.setGraveyard(player2, List.of(new ShockTroops()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.pendingMayAbilities).hasSize(1);
    }

    @Test
    @DisplayName("Does not return when another creature enters the graveyard before resolution")
    void doesNotReturnIfConditionChangesBeforeResolution() {
        NetherSpirit spirit = new NetherSpirit();
        harness.setGraveyard(player1, List.of(spirit));

        advanceToUpkeep(player1);
        harness.setGraveyard(player1, List.of(spirit, new ShockTroops()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(spirit.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(spirit.getId()));
    }
}
