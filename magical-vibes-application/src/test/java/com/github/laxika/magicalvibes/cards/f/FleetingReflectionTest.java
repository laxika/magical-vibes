package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FleetingReflection.class, GrizzlyBears.class, HillGiant.class})
class FleetingReflectionTest extends BaseCardTest {

    @Test
    @DisplayName("Gains hexproof, untaps, and copies the other target creature")
    void protectsUntapsAndCopies() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent copySource = addCreatureReady(player2, new HillGiant());
        target.tap();

        castFleetingReflection(target, copySource);

        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
        assertThat(target.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("The optional copy target may be omitted")
    void copyTargetMayBeOmitted() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.tap();
        harness.setHand(player1, List.of(new FleetingReflection()));
        addMana();

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
        assertThat(target.getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("An illegal copy target does not stop the first target's other effects")
    void illegalCopyTargetStillProtectsAndUntapsFirstTarget() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent copySource = addCreatureReady(player2, new HillGiant());
        target.tap();

        harness.setHand(player1, List.of(new FleetingReflection()));
        addMana();
        harness.castInstant(player1, 0, List.of(target.getId(), copySource.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(copySource);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
        assertThat(target.getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Copy and hexproof expire at end of turn")
    void effectsExpireAtEndOfTurn() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        Permanent copySource = addCreatureReady(player2, new HillGiant());

        castFleetingReflection(target, copySource);
        assertThat(target.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("The first target must be a creature you control")
    void firstTargetMustBeControlledCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FleetingReflection()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    private void castFleetingReflection(Permanent target, Permanent copySource) {
        harness.setHand(player1, List.of(new FleetingReflection()));
        addMana();
        harness.castInstant(player1, 0, List.of(target.getId(), copySource.getId()));
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
