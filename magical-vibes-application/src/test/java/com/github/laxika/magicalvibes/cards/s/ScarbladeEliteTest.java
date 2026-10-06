package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.IndomitableAncients;
import com.github.laxika.magicalvibes.cards.m.MothdustChangeling;
import com.github.laxika.magicalvibes.cards.r.RusticClachan;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScarbladeElite.class, IndomitableAncients.class, RusticClachan.class, MothdustChangeling.class})
class ScarbladeEliteTest extends BaseCardTest {

    private Permanent setup() {
        return addCreatureReady(player1, new ScarbladeElite());
    }

    private int idxOf(Permanent p) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(p);
    }

    @Test
    @DisplayName("Activating prompts to choose an Assassin card to exile")
    void promptsForAssassinExile() {
        Permanent elite = setup();
        harness.setGraveyard(player1, List.of(new ScarbladeElite()));
        UUID targetId = addCreatureReady(player2, new IndomitableAncients()).getId();

        harness.activateAbility(player1, idxOf(elite), 0, null, targetId);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.GraveyardExileCostChoice.class);
    }

    @Test
    @DisplayName("Only Assassin cards are valid to exile as the cost")
    void onlyAssassinCardsAreValid() {
        Permanent elite = setup();
        // Graveyard: index 0 non-Assassin (Indomitable Ancients), index 1 Assassin (Scarblade Elite)
        harness.setGraveyard(player1, List.of(new IndomitableAncients(), new ScarbladeElite()));
        UUID targetId = addCreatureReady(player2, new IndomitableAncients()).getId();

        harness.activateAbility(player1, idxOf(elite), 0, null, targetId);

        PendingInteraction.GraveyardExileCostChoice choice =
                (PendingInteraction.GraveyardExileCostChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(1);
    }

    @Test
    @DisplayName("Exiles the chosen Assassin and destroys the target creature")
    void exilesAssassinAndDestroysTarget() {
        Permanent elite = setup();
        harness.setGraveyard(player1, List.of(new ScarbladeElite()));
        UUID targetId = addCreatureReady(player2, new IndomitableAncients()).getId();

        harness.activateAbility(player1, idxOf(elite), 0, null, targetId);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(elite.isTapped()).isTrue();

        // Assassin card exiled from graveyard
        harness.assertNotInGraveyard(player1, "Scarblade Elite");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Scarblade Elite"));

        harness.passBothPriorities();

        // Indomitable Ancients destroyed
        harness.assertNotOnBattlefield(player2, "Indomitable Ancients");
        harness.assertInGraveyard(player2, "Indomitable Ancients");
    }

    @Test
    @DisplayName("Cannot activate without an Assassin card in graveyard")
    void cannotActivateWithoutAssassinInGraveyard() {
        Permanent elite = setup();
        harness.setGraveyard(player1, List.of(new IndomitableAncients()));
        UUID targetId = addCreatureReady(player2, new IndomitableAncients()).getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, idxOf(elite), 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a player — the ability only targets creatures")
    void cannotTargetPlayer() {
        Permanent elite = setup();
        harness.setGraveyard(player1, List.of(new ScarbladeElite()));

        assertThatThrownBy(() -> harness.activateAbility(player1, idxOf(elite), 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent elite = setup();
        harness.setGraveyard(player1, List.of(new ScarbladeElite()));
        Permanent land = harness.addToBattlefieldAndReturn(player2, new RusticClachan());

        assertThatThrownBy(() -> harness.activateAbility(player1, idxOf(elite), 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canExileChangelingAsAnAssassin() {
        Permanent elite = setup();
        harness.setGraveyard(player1, List.of(new MothdustChangeling()));
        UUID targetId = addCreatureReady(player2, new IndomitableAncients()).getId();

        harness.activateAbility(player1, idxOf(elite), 0, null, targetId);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.assertNotInGraveyard(player1, "Mothdust Changeling");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Mothdust Changeling"));
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Indomitable Ancients");
        harness.assertInGraveyard(player2, "Indomitable Ancients");
    }

    @Test
    void cannotPayWithOpponentsAssassin() {
        Permanent elite = setup();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new ScarbladeElite()));
        UUID targetId = addCreatureReady(player2, new IndomitableAncients()).getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, idxOf(elite), 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
        assertThat(elite.isTapped()).isFalse();
        harness.assertInGraveyard(player2, "Scarblade Elite");
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent elite = setup();
        elite.setSummoningSick(true);
        harness.setGraveyard(player1, List.of(new ScarbladeElite()));
        UUID targetId = addCreatureReady(player2, new IndomitableAncients()).getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, idxOf(elite), 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Scarblade Elite");
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent elite = setup();
        elite.setTapped(true);
        harness.setGraveyard(player1, List.of(new ScarbladeElite()));
        UUID targetId = addCreatureReady(player2, new IndomitableAncients()).getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, idxOf(elite), 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Scarblade Elite");
    }

    @Test
    void canTargetItself() {
        Permanent elite = setup();
        harness.setGraveyard(player1, List.of(new ScarbladeElite()));

        harness.activateAbility(player1, idxOf(elite), 0, null, elite.getId());
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Scarblade Elite");
        harness.assertInGraveyard(player1, "Scarblade Elite");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Scarblade Elite"));
    }
}
