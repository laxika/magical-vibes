package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmeritusOfConflictLightningBolt.class, LightningBolt.class})
class EmeritusOfConflictLightningBoltTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a third spell prepares Emeritus of Conflict")
    void thirdSpellPreparesEmeritus() {
        Permanent emeritus = addEmeritus();

        castThreeBolts();

        assertThat(emeritus.isPrepared()).isTrue();
        UUID copyId = emeritus.getPreparedSpellCardId();
        assertThat(copyId).isNotNull();
        assertThat(gd.findExiledCard(copyId)).isNotNull();
    }

    @Test
    @DisplayName("The first two spells do not prepare Emeritus of Conflict")
    void firstTwoSpellsDoNotPrepareEmeritus() {
        Permanent emeritus = addEmeritus();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(emeritus.isPrepared()).isFalse();
        assertThat(emeritus.getPreparedSpellCardId()).isNull();
    }

    @Test
    @DisplayName("Casting the prepared Lightning Bolt copy deals damage and unprepares Emeritus")
    void castingPreparedCopyDealsDamageAndUnpreparesEmeritus() {
        Permanent emeritus = addEmeritus();
        castThreeBolts();
        UUID copyId = emeritus.getPreparedSpellCardId();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, copyId, player2.getId());
        harness.passBothPriorities();

        assertThat(emeritus.isPrepared()).isFalse();
        assertThat(emeritus.getPreparedSpellCardId()).isNull();
        harness.assertLife(player2, 8);
    }

    @Test
    void preparesBeforeThirdSpellResolves() {
        Permanent emeritus = addEmeritus();
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.castInstant(player1, 0, player2.getId());

        assertThat(emeritus.isPrepared()).isFalse();
        harness.passBothPriorities();
        assertThat(emeritus.isPrepared()).isTrue();
        harness.assertLife(player2, 14);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertLife(player2, 11);
    }

    @Test
    void opponentsThirdSpellDoesNotPrepareEmeritus() {
        Permanent emeritus = addEmeritus();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 3);

        for (int i = 0; i < 3; i++) {
            harness.castAndResolveInstant(player2, 0, player1.getId());
        }
        resolveAllTriggers();

        assertThat(emeritus.isPrepared()).isFalse();
        assertThat(emeritus.getPreparedSpellCardId()).isNull();
    }

    @Test
    void preparedSpellStillRequiresMana() {
        Permanent emeritus = addEmeritus();
        castThreeBolts();
        UUID copyId = emeritus.getPreparedSpellCardId();

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(emeritus.isPrepared()).isTrue();
        assertThat(emeritus.getPreparedSpellCardId()).isEqualTo(copyId);
        assertThat(gd.findExiledCard(copyId)).isNotNull();
    }

    @Test
    void fourthSpellDoesNotPrepareAgainAfterCastingPreparedSpell() {
        Permanent emeritus = addEmeritus();
        castThreeBolts();
        UUID copyId = emeritus.getPreparedSpellCardId();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFromExile(player1, copyId, player2.getId());

        assertThat(emeritus.isPrepared()).isFalse();
        assertThat(emeritus.getPreparedSpellCardId()).isNull();
        resolveAllTriggers();
        assertThat(emeritus.isPrepared()).isFalse();
        assertThat(gd.findExiledCard(copyId)).isNull();
        harness.assertLife(player2, 8);
    }

    @Test
    void preparedBoltCanTargetACreature() {
        Permanent emeritus = addEmeritus();
        Permanent target = addCreatureReady(player2, new EmeritusOfConflictLightningBolt());
        castThreeBolts();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castFromExile(player1, emeritus.getPreparedSpellCardId(), target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
        assertThat(emeritus.isPrepared()).isFalse();
    }

    @Test
    void countsSpellsCastBeforeEmeritusEntered() {
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        Permanent emeritus = addEmeritus();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(emeritus.isPrepared()).isTrue();
    }

    @Test
    void controllersThirdSpellOnOpponentsTurnPreparesEmeritus() {
        Permanent emeritus = addEmeritus();
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        for (int i = 0; i < 3; i++) {
            harness.castAndResolveInstant(player1, 0, player2.getId());
        }
        resolveAllTriggers();

        assertThat(emeritus.isPrepared()).isTrue();
    }

    @Test
    void preparedCopyDisappearsWhenEmeritusDies() {
        Permanent emeritus = addEmeritus();
        castThreeBolts();
        UUID copyId = emeritus.getPreparedSpellCardId();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, emeritus.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(emeritus);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(emeritus.getCard());
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(copyId);
    }

    private Permanent addEmeritus() {
        return addCreatureReady(player1, new EmeritusOfConflictLightningBolt());
    }

    private void castThreeBolts() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        resolveAllTriggers();
    }
}
