package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JawboneSkulkinTest extends BaseCardTest {

    private Permanent addSkulkin() {
        Permanent skulkin = harness.addToBattlefieldAndReturn(player1, new JawboneSkulkin());
        skulkin.setSummoningSick(false);
        return skulkin;
    }

    @Test
    @DisplayName("Grants haste to a red creature until end of turn, then it wears off")
    void grantsHasteToRedCreature() {
        addSkulkin();
        Permanent redCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player1, "Hill Giant");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, redCreature, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, redCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-red creature")
    void cannotTargetNonRedCreature() {
        addSkulkin();
        harness.addToBattlefield(player1, new GrizzlyBears()); // green
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
