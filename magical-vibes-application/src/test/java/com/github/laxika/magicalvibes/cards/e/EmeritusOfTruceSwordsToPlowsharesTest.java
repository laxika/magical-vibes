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

class EmeritusOfTruceSwordsToPlowsharesTest extends BaseCardTest {

    @Test
    @DisplayName("The ETB lets a target player create a flying Inkling and prepares Emeritus after it enters")
    void createsTokenBeforeCheckingCreatureCount() {
        addCreatureReady(player2, new GrizzlyBears());

        Permanent emeritus = castEmeritus(player2.getId());

        Permanent inkling = findPermanent(player2, "Inkling");
        assertThat(gqs.getEffectivePower(gd, inkling)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, inkling)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, inkling, Keyword.FLYING)).isTrue();
        assertThat(emeritus.isPrepared()).isTrue();
        assertThat(emeritus.getPreparedSpellCardId()).isNotNull();
    }

    @Test
    @DisplayName("Emeritus does not become prepared when no opponent controls more creatures")
    void doesNotPrepareWithoutCreatureAdvantage() {
        Permanent emeritus = castEmeritus(player1.getId());

        assertThat(findPermanents(player1, "Inkling")).hasSize(1);
        assertThat(emeritus.isPrepared()).isFalse();
        assertThat(emeritus.getPreparedSpellCardId()).isNull();
    }

    @Test
    @DisplayName("Casting the prepared Swords to Plowshares copy unprepares Emeritus and exiles a creature")
    void castingPreparedCopyResolvesSwordsToPlowshares() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent emeritus = castEmeritus(player2.getId());
        UUID copyId = emeritus.getPreparedSpellCardId();
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromExile(player1, copyId, target.getId());
        harness.passBothPriorities();

        assertThat(emeritus.isPrepared()).isFalse();
        assertThat(emeritus.getPreparedSpellCardId()).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.findExiledCard(copyId)).isNull();
    }

    private Permanent castEmeritus(UUID targetId) {
        harness.setHand(player1, List.of(new EmeritusOfTruceSwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Emeritus of Truce");
    }
}
