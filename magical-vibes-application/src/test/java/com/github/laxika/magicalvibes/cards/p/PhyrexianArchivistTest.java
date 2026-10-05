package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AlabasterHostIntercessor;
import com.github.laxika.magicalvibes.cards.c.CrystalCarapace;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhyrexianArchivist.class, AlabasterHostIntercessor.class, CrystalCarapace.class})
class PhyrexianArchivistTest extends BaseCardTest {

    @Test
    void putsTargetCardFromOwnGraveyardOnBottomOfOwnersLibrary() {
        int archivistIndex = addReadyArchivist();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Card target = new AlabasterHostIntercessor();
        Card libraryCard = new CrystalCarapace();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(libraryCard));

        harness.activateAbilityWithGraveyardTargets(player1, archivistIndex, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(libraryCard.getId(), target.getId());
    }

    @Test
    void putsTargetCardFromOpponentsGraveyardOnBottomOfOwnersLibrary() {
        int archivistIndex = addReadyArchivist();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Card target = new AlabasterHostIntercessor();
        Card libraryCard = new CrystalCarapace();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of(libraryCard));

        harness.activateAbilityWithGraveyardTargets(player1, archivistIndex, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(libraryCard.getId(), target.getId());
    }

    @Test
    void doesNothingIfTargetLeavesGraveyardBeforeResolution() {
        int archivistIndex = addReadyArchivist();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Card target = new AlabasterHostIntercessor();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(new CrystalCarapace()));

        harness.activateAbilityWithGraveyardTargets(player1, archivistIndex, 0, List.of(target.getId()));
        gd.playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(target.getId());
    }

    @Test
    void cannotActivateWithoutPayingMana() {
        int archivistIndex = addReadyArchivist();
        Card target = new AlabasterHostIntercessor();
        harness.setGraveyard(player1, List.of(target));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, archivistIndex, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetNoncreatureCardAndPutItIntoEmptyLibrary() {
        int archivistIndex = addReadyArchivist();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Card target = new CrystalCarapace();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of());

        harness.activateAbilityWithGraveyardTargets(player1, archivistIndex, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target);
    }

    @Test
    void tappingToActivatePreventsAnotherActivation() {
        int archivistIndex = addReadyArchivist();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        Card target = new CrystalCarapace();
        harness.setGraveyard(player1, List.of(target));

        harness.activateAbilityWithGraveyardTargets(player1, archivistIndex, 0, List.of(target.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()).get(archivistIndex).isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, archivistIndex, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent archivist = harness.addToBattlefieldAndReturn(player1, new PhyrexianArchivist());
        archivist.setSummoningSick(true);
        int archivistIndex = gd.playerBattlefields.get(player1.getId()).indexOf(archivist);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Card target = new CrystalCarapace();
        harness.setGraveyard(player1, List.of(target));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, archivistIndex, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void abilityStillResolvesAfterArchivistLeavesBattlefield() {
        int archivistIndex = addReadyArchivist();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Card target = new CrystalCarapace();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of());

        harness.activateAbilityWithGraveyardTargets(player1, archivistIndex, 0, List.of(target.getId()));
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(target);
    }

    private int addReadyArchivist() {
        Permanent archivist = harness.addToBattlefieldAndReturn(player1, new PhyrexianArchivist());
        archivist.setSummoningSick(false);
        return gd.playerBattlefields.get(player1.getId()).indexOf(archivist);
    }
}
