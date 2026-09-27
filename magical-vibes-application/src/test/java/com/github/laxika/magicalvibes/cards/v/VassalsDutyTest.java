package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.k.KondaLordOfEiganjo;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.cards.y.YamabushisFlame;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VassalsDuty.class, KondaLordOfEiganjo.class, LanternKami.class, YamabushisFlame.class})
class VassalsDutyTest extends BaseCardTest {

    @Test
    @DisplayName("Redirects the next damage to a legendary creature to its controller")
    void redirectsNextDamageToController() {
        harness.addToBattlefield(player1, new VassalsDuty());
        harness.addToBattlefield(player1, new KondaLordOfEiganjo());
        UUID targetId = harness.getPermanentId(player1, "Konda, Lord of Eiganjo");
        harness.setLife(player1, 20);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new YamabushisFlame()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(findPermanent(player1, "Konda, Lord of Eiganjo").getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Redirects the next combat damage to a legendary creature to its controller")
    void redirectsNextCombatDamageToController() {
        harness.addToBattlefield(player1, new VassalsDuty());
        harness.addToBattlefield(player1, new KondaLordOfEiganjo());
        addCreatureReady(player2, new KondaLordOfEiganjo());
        UUID targetId = harness.getPermanentId(player1, "Konda, Lord of Eiganjo");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(findPermanent(player1, "Konda, Lord of Eiganjo").getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("The redirect shield expires at the end of the turn")
    void redirectShieldExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new VassalsDuty());
        harness.addToBattlefield(player1, new KondaLordOfEiganjo());
        UUID targetId = harness.getPermanentId(player1, "Konda, Lord of Eiganjo");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new YamabushisFlame()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(findPermanent(player1, "Konda, Lord of Eiganjo").getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Rejects a nonlegendary creature as the target")
    void rejectsNonlegendaryCreature() {
        harness.addToBattlefield(player1, new VassalsDuty());
        harness.addToBattlefield(player1, new LanternKami());
        UUID targetId = harness.getPermanentId(player1, "Lantern Kami");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a legendary creature controlled by an opponent")
    void rejectsOpponentsLegendaryCreature() {
        harness.addToBattlefield(player1, new VassalsDuty());
        harness.addToBattlefield(player2, new KondaLordOfEiganjo());
        UUID targetId = harness.getPermanentId(player2, "Konda, Lord of Eiganjo");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
