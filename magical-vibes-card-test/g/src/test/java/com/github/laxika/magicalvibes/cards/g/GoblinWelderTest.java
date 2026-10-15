package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.t.TickingGnomes;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinWelder.class, GrimMonolith.class, GrafdiggersCage.class, TickingGnomes.class})
class GoblinWelderTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Grafdigger's Cage simultaneously does not bypass its restriction")
    void sacrificedCageStillPreventsCreatureReturn() {
        Permanent welder = harness.addToBattlefieldAndReturn(player1, new GoblinWelder());
        welder.setSummoningSick(false);
        Permanent cage = harness.addToBattlefieldAndReturn(player1, new GrafdiggersCage());
        Card gnomes = new TickingGnomes();
        harness.setGraveyard(player1, List.of(gnomes));

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(cage.getId(), gnomes.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grafdigger's Cage");
        harness.assertInGraveyard(player1, "Grafdigger's Cage");
        harness.assertNotOnBattlefield(player1, "Ticking Gnomes");
        harness.assertInGraveyard(player1, "Ticking Gnomes");
    }

    @Test
    @DisplayName("A blocked return does not prevent sacrificing the other artifact")
    void sacrificesArtifactEvenWhenReturnIsBlocked() {
        Permanent welder = harness.addToBattlefieldAndReturn(player1, new GoblinWelder());
        welder.setSummoningSick(false);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new GrimMonolith());
        harness.addToBattlefield(player1, new GrafdiggersCage());
        Card gnomes = new TickingGnomes();
        harness.setGraveyard(player1, List.of(gnomes));

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(artifact.getId(), gnomes.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grim Monolith");
        harness.assertInGraveyard(player1, "Grim Monolith");
        harness.assertOnBattlefield(player1, "Grafdigger's Cage");
        harness.assertNotOnBattlefield(player1, "Ticking Gnomes");
        harness.assertInGraveyard(player1, "Ticking Gnomes");
    }

    @Test
    @DisplayName("The ability still resolves after Goblin Welder leaves the battlefield")
    void resolvesWithoutWelder() {
        Permanent welder = harness.addToBattlefieldAndReturn(player1, new GoblinWelder());
        welder.setSummoningSick(false);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new GrimMonolith());
        Card gnomes = new TickingGnomes();
        harness.setGraveyard(player1, List.of(gnomes));

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(artifact.getId(), gnomes.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(welder);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grim Monolith");
        harness.assertInGraveyard(player1, "Grim Monolith");
        harness.assertOnBattlefield(player1, "Ticking Gnomes");
        harness.assertNotInGraveyard(player1, "Ticking Gnomes");
    }

    @Test
    @DisplayName("Cannot activate the tap ability while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent welder = harness.addToBattlefieldAndReturn(player1, new GoblinWelder());
        welder.setSummoningSick(true);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new GrimMonolith());
        Card graveyardArtifact = new GrimMonolith();
        harness.setGraveyard(player1, List.of(graveyardArtifact));

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(artifact.getId(), graveyardArtifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does nothing if the artifact changes controller before resolution")
    void doesNothingWhenArtifactChangesController() {
        Permanent welder = harness.addToBattlefieldAndReturn(player1, new GoblinWelder());
        welder.setSummoningSick(false);
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new GrimMonolith());
        Card graveyardArtifact = new GrimMonolith();
        harness.setGraveyard(player1, List.of(graveyardArtifact));

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(artifact.getId(), graveyardArtifact.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(artifact);
        gd.playerBattlefields.get(player2.getId()).add(artifact);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(graveyardArtifact.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardArtifact);
    }

    @Test
    @DisplayName("Sacrifices a targeted artifact and returns the targeted artifact card")
    void exchangesArtifacts() {
        Permanent welder = harness.addToBattlefieldAndReturn(player1, new GoblinWelder());
        welder.setSummoningSick(false);
        Permanent battlefieldArtifact = harness.addToBattlefieldAndReturn(player1, new GrimMonolith());
        Card graveyardArtifact = new GrimMonolith();
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
        Permanent battlefieldArtifact = harness.addToBattlefieldAndReturn(player2, new GrimMonolith());
        Card graveyardArtifact = new GrimMonolith();
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
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new GrimMonolith());
        Card graveyardArtifact = new GrimMonolith();
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
        Permanent battlefieldArtifact = harness.addToBattlefieldAndReturn(player1, new GrimMonolith());
        Card graveyardArtifact = new GrimMonolith();
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

    @Test
    @DisplayName("Does nothing when the graveyard target is no longer legal")
    void doesNothingWhenGraveyardTargetLeaves() {
        Permanent welder = harness.addToBattlefieldAndReturn(player1, new GoblinWelder());
        welder.setSummoningSick(false);
        Permanent battlefieldArtifact = harness.addToBattlefieldAndReturn(player1, new GrimMonolith());
        Card graveyardArtifact = new GrimMonolith();
        harness.setGraveyard(player1, List.of(graveyardArtifact));

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(battlefieldArtifact.getId(), graveyardArtifact.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(graveyardArtifact);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(battlefieldArtifact.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(graveyardArtifact.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(battlefieldArtifact.getCard().getId()));
    }

    @Test
    @DisplayName("Cannot target a non-artifact card in a graveyard")
    void cannotTargetNonArtifactCardInGraveyard() {
        Permanent welder = harness.addToBattlefieldAndReturn(player1, new GoblinWelder());
        welder.setSummoningSick(false);
        Permanent battlefieldArtifact = harness.addToBattlefieldAndReturn(player1, new GrimMonolith());
        Card nonArtifactCard = new GoblinWelder();
        harness.setGraveyard(player1, List.of(nonArtifactCard));

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(battlefieldArtifact.getId(), nonArtifactCard.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
