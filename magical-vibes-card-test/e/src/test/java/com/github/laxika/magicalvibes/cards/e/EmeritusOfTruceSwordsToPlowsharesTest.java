package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({EmeritusOfTruceSwordsToPlowshares.class, SwordsToPlowshares.class, GrizzlyBears.class})
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
        assertThat(emeritus.isPrepared()).isFalse();
        assertThat(emeritus.getPreparedSpellCardId()).isNull();
        harness.passBothPriorities();

        assertThat(emeritus.isPrepared()).isFalse();
        assertThat(emeritus.getPreparedSpellCardId()).isNull();
        harness.assertLife(player2, 22);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.findExiledCard(copyId)).isNull();
    }

    @Test
    void equalCreatureCountsAfterTokenCreationDoNotPrepare() {
        Permanent emeritus = castEmeritus(player2.getId());

        assertThat(findPermanents(player2, "Inkling")).hasSize(1);
        assertThat(emeritus.isPrepared()).isFalse();
    }

    @Test
    void creatureCountIsCheckedWhenEntryAbilityResolves() {
        harness.setHand(player1, List.of(new EmeritusOfTruceSwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        addCreatureReady(player2, new EmeritusOfTruceSwordsToPlowshares());

        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Emeritus of Truce").isPrepared()).isTrue();
        assertThat(findPermanents(player2, "Inkling")).hasSize(1);
    }

    @Test
    void givingYourselfTokenCanRemoveOpponentsCreatureAdvantage() {
        addCreatureReady(player2, new EmeritusOfTruceSwordsToPlowshares());
        addCreatureReady(player2, new EmeritusOfTruceSwordsToPlowshares());

        Permanent emeritus = castEmeritus(player1.getId());

        assertThat(findPermanents(player1, "Inkling")).hasSize(1);
        assertThat(emeritus.isPrepared()).isFalse();
    }

    @Test
    void preparedSpellRequiresItsManaCostAndRemainsPreparedAfterRejectedCast() {
        Permanent target = addCreatureReady(player2, new EmeritusOfTruceSwordsToPlowshares());
        Permanent emeritus = castEmeritus(player2.getId());
        UUID copyId = emeritus.getPreparedSpellCardId();

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(emeritus.isPrepared()).isTrue();
        assertThat(emeritus.getPreparedSpellCardId()).isEqualTo(copyId);
        assertThat(gd.findExiledCard(copyId)).isNotNull();
    }

    @Test
    void preparedSpellCanExileItsOwnSourceAndGainLifeForItsController() {
        addCreatureReady(player2, new EmeritusOfTruceSwordsToPlowshares());
        Permanent emeritus = castEmeritus(player2.getId());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castFromExile(player1, emeritus.getPreparedSpellCardId(), emeritus.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Emeritus of Truce");
        harness.assertLife(player1, 23);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(emeritus.getCard().getId()));
    }

    private Permanent castEmeritus(UUID targetId) {
        harness.setHand(player1, List.of(new EmeritusOfTruceSwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Emeritus of Truce");
    }
}
