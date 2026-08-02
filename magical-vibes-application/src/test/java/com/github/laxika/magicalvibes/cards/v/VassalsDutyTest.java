package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VassalsDutyTest extends BaseCardTest {

    @Test
    @DisplayName("Redirects the next damage to a legendary creature to its controller")
    void redirectsNextDamageToController() {
        harness.addToBattlefield(player1, new VassalsDuty());
        harness.addToBattlefield(player1, new ArvadTheCursed());
        UUID targetId = harness.getPermanentId(player1, "Arvad the Cursed");
        harness.setLife(player1, 20);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(findPermanent(player1, "Arvad the Cursed").getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Rejects a nonlegendary creature as the target")
    void rejectsNonlegendaryCreature() {
        harness.addToBattlefield(player1, new VassalsDuty());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a legendary creature controlled by an opponent")
    void rejectsOpponentsLegendaryCreature() {
        harness.addToBattlefield(player1, new VassalsDuty());
        harness.addToBattlefield(player2, new ArvadTheCursed());
        UUID targetId = harness.getPermanentId(player2, "Arvad the Cursed");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
