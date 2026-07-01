package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BlazingFiresingerSeethingSongTest extends BaseCardTest {

    @Test
    @DisplayName("Has ON_ENTER_BATTLEFIELD BecomePreparedEffect and Seething Song back face")
    void hasCorrectStructure() {
        BlazingFiresingerSeethingSong card = new BlazingFiresingerSeethingSong();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(BecomePreparedEffect.class);
        assertThat(card.getBackFaceClassName()).isEqualTo("SeethingSong");
        assertThat(card.getBackFaceCard()).isNotNull();
        assertThat(card.getBackFaceCard().getName()).isEqualTo("Seething Song");
        assertThat(card.getBackFaceCard().getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(AwardManaEffect.class);
        AwardManaEffect manaEffect = (AwardManaEffect) card.getBackFaceCard().getEffects(EffectSlot.SPELL).getFirst();
        assertThat(manaEffect.color()).isEqualTo(ManaColor.RED);
        assertThat(manaEffect.amount()).isEqualTo(5);
    }

    @Test
    @DisplayName("Entering the battlefield prepares Blazing Firesinger and exiles a castable Seething Song copy")
    void entersPrepared() {
        Permanent firesinger = castBlazingFiresinger();

        assertThat(firesinger.isPrepared()).isTrue();
        UUID copyId = firesinger.getPreparedSpellCardId();
        assertThat(copyId).isNotNull();
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.exilePlayPermissions.get(copyId)).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(copyId);
    }

    @Test
    @DisplayName("Casting the prepared Seething Song copy unprepares Blazing Firesinger and adds five red mana")
    void castingPrepareCopyUnpreparesAndResolvesSpell() {
        Permanent firesinger = castBlazingFiresinger();
        UUID copyId = firesinger.getPreparedSpellCardId();

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castFromExile(player1, copyId);
        harness.passBothPriorities();

        assertThat(firesinger.isPrepared()).isFalse();
        assertThat(firesinger.getPreparedSpellCardId()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(5);
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(copyId);
    }

    @Test
    @DisplayName("When prepared Blazing Firesinger leaves the battlefield, the exiled copy ceases to exist")
    void leavingBattlefieldRemovesExiledCopy() {
        Permanent firesinger = castBlazingFiresinger();
        UUID copyId = firesinger.getPreparedSpellCardId();
        assertThat(gd.findExiledCard(copyId)).isNotNull();

        firesinger.setMarkedDamage(3);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(firesinger);
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(copyId);
    }

    private Permanent castBlazingFiresinger() {
        harness.setHand(player1, List.of(new BlazingFiresingerSeethingSong()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB BecomePrepared trigger

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Blazing Firesinger"))
                .findFirst()
                .orElseThrow();
    }
}
