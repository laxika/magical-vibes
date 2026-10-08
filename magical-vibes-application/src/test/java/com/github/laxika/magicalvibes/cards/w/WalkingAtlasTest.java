package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KhalniGarden;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WalkingAtlas.class, Forest.class, GrizzlyBears.class, KhalniGarden.class})
class WalkingAtlasTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Walking Atlas presents a may choice")
    void tappingPresentsMayChoice() {
        Permanent atlas = addReadyAtlas(player1);
        harness.setHand(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(atlas.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the choice offers only lands and puts the chosen land onto the battlefield")
    void acceptsOnlyLandAndPutsItOntoBattlefield() {
        addReadyAtlas(player1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices())
                .containsExactly(1);

        harness.handleCardChosen(player1, 1);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the choice leaves the hand unchanged")
    void decliningLeavesHandUnchanged() {
        addReadyAtlas(player1);
        harness.setHand(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Cannot activate Walking Atlas with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new WalkingAtlas());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    private Permanent addReadyAtlas(Player player) {
        Permanent atlas = harness.addToBattlefieldAndReturn(player, new WalkingAtlas());
        atlas.setSummoningSick(false);
        return atlas;
    }

    @Test
    @DisplayName("Accepting with no lands in hand finishes without moving a nonland")
    void acceptingWithoutLandsDoesNothing() {
        addReadyAtlas(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Accepting with an empty hand finishes normally")
    void acceptingWithEmptyHandDoesNothing() {
        addReadyAtlas(player1);
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Putting a land does not consume a land play and works after the normal land play")
    void putsExactlyOneLandAfterNormalLandPlay() {
        addReadyAtlas(player1);
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.playLand(player1, 0);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() instanceof Forest).hasSize(2)
                .allSatisfy(p -> assertThat(p.isTapped()).isFalse());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Putting a nonbasic land preserves its tapped entry and entry trigger")
    void putsNonbasicLandWithItsEntryAbilities() {
        addReadyAtlas(player1);
        harness.setHand(player1, List.of(new KhalniGarden()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, harness.getPermanentId(player1, "Khalni Garden")).isTapped())
                .isTrue();
        harness.assertOnBattlefield(player1, "Plant");
        harness.assertNotInHand(player1, "Khalni Garden");
    }

    @Test
    @DisplayName("The controller may put a land on an opponent's turn")
    void canPutLandOnOpponentsTurn() {
        addReadyAtlas(player1);
        harness.setHand(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.ensurePriority(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotInHand(player1, "Forest");
        harness.assertInHand(player2, "Forest");
        harness.assertNotOnBattlefield(player2, "Forest");
    }
}
