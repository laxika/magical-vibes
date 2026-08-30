package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecklessRageTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to an opponent's creature and 2 damage to your creature")
    void dealsDamageToBothTargets() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new RecklessRage()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID ownCreatureId = harness.getPermanentId(player1, "Hill Giant");
        UUID opponentCreatureId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, List.of(opponentCreatureId, ownCreatureId));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
        Permanent ownCreature = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(ownCreature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("The opponent's creature must be the first target")
    void rejectsOwnCreatureAsFirstTarget() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new RecklessRage()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID ownCreatureId = harness.getPermanentId(player1, "Hill Giant");
        UUID opponentCreatureId = harness.getPermanentId(player2, "Hill Giant");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(ownCreatureId, opponentCreatureId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Your creature must be the second target")
    void rejectsOpponentCreatureAsSecondTarget() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new RecklessRage()));
        harness.addMana(player1, ManaColor.RED, 1);

        List<Permanent> opponentBattlefield = gd.playerBattlefields.get(player2.getId());
        UUID firstTargetId = opponentBattlefield.get(0).getId();
        UUID secondTargetId = opponentBattlefield.get(1).getId();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(firstTargetId, secondTargetId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
