package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InfirmaryHealerStreamOfLifeTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield prepares Infirmary Healer and exiles a Stream of Life copy")
    void entersPrepared() {
        Permanent healer = castInfirmaryHealer();

        assertThat(healer.isPrepared()).isTrue();
        UUID copyId = healer.getPreparedSpellCardId();
        assertThat(copyId).isNotNull();
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.exilePlayPermissions.get(copyId)).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Casting the prepared Stream of Life copy unprepares Infirmary Healer")
    void castingPrepareCopyUnpreparesHealer() {
        Permanent healer = castInfirmaryHealer();
        UUID copyId = healer.getPreparedSpellCardId();

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castFromExile(player1, copyId, player2.getId());
        harness.passBothPriorities();

        assertThat(healer.isPrepared()).isFalse();
        assertThat(healer.getPreparedSpellCardId()).isNull();
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(copyId);
    }

    @Test
    @DisplayName("When prepared Infirmary Healer leaves the battlefield, the exiled copy ceases to exist")
    void leavingBattlefieldRemovesExiledCopy() {
        Permanent healer = castInfirmaryHealer();
        UUID copyId = healer.getPreparedSpellCardId();

        healer.setMarkedDamage(3);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(healer);
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(copyId);
    }

    private Permanent castInfirmaryHealer() {
        harness.setHand(player1, List.of(new InfirmaryHealerStreamOfLife()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player1.getId()).getLast();
    }
}
