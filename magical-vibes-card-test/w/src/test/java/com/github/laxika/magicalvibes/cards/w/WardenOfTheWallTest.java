package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WardenOfTheWall.class})
class WardenOfTheWallTest extends BaseCardTest {

    // ===== Enters tapped =====

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new WardenOfTheWall()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent warden = findWarden(player1);
        assertThat(warden.isTapped()).isTrue();
    }

    // ===== Mana ability =====

    @Test
    @DisplayName("Tapping for mana adds colorless mana")
    void tapForColorlessMana() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new WardenOfTheWall());
        warden.untap();

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isGreaterThanOrEqualTo(1);
        assertThat(warden.isTapped()).isTrue();
    }

    // ===== Opponent-turn animation =====

    @Test
    @DisplayName("Is not a creature during controller's turn")
    void notCreatureDuringControllerTurn() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new WardenOfTheWall());

        harness.forceActivePlayer(player1);

        assertThat(gqs.isCreature(gd, warden)).isFalse();
        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, warden)).isEqualTo(0);
        assertThat(gqs.hasKeyword(gd, warden, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Becomes a 2/3 Gargoyle with flying during opponent's turn")
    void becomesCreatureDuringOpponentTurn() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new WardenOfTheWall());

        harness.forceActivePlayer(player2);

        assertThat(gqs.isCreature(gd, warden)).isTrue();
        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warden)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, warden, Keyword.FLYING)).isTrue();
        assertThat(gqs.computeStaticBonus(gd, warden).grantedSubtypes()).contains(CardSubtype.GARGOYLE);
    }

    @Test
    @DisplayName("Creature status toggles when active player changes")
    void creatureStatusTogglesWithActivePlayer() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new WardenOfTheWall());

        harness.forceActivePlayer(player1);
        assertThat(gqs.isCreature(gd, warden)).isFalse();

        harness.forceActivePlayer(player2);
        assertThat(gqs.isCreature(gd, warden)).isTrue();
        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(2);

        harness.forceActivePlayer(player1);
        assertThat(gqs.isCreature(gd, warden)).isFalse();
    }

    @Test
    @DisplayName("Remains an artifact while animated on opponent's turn")
    void remainsArtifactWhileAnimated() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new WardenOfTheWall());

        harness.forceActivePlayer(player2);

        assertThat(gqs.isArtifact(warden)).isTrue();
        assertThat(gqs.isCreature(gd, warden)).isTrue();
    }

    @Test
    @DisplayName("Newly controlled Warden cannot tap for mana while animated")
    void newlyControlledAnimatedWardenCannotTapForMana() {
        harness.forceActivePlayer(player2);
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new WardenOfTheWall());
        warden.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(warden.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Animated Warden controlled since the turn began can tap for mana")
    void establishedAnimatedWardenCanTapForMana() {
        harness.forceActivePlayer(player2);
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new WardenOfTheWall());
        warden.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(warden.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isCreature(gd, warden)).isTrue();
        assertThat(gqs.hasKeyword(gd, warden, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Animation affects only Warden whose controller is not taking the turn")
    void animationUsesEachWardensController() {
        Permanent ownWarden = harness.addToBattlefieldAndReturn(player1, new WardenOfTheWall());
        Permanent opposingWarden = harness.addToBattlefieldAndReturn(player2, new WardenOfTheWall());
        harness.forceActivePlayer(player1);

        assertThat(gqs.isCreature(gd, ownWarden)).isFalse();
        assertThat(gqs.isCreature(gd, opposingWarden)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingWarden)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingWarden)).isEqualTo(3);

        harness.forceActivePlayer(player2);

        assertThat(gqs.isCreature(gd, ownWarden)).isTrue();
        assertThat(gqs.isCreature(gd, opposingWarden)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingWarden, Keyword.FLYING)).isFalse();
    }
    // ===== Helpers =====

    private Permanent findWarden(com.github.laxika.magicalvibes.model.Player player) {
        return findPermanent(player, "Warden of the Wall");
    }
}
