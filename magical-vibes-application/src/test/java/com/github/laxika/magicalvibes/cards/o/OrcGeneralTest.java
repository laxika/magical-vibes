package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrcGeneral.class, GoblinPiker.class, GrizzlyBears.class})
class OrcGeneralTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another Orc or Goblin boosts other Orcs on all battlefields")
    void activationSacrificesAnotherOrcOrGoblinAndBoostsOtherOrcs() {
        Permanent general = addReady(player1, new OrcGeneral());
        Permanent sacrificed = addReady(player1, new GoblinPiker());
        Permanent allyOrc = addReady(player1, new OrcGeneral());
        Permanent nonOrc = addReady(player1, new GrizzlyBears());
        Permanent opponentOrc = addReady(player2, new OrcGeneral());

        harness.activateAbility(player1, battlefieldIndex(general), null, null);

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(sacrificed.getId(), allyOrc.getId());
        assertThat(choice.validIds()).doesNotContain(general.getId(), nonOrc.getId());

        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrificed.getCard());
        assertThat(general.isTapped()).isTrue();
        assertThat(general.getPowerModifier()).isZero();
        assertThat(allyOrc.getPowerModifier()).isEqualTo(1);
        assertThat(allyOrc.getToughnessModifier()).isEqualTo(1);
        assertThat(opponentOrc.getPowerModifier()).isEqualTo(1);
        assertThat(opponentOrc.getToughnessModifier()).isEqualTo(1);
        assertThat(nonOrc.getPowerModifier()).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(allyOrc.getPowerModifier()).isZero();
        assertThat(opponentOrc.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot activate without another Orc or Goblin to sacrifice")
    void activationRequiresAnotherOrcOrGoblin() {
        Permanent general = addReady(player1, new OrcGeneral());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(general), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
