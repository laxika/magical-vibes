package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.s.StreamOfLife;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InfirmaryHealerStreamOfLife.class, StreamOfLife.class})
class InfirmaryHealerStreamOfLifeTest extends BaseCardTest {

    @Test
    @DisplayName("Infirmary Healer enters prepared without a triggered ability on the stack")
    void preparesDuringEntryWithoutTrigger() {
        harness.setHand(player1, List.of(new InfirmaryHealerStreamOfLife()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();

        Permanent healer = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(healer.isPrepared()).isTrue();
        assertThat(healer.getPreparedSpellCardId()).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Prepared Stream of Life pays for X and unprepares its source before resolution")
    void preparedSpellGainsChosenXLife() {
        Permanent healer = castInfirmaryHealer();
        UUID copyId = healer.getPreparedSpellCardId();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.GREEN, 5);

        gs.playCardFromExile(gd, player1, copyId, 4, player2.getId());

        assertThat(healer.isPrepared()).isFalse();
        assertThat(healer.getPreparedSpellCardId()).isNull();
        harness.assertLife(player2, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 24);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An unsuccessful attempt to pay for prepared Stream of Life leaves the healer prepared")
    void insufficientManaDoesNotUnprepare() {
        Permanent healer = castInfirmaryHealer();
        UUID copyId = healer.getPreparedSpellCardId();
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> gs.playCardFromExile(gd, player1, copyId, 4, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(healer.isPrepared()).isTrue();
        assertThat(healer.getPreparedSpellCardId()).isEqualTo(copyId);
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The prepared spell still resolves after its source leaves the battlefield")
    void preparedSpellSurvivesSourceLeaving() {
        Permanent healer = castInfirmaryHealer();
        UUID copyId = healer.getPreparedSpellCardId();
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.GREEN, 4);
        gs.playCardFromExile(gd, player1, copyId, 3, player1.getId());

        healer.setMarkedDamage(3);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(healer);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
    }

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
        resolveAllTriggers();

        return gd.playerBattlefields.get(player1.getId()).getLast();
    }
}
