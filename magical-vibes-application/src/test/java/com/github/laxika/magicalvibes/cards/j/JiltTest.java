package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Jilt.class, GrizzlyBears.class, HillGiant.class})
class JiltTest extends BaseCardTest {

    @Test
    void returnsTargetCreatureWithoutKicker() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Jilt()));
        addJiltMana();

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    void returnsFirstTargetAndDamagesAnotherWhenKicked() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent damageTarget = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        UUID returnTargetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Jilt()));
        addJiltMana();

        harness.castKickedInstantWithSacrifices(player1, 0, returnTargetId,
                List.of(damageTarget.getId()), List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(damageTarget.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void kickedSpellRequiresAnotherCreatureTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Jilt()));
        addJiltMana();

        assertThatThrownBy(() -> harness.castKickedInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotUseSameCreatureAsBothTargets() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Jilt()));
        addJiltMana();

        assertThatThrownBy(() -> harness.castKickedInstantWithSacrifices(player1, 0, targetId,
                List.of(targetId), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addJiltMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
