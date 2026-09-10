package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
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

@CardUsed({ThorinsLastStand.class, GrizzlyBears.class, FountainOfYouth.class, AngelicChorus.class})
class ThorinsLastStandTest extends BaseCardTest {

    @Test
    @DisplayName("The pump mode boosts only your creatures and wears off at end of turn")
    void pumpModeBoostsOwnCreaturesUntilEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast(0, null);

        Permanent ownBears = findPermanent(player1, "Grizzly Bears");
        Permanent opponentBears = findPermanent(player2, "Grizzly Bears");
        assertThat(ownBears.getEffectivePower()).isEqualTo(4);
        assertThat(ownBears.getEffectiveToughness()).isEqualTo(3);
        assertThat(opponentBears.getEffectivePower()).isEqualTo(2);
        assertThat(opponentBears.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownBears.getEffectivePower()).isEqualTo(2);
        assertThat(ownBears.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The removal mode destroys an artifact and you gain 2 life")
    void removalModeDestroysArtifactAndGainsLife() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setLife(player1, 10);

        cast(1, harness.getPermanentId(player2, "Fountain of Youth"));

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("The removal mode also destroys an enchantment")
    void removalModeDestroysEnchantment() {
        harness.addToBattlefield(player2, new AngelicChorus());

        cast(1, harness.getPermanentId(player2, "Angelic Chorus"));

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("The removal mode cannot target a creature")
    void removalModeRejectsCreatureTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ThorinsLastStand()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1,
                harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new ThorinsLastStand()));
        addMana();
        harness.castInstant(player1, 0, mode, targetId);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
