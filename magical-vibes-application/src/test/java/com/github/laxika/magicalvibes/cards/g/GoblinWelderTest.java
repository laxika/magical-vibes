package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DarksteelJuggernaut;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinWelderTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a targeted artifact and returns the targeted artifact card")
    void exchangesArtifacts() {
        Permanent welder = harness.addToBattlefieldAndReturn(player1, new GoblinWelder());
        welder.setSummoningSick(false);
        Permanent battlefieldArtifact = harness.addToBattlefieldAndReturn(player1, new DarksteelJuggernaut());
        Card graveyardArtifact = new DarksteelJuggernaut();
        harness.setGraveyard(player1, List.of(graveyardArtifact));

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(battlefieldArtifact.getId(), graveyardArtifact.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(graveyardArtifact.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(battlefieldArtifact.getCard().getId()));
    }

    @Test
    @DisplayName("Returns an opponent's graveyard artifact under that player's control")
    void exchangesAnOpponentsArtifacts() {
        Permanent welder = harness.addToBattlefieldAndReturn(player1, new GoblinWelder());
        welder.setSummoningSick(false);
        Permanent battlefieldArtifact = harness.addToBattlefieldAndReturn(player2, new DarksteelJuggernaut());
        Card graveyardArtifact = new DarksteelJuggernaut();
        harness.setGraveyard(player2, List.of(graveyardArtifact));

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(battlefieldArtifact.getId(), graveyardArtifact.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(graveyardArtifact.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(battlefieldArtifact.getCard().getId()));
    }

    @Test
    @DisplayName("Requires both artifacts to belong to the same player")
    void requiresMatchingArtifactControllerAndGraveyardOwner() {
        Permanent welder = harness.addToBattlefieldAndReturn(player1, new GoblinWelder());
        welder.setSummoningSick(false);
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new DarksteelJuggernaut());
        Card graveyardArtifact = new DarksteelJuggernaut();
        harness.setGraveyard(player1, List.of(graveyardArtifact));

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(opponentArtifact.getId(), graveyardArtifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does nothing when the battlefield target is no longer legal")
    void doesNothingWhenBattlefieldTargetLeaves() {
        Permanent welder = harness.addToBattlefieldAndReturn(player1, new GoblinWelder());
        welder.setSummoningSick(false);
        Permanent battlefieldArtifact = harness.addToBattlefieldAndReturn(player1, new DarksteelJuggernaut());
        Card graveyardArtifact = new DarksteelJuggernaut();
        harness.setGraveyard(player1, List.of(graveyardArtifact));

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(battlefieldArtifact.getId(), graveyardArtifact.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(battlefieldArtifact);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(graveyardArtifact.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(graveyardArtifact.getId()));
    }
}
