package com.github.laxika.magicalvibes.cards.e;

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

class EncouragingAviatorJumpTest extends BaseCardTest {

    @Test
    @DisplayName("Whenever Encouraging Aviator attacks, it becomes prepared with a Jump copy in exile")
    void attackingPreparesIt() {
        Permanent aviator = addCreatureReady(player1, new EncouragingAviatorJump());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(aviator.isPrepared()).isTrue();
        UUID copyId = aviator.getPreparedSpellCardId();
        assertThat(copyId).isNotNull();
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        assertThat(gd.exilePlayPermissions.get(copyId)).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Casting the prepared Jump copy unprepares Encouraging Aviator and grants flying")
    void castingPreparedJumpUnpreparesAndGrantsFlying() {
        Permanent aviator = addCreatureReady(player1, new EncouragingAviatorJump());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        UUID copyId = aviator.getPreparedSpellCardId();

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, copyId, target.getId());
        resolveAllTriggers();

        assertThat(aviator.isPrepared()).isFalse();
        assertThat(aviator.getPreparedSpellCardId()).isNull();
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
    }
}
