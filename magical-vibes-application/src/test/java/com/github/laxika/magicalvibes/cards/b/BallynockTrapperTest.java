package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BallynockTrapperTest extends BaseCardTest {

    @BeforeEach
    void setUp() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private Permanent addReadyTrapper(Player player) {
        Permanent perm = new Permanent(new BallynockTrapper());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyBears(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    @Test
    @DisplayName("{T}: Tap target creature taps the chosen creature")
    void tapAbilityTapsTarget() {
        addReadyTrapper(player1);
        Permanent target = addReadyBears(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating the tap ability taps the Trapper")
    void activatingTapsSelf() {
        Permanent trapper = addReadyTrapper(player1);
        Permanent target = addReadyBears(player2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(trapper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting a white spell and accepting untaps the tapped Trapper")
    void whiteSpellUntapsTrapper() {
        Permanent trapper = addReadyTrapper(player1);
        Permanent target = addReadyBears(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(trapper.isTapped()).isTrue();

        harness.setHand(player1, List.of(new SuntailHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(trapper.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the white-spell trigger leaves the Trapper tapped")
    void decliningLeavesTrapperTapped() {
        Permanent trapper = addReadyTrapper(player1);
        Permanent target = addReadyBears(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(trapper.isTapped()).isTrue();

        harness.setHand(player1, List.of(new SuntailHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(trapper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting a non-white spell does not trigger the untap")
    void nonWhiteSpellDoesNotTrigger() {
        addReadyTrapper(player1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Tap ability rejects a non-creature target")
    void tapAbilityRejectsNonCreatureTarget() {
        addReadyTrapper(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
