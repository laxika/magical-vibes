package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CheerfulOsteomancerRaiseDeadTest extends BaseCardTest {

    

    @Test
    @DisplayName("Entering the battlefield prepares Cheerful Osteomancer and exiles a castable Raise Dead copy")
    void entersPrepared() {
        Permanent osteomancer = castCheerfulOsteomancer();

        assertThat(osteomancer.isPrepared()).isTrue();
        UUID copyId = osteomancer.getPreparedSpellCardId();
        assertThat(copyId).isNotNull();
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.exilePlayPermissions.get(copyId)).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(copyId);
    }

    @Test
    @DisplayName("Casting the prepared Raise Dead copy unprepares Cheerful Osteomancer and returns a creature to hand")
    void castingPrepareCopyUnpreparesAndReturnsCreatureFromGraveyard() {
        Permanent osteomancer = castCheerfulOsteomancer();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        UUID copyId = osteomancer.getPreparedSpellCardId();

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromExile(player1, copyId, creature.getId());
        harness.passBothPriorities();

        assertThat(osteomancer.isPrepared()).isFalse();
        assertThat(osteomancer.getPreparedSpellCardId()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(creature.getId()));
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(copyId);
    }

    @Test
    @DisplayName("Prepared Raise Dead copy cannot target non-creature card in graveyard")
    void preparedRaiseDeadCannotTargetNonCreature() {
        Permanent osteomancer = castCheerfulOsteomancer();
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        UUID copyId = osteomancer.getPreparedSpellCardId();

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Prepared Raise Dead copy cannot target a creature in the opponent's graveyard")
    void preparedRaiseDeadCannotTargetOpponentGraveyard() {
        Permanent osteomancer = castCheerfulOsteomancer();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        UUID copyId = osteomancer.getPreparedSpellCardId();

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("When prepared Cheerful Osteomancer leaves the battlefield, the exiled copy ceases to exist")
    void leavingBattlefieldRemovesExiledCopy() {
        Permanent osteomancer = castCheerfulOsteomancer();
        UUID copyId = osteomancer.getPreparedSpellCardId();
        assertThat(gd.findExiledCard(copyId)).isNotNull();

        osteomancer.setMarkedDamage(2);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(osteomancer);
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(copyId);
    }

    private Permanent castCheerfulOsteomancer() {
        harness.setHand(player1, List.of(new CheerfulOsteomancerRaiseDead()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB BecomePrepared trigger

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Cheerful Osteomancer"))
                .findFirst()
                .orElseThrow();
    }
}
