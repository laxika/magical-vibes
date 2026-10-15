package com.github.laxika.magicalvibes.cards.j;

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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JawboneSkulkin.class, HillGiant.class, GrizzlyBears.class})
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

    @Test
    void canTargetOpponentsRedCreature() {
        addSkulkin();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        Permanent skulkin = harness.addToBattlefieldAndReturn(player1, new JawboneSkulkin());
        skulkin.setSummoningSick(true);
        skulkin.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(skulkin.isTapped()).isTrue();
    }

    @Test
    void canActivateRepeatedlyWithoutTapping() {
        Permanent skulkin = addSkulkin();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, first.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, first, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.HASTE)).isTrue();
        assertThat(skulkin.isTapped()).isFalse();
    }

    @Test
    void cannotActivateWithOnlyOneMana() {
        addSkulkin();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetColorlessSkulkin() {
        Permanent skulkin = addSkulkin();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, skulkin.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
