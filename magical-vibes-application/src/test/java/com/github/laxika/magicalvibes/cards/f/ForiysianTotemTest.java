package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForiysianTotem.class, GrizzlyBears.class})
class ForiysianTotemTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Foriysian Totem adds red mana")
    void tappingAddsRedMana() {
        Permanent totem = addReadyTotem(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(totem.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Foriysian Totem becomes a red 4/4 Giant with trample")
    void animatesIntoGiant() {
        Permanent totem = addReadyTotem(player1);
        animate(totem, player1);

        assertThat(gqs.isCreature(gd, totem)).isTrue();
        assertThat(gqs.isArtifact(totem)).isTrue();
        assertThat(gqs.getEffectivePower(gd, totem)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, totem)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, totem)).containsExactly(CardColor.RED);
        assertThat(totem.getTransientSubtypes()).contains(CardSubtype.GIANT);
        assertThat(gqs.hasKeyword(gd, totem, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Animated Foriysian Totem can block two attackers")
    void animatedTotemCanBlockTwoAttackers() {
        Permanent totem = addReadyTotem(player2);
        animate(totem, player2);
        int totemIndex = gd.playerBattlefields.get(player2.getId()).indexOf(totem);
        addAttackers(2);
        beginBlockerDeclaration();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(totemIndex, 0),
                new BlockerAssignment(totemIndex, 1)
        ));

        assertThat(totem.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("Animated Foriysian Totem does not grant an additional block to other creatures")
    void animatedTotemDoesNotGrantOtherCreaturesAnAdditionalBlock() {
        Permanent totem = addReadyTotem(player2);
        animate(totem, player2);
        Permanent bears = addReadyCreature(player2, new GrizzlyBears());
        int bearsIndex = gd.playerBattlefields.get(player2.getId()).indexOf(bears);
        addAttackers(2);
        beginBlockerDeclaration();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(bearsIndex, 0),
                new BlockerAssignment(bearsIndex, 1)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("too many times");
    }

    @Test
    @DisplayName("Foriysian Totem stops being a creature at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent totem = addReadyTotem(player1);
        animate(totem, player1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, totem)).isFalse();
    }

    private Permanent addReadyTotem(Player player) {
        return addReadyCreature(player, new ForiysianTotem());
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void animate(Permanent totem, Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 4);
        harness.addMana(player, ManaColor.RED, 1);
        int totemIndex = gd.playerBattlefields.get(player.getId()).indexOf(totem);
        harness.activateAbility(player, totemIndex, 1, null, null);
        harness.passBothPriorities();
    }

    private void addAttackers(int count) {
        for (int i = 0; i < count; i++) {
            Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
            attacker.setAttacking(true);
        }
    }

    private void beginBlockerDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
