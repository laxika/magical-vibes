package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HonorboundPageForumsFavorTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield prepares Honorbound Page and exiles a castable Forum's Favor copy")
    void entersPrepared() {
        Permanent page = castHonorboundPage();

        assertThat(page.isPrepared()).isTrue();
        UUID copyId = page.getPreparedSpellCardId();
        assertThat(copyId).isNotNull();
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.exilePlayPermissions.get(copyId)).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(copyId);
    }

    @Test
    @DisplayName("Casting Forum's Favor unprepares Honorbound Page and gives the target +1/+0 and flying")
    void castingForumFavorUnpreparesAndBoostsTarget() {
        Permanent page = castHonorboundPage();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        UUID copyId = page.getPreparedSpellCardId();

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromExile(player1, copyId, target.getId());
        harness.passBothPriorities();

        assertThat(page.isPrepared()).isFalse();
        assertThat(page.getPreparedSpellCardId()).isNull();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(copyId);
    }

    @Test
    @DisplayName("When prepared Honorbound Page leaves the battlefield, its exiled copy ceases to exist")
    void leavingBattlefieldRemovesExiledCopy() {
        Permanent page = castHonorboundPage();
        UUID copyId = page.getPreparedSpellCardId();

        page.setMarkedDamage(3);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(page);
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(copyId);
    }

    private Permanent castHonorboundPage() {
        harness.setHand(player1, List.of(new HonorboundPageForumsFavor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        return findPermanent(player1, "Honorbound Page");
    }
}
