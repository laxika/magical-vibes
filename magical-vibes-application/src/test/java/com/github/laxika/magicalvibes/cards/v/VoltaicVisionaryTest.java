package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.b.BelligerentGuest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.SureStrike;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoltaicVisionary.class, VoltChargedBerserker.class, BelligerentGuest.class, Mountain.class, Abrade.class, SureStrike.class})
class VoltaicVisionaryTest extends BaseCardTest {

    @Test
    void damagesControllerAndTracksTopCardWithPlayPermission() {
        Card topCard = new BelligerentGuest();
        Permanent visionary = activateWithTopCard(topCard);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        ExiledCardEntry exiled = gd.findExiledCard(topCard.getId());
        assertThat(exiled).isNotNull();
        assertThat(exiled.sourcePermanentId()).isEqualTo(visionary.getId());
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
    }

    @Test
    void transformsWhenCardExiledWithItIsCast() {
        Card topCard = new BelligerentGuest();
        Permanent visionary = activateWithTopCard(topCard);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(visionary.isTransformed()).isTrue();
        assertThat(visionary.getCard()).isInstanceOf(VoltChargedBerserker.class);
    }

    @Test
    void transformsWhenCardExiledWithItIsPlayedAsALand() {
        Card topCard = new Mountain();
        Permanent visionary = activateWithTopCard(topCard);

        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(visionary.isTransformed()).isTrue();
        assertThat(visionary.getCard()).isInstanceOf(VoltChargedBerserker.class);
    }

    @Test
    void activationIsSorcerySpeed() {
        addVisionary();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void stillExilesAndAllowsPlayingWhenVisionaryDiesInResponse() {
        Permanent visionary = addVisionary();
        Card topCard = new Mountain();
        harness.setLibrary(player1, List.of(topCard));
        prepareMainPhase(player1);
        harness.activateAbility(player1, 0, null, null);

        harness.setHand(player2, List.of(new Abrade()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castModalInstant(player2, 0, 0, List.of(visionary.getId()));
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(visionary);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        prepareMainPhase(player1);
        harness.castFromExile(player1, topCard.getId());
        harness.assertOnBattlefield(player1, "Mountain");
    }

    @Test
    void emptyLibraryStillDealsDamageWithoutTransforming() {
        Permanent visionary = activateWithTopCard(new Mountain());
        visionary.untap();
        harness.setLibrary(player1, List.of());
        prepareMainPhase(player1);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
        assertThat(visionary.isTransformed()).isFalse();
    }

    @Test
    void onlyTheVisionaryThatExiledTheCardTransforms() {
        Card topCard = new Mountain();
        Permanent first = activateWithTopCard(topCard);
        Permanent second = addVisionary();

        harness.castFromExile(player1, topCard.getId());
        resolveAllTriggers();

        assertThat(first.isTransformed()).isTrue();
        assertThat(second.isTransformed()).isFalse();
    }

    @Test
    void exiledCreatureStillRequiresItsManaCost() {
        Card topCard = new BelligerentGuest();
        Permanent visionary = activateWithTopCard(topCard);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(visionary.isTransformed()).isFalse();
    }

    @Test
    void playPermissionExpiresAtEndOfTurn() {
        Card topCard = new Mountain();
        activateWithTopCard(topCard);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        prepareMainPhase(player1);
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void transformedBerserkerCannotBlock() {
        Card topCard = new Mountain();
        Permanent visionary = activateWithTopCard(topCard);
        harness.castFromExile(player1, topCard.getId());
        resolveAllTriggers();
        visionary.untap();
        addCreatureReady(player2, new BelligerentGuest());

        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void overlappingPlayTriggersTransformOnlyOnce() {
        Permanent visionary = addVisionary();
        Card first = new SureStrike();
        Card second = new SureStrike();
        harness.setLibrary(player1, List.of(first, second));
        prepareMainPhase(player1);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        visionary.untap();
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castFromExile(player1, first.getId(), visionary.getId());
        harness.castFromExile(player1, second.getId(), visionary.getId());
        resolveAllTriggers();

        assertThat(visionary.isTransformed()).isTrue();
        assertThat(visionary.getCard()).isInstanceOf(VoltChargedBerserker.class);
    }

    private Permanent activateWithTopCard(Card topCard) {
        Permanent visionary = addVisionary();
        harness.setLibrary(player1, List.of(topCard));
        prepareMainPhase(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return visionary;
    }

    private Permanent addVisionary() {
        return addCreatureReady(player1, new VoltaicVisionary());
    }

    private void prepareMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
