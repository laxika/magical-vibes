package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.ElaborateFirecannon;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MirozelTest extends BaseCardTest {

    @Test
    @DisplayName("Returns itself to its owner's hand when targeted by a spell")
    void returnsToHandWhenTargetedBySpell() {
        Permanent miroz = harness.addToBattlefieldAndReturn(player1, new Mirozel());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, miroz.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Mirozel");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(miroz.getId()));
    }

    @Test
    @DisplayName("Returns itself to its owner's hand when targeted by an ability")
    void returnsToHandWhenTargetedByAbility() {
        Permanent miroz = harness.addToBattlefieldAndReturn(player1, new Mirozel());

        Permanent firecannon = new Permanent(new ElaborateFirecannon());
        firecannon.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(firecannon);

        harness.addMana(player2, ManaColor.COLORLESS, 4);
        UUID mirozId = miroz.getId();
        harness.activateAbility(player2, 0, null, mirozId);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Mirozel");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(mirozId));
    }
}
