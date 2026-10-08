package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RaiseDead;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({CheerfulOsteomancerRaiseDead.class, RaiseDead.class, GrizzlyBears.class})
class CheerfulOsteomancerRaiseDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield prepares Cheerful Osteomancer and exiles a castable Raise Dead copy")
    void entersPrepared() {
        Permanent osteomancer = castCheerfulOsteomancer();

        assertThat(osteomancer.isPrepared()).isTrue();
        assertThat(gd.stack).isEmpty();
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
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(copyId)).isNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(copyId);
    }

    @Test
    @DisplayName("Casting Raise Dead directly returns a target creature card from its controller's graveyard to hand")
    void directRaiseDeadReturnsCreatureCardToHand() {
        Card creature = new GrizzlyBears();
        Card raiseDead = new RaiseDead();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(raiseDead));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Raise Dead");
    }

    @Test
    @DisplayName("Prepared Raise Dead copy cannot target non-creature card in graveyard")
    void preparedRaiseDeadCannotTargetNonCreature() {
        Permanent osteomancer = castCheerfulOsteomancer();
        Card sorcery = new RaiseDead();
        harness.setGraveyard(player1, List.of(sorcery));
        UUID copyId = osteomancer.getPreparedSpellCardId();

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId, sorcery.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Raise Dead fizzles if the target creature leaves the graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new RaiseDead()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, creature.getId());
        gd.playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Raise Dead cannot be cast without a graveyard target")
    void cannotCastWithoutGraveyardTarget() {
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new RaiseDead()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, (UUID) null))
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

    @Test
    @DisplayName("Casting the prepared spell unprepares the creature before the spell resolves")
    void unpreparesAtCastingEvenIfTargetLaterLeaves() {
        Permanent osteomancer = castCheerfulOsteomancer();
        Card creature = new CheerfulOsteomancerRaiseDead();
        harness.setGraveyard(player1, List.of(creature));
        UUID copyId = osteomancer.getPreparedSpellCardId();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castFromExile(player1, copyId, creature.getId());

        assertThat(osteomancer.isPrepared()).isFalse();
        assertThat(osteomancer.getPreparedSpellCardId()).isNull();
        gd.playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Cheerful Osteomancer");
        harness.assertNotInGraveyard(player1, "Raise Dead");
        assertThat(osteomancer.isPrepared()).isFalse();
        assertThat(gd.findExiledCard(copyId)).isNull();
    }

    @Test
    @DisplayName("A prepared spell requires its mana cost and failed casting leaves the creature prepared")
    void cannotCastPreparedSpellWithoutMana() {
        Permanent osteomancer = castCheerfulOsteomancer();
        Card creature = new CheerfulOsteomancerRaiseDead();
        harness.setGraveyard(player1, List.of(creature));
        UUID copyId = osteomancer.getPreparedSpellCardId();

        assertThatThrownBy(() -> harness.castFromExile(player1, copyId, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(osteomancer.isPrepared()).isTrue();
        assertThat(osteomancer.getPreparedSpellCardId()).isEqualTo(copyId);
        assertThat(gd.findExiledCard(copyId)).isNotNull();
        harness.assertInGraveyard(player1, "Cheerful Osteomancer");
    }

    private Permanent castCheerfulOsteomancer() {
        harness.withAutoStop(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN, () -> {
            harness.castFromHand(player1, new CheerfulOsteomancerRaiseDead(), "{3}{B}");
            harness.passBothPriorities();
            resolveAllTriggers();
        });

        return findPermanent(player1, "Cheerful Osteomancer");
    }
}
