package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkyclaveAerialist.class, SkyclaveInvader.class, Forest.class})
class SkyclaveAerialistTest extends BaseCardTest {

    @Test
    void acceptsTopLandOntoBattlefieldWhenItTransforms() {
        Forest topLand = new Forest();
        harness.setLibrary(player1, List.of(topLand));
        Permanent aerialist = addAerialist();

        transform(aerialist);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        Permanent land = findPermanent(player1, topLand.getName());
        assertThat(land).isNotNull();
        assertThat(land.isTapped()).isFalse();
    }

    @Test
    void decliningTopLandPutsItIntoHand() {
        Forest topLand = new Forest();
        harness.setLibrary(player1, List.of(topLand));
        Permanent aerialist = addAerialist();

        transform(aerialist);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(topLand);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(topLand.getId()));
    }

    @Test
    void nonlandTopCardGoesDirectlyToHand() {
        SkyclaveAerialist topCard = new SkyclaveAerialist();
        harness.setLibrary(player1, List.of(topCard));
        Permanent aerialist = addAerialist();

        transform(aerialist);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
    }

    @Test
    void canPayPhyrexianManaWithLife() {
        Permanent aerialist = addAerialist();
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, battlefieldIndex(aerialist), null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(aerialist.isTransformed()).isTrue();
    }

    @Test
    void emptyLibraryDoesNotDrawOrOfferAChoice() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of());
        Permanent aerialist = addAerialist();

        transform(aerialist);

        assertThat(aerialist.isTransformed()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void greenManaPaymentDoesNotCostLife() {
        harness.setLibrary(player1, List.of());
        Permanent aerialist = addAerialist();

        transform(aerialist);

        assertThat(aerialist.isTransformed()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        Permanent aerialist = addAerialist();
        prepareMainPhase();
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.GREEN, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(aerialist), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(aerialist.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileStackIsNonempty() {
        Permanent aerialist = addAerialist();
        prepareMainPhase();
        harness.addMana(player1, ManaColor.GREEN, 10);
        harness.activateAbility(player1, battlefieldIndex(aerialist), null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(aerialist), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void transformTriggerStillResolvesAfterSourceLeavesBattlefield() {
        SkyclaveAerialist topCard = new SkyclaveAerialist();
        harness.setLibrary(player1, List.of(topCard));
        Permanent aerialist = addAerialist();
        prepareMainPhase();
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.activateAbility(player1, battlefieldIndex(aerialist), null, null);
        harness.passBothPriorities();
        assertThat(aerialist.isTransformed()).isTrue();
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(aerialist);
        gd.playerGraveyards.get(player1.getId()).add(aerialist.getOriginalCard());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private Permanent addAerialist() {
        return harness.addToBattlefieldAndReturn(player1, new SkyclaveAerialist());
    }

    private void transform(Permanent aerialist) {
        prepareMainPhase();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, battlefieldIndex(aerialist), null, null);
        resolveAllTriggers();
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
