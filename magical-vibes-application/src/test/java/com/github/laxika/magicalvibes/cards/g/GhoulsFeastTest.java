package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GhoulsFeastTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature +X/+0 for creature cards in the caster's graveyard")
    void givesPowerBoostBasedOnCreatureCardsInCastersGraveyard() {
        Permanent target = addCreature(player2);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new HillGiant(), new Shock()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new HillGiant()));
        harness.setHand(player1, List.of(new GhoulsFeast()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts creature cards when the spell resolves")
    void countsCreatureCardsAtResolution() {
        Permanent target = addCreature(player1);
        harness.setHand(player1, List.of(new GhoulsFeast()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new HillGiant()));
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = addCreature(player2);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new GhoulsFeast()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new GhoulsFeast()));
        addMana();

        UUID targetId = harness.getPermanentId(player2, "Forest");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
