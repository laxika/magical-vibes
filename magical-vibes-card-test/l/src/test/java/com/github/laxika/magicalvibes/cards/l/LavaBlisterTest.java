package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HallowedHealer;
import com.github.laxika.magicalvibes.cards.p.PetrifiedField;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LavaBlister.class, PetrifiedField.class, Forest.class, HallowedHealer.class})
class LavaBlisterTest extends BaseCardTest {

    @Test
    @DisplayName("The target land's controller takes 6 damage and the land survives")
    void targetControllerTakesDamageAndLandSurvives() {
        harness.addToBattlefield(player2, new PetrifiedField());
        harness.setHand(player1, List.of(new LavaBlister()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Petrified Field");
        harness.castAndResolveSorcery(player1, 0, targetId);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player2, "Petrified Field");
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("The caster is prompted when targeting their own nonbasic land")
    void casterCanBeTargetLandController() {
        harness.addToBattlefield(player1, new PetrifiedField());
        harness.setHand(player1, List.of(new LavaBlister()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        UUID targetId = harness.getPermanentId(player1, "Petrified Field");
        harness.castAndResolveSorcery(player1, 0, targetId);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Petrified Field");
        harness.assertLife(player1, 14);
    }

    @Test
    @DisplayName("The target land's controller declines and the land is destroyed")
    void decliningDestroysLand() {
        harness.addToBattlefield(player2, new PetrifiedField());
        harness.setHand(player1, List.of(new LavaBlister()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Petrified Field");
        harness.castAndResolveSorcery(player1, 0, targetId);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Petrified Field");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target a basic land")
    void cannotTargetBasicLand() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new LavaBlister()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the target land leaves before resolution")
    void fizzlesIfTargetLeaves() {
        harness.addToBattlefield(player2, new PetrifiedField());
        harness.setHand(player1, List.of(new LavaBlister()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Petrified Field");
        harness.castSorcery(player1, 0, targetId);
        GameData gameData = harness.getGameData();
        gameData.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gameData.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Choosing damage saves the land even when all six damage is prevented")
    void preventedDamageStillSavesLand() {
        for (int i = 0; i < 3; i++) {
            addCreatureReady(player1, new HallowedHealer());
            harness.activateAbility(player1, i, null, player2.getId());
            harness.passBothPriorities();
        }
        harness.addToBattlefield(player2, new PetrifiedField());
        harness.setHand(player1, List.of(new LavaBlister()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Petrified Field"));
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player2, "Petrified Field");
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Lava Blister");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        harness.addToBattlefield(player2, new HallowedHealer());
        harness.setHand(player1, List.of(new LavaBlister()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Hallowed Healer");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The caster cannot make the opposing land controller's choice")
    void casterCannotChooseForOpponent() {
        harness.addToBattlefield(player2, new PetrifiedField());
        harness.setHand(player1, List.of(new LavaBlister()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Petrified Field"));
        assertThatThrownBy(() -> harness.handleMayAbilityChosen(player1, true))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotOnBattlefield(player2, "Petrified Field");
        harness.assertInGraveyard(player2, "Petrified Field");
        harness.assertLife(player2, 20);
    }
}
