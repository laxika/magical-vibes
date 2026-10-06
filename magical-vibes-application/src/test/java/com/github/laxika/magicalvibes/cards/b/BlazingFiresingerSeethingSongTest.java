package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.ActOfTreason;
import com.github.laxika.magicalvibes.cards.s.SeethingSong;
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

@CardUsed({BlazingFiresingerSeethingSong.class, SeethingSong.class, ActOfTreason.class})
class BlazingFiresingerSeethingSongTest extends BaseCardTest {

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

    @Test
    @DisplayName("Blazing Firesinger is prepared immediately on entering, without a triggered ability")
    void entersPreparedWithoutUsingStack() {
        harness.castFromHand(player1, new BlazingFiresingerSeethingSong(), "{2}{R}");
        harness.passBothPriorities();

        Permanent firesinger = findPermanent(player1, "Blazing Firesinger");
        assertThat(firesinger.isPrepared()).isTrue();
        assertThat(firesinger.getPreparedSpellCardId()).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting Seething Song unprepares the creature before the spell resolves")
    void castingUnpreparesImmediately() {
        Permanent firesinger = castBlazingFiresinger();
        UUID copyId = firesinger.getPreparedSpellCardId();
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castFromExile(player1, copyId);

        assertThat(firesinger.isPrepared()).isFalse();
        assertThat(firesinger.getPreparedSpellCardId()).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        harness.passBothPriorities();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(5);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A failed attempt to pay for Seething Song leaves the creature prepared")
    void insufficientManaDoesNotUnprepare() {
        Permanent firesinger = castBlazingFiresinger();
        UUID copyId = firesinger.getPreparedSpellCardId();
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId))
                .isInstanceOf(IllegalStateException.class);

        assertThat(firesinger.isPrepared()).isTrue();
        assertThat(firesinger.getPreparedSpellCardId()).isEqualTo(copyId);
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The new controller can cast the prepared Seething Song copy")
    void preparedSpellPermissionFollowsController() {
        Permanent firesinger = stealPreparedFiresinger();
        UUID copyId = firesinger.getPreparedSpellCardId();
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castFromExile(player2, copyId);
        assertThat(firesinger.isPrepared()).isFalse();
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(5);
    }

    @Test
    @DisplayName("The previous controller cannot cast the prepared Seething Song copy")
    void previousControllerCannotCastPreparedSpell() {
        Permanent firesinger = stealPreparedFiresinger();
        UUID copyId = firesinger.getPreparedSpellCardId();
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId))
                .isInstanceOf(IllegalStateException.class);

        assertThat(firesinger.isPrepared()).isTrue();
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent stealPreparedFiresinger() {
        Permanent firesinger = castBlazingFiresinger();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new ActOfTreason()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player2, 0, firesinger.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(firesinger);
        return firesinger;
    }

    private Permanent castBlazingFiresinger() {
        harness.castFromHand(player1, new BlazingFiresingerSeethingSong(), "{2}{R}");
        harness.passBothPriorities();

        return findPermanent(player1, "Blazing Firesinger");
    }
}
