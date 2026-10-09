package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrossoverCollaboration.class, GrizzlyBears.class})
class CrossoverCollaborationTest extends BaseCardTest {

    @Test
    void exilesTopTwoCardsWithoutTeamwork() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new CrossoverCollaboration()));
        addMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.exilePlayPermissions)
                .containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    void teamworkCreatesTreasureAndTapsTheCreature() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new CrossoverCollaboration()));
        Permanent teammate = addCreatureReady(player1, new GrizzlyBears());
        addMana();

        harness.castInstantWithSacrifices(player1, 0, null, List.of(teammate.getId()));
        harness.passBothPriorities();

        assertThat(teammate.isTapped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }


    @Test
    void teamworkCanTapSummoningSickCreaturesAndStillCreatesTreasureWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new CrossoverCollaboration()));
        Permanent teammate = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        teammate.setSummoningSick(true);
        addMana();

        harness.castInstantWithSacrifices(player1, 0, null, List.of(teammate.getId()));

        assertThat(teammate.isTapped()).isTrue();
        assertThat(countPermanents(player1, "Treasure")).isZero();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(teammate);
    }

    @Test
    void teamworkMayTapMoreCreaturesThanNeededButCreatesOnlyOneTreasure() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new CrossoverCollaboration()));
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        addMana();

        harness.castInstantWithSacrifices(player1, 0, null, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    void cannotUseAnAlreadyTappedCreatureForTeamwork() {
        harness.setHand(player1, List.of(new CrossoverCollaboration()));
        Permanent teammate = addCreatureReady(player1, new GrizzlyBears());
        teammate.tap();
        addMana();

        assertThatThrownBy(() -> harness.castInstantWithSacrifices(
                player1, 0, null, List.of(teammate.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void exilesOnlyAvailableCardAndAllowsCastingItForItsManaCost() {
        Card exiled = new CrossoverCollaboration();
        harness.setLibrary(player1, List.of(exiled));
        harness.setHand(player1, List.of(new CrossoverCollaboration()));
        addMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(exiled);
        assertThatThrownBy(() -> harness.castFromExile(player1, exiled.getId()))
                .isInstanceOf(IllegalStateException.class);

        addMana();
        harness.castFromExile(player1, exiled.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(exiled);
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }


    @Test
    void playPermissionLastsThroughNextTurnAndThenExpires() {
        harness.setHand(player2, List.of());
        Card first = new CrossoverCollaboration();
        Card second = new CrossoverCollaboration();
        harness.setLibrary(player1, List.of(first, second,
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new CrossoverCollaboration()));
        addMana();

        harness.castAndResolveInstant(player1, 0);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(first.getId(), player1.getId());
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).containsEntry(first.getId(), player1.getId());

        addMana();
        harness.castFromExile(player1, first.getId());
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(second.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(second);
        addMana();
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
