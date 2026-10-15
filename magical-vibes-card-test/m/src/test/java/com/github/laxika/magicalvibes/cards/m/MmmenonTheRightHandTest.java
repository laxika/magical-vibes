package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MmmenonTheRightHand.class, Bonesplitter.class, GrizzlyBears.class, Ornithopter.class})
class MmmenonTheRightHandTest extends BaseCardTest {

    @Test
    @DisplayName("Artifacts you control gain the restricted blue mana ability")
    void grantsRestrictedManaAbilityToArtifacts() {
        addMmmenonAndReadyOrnithopter();

        harness.activateAbility(player1, 1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getNonHandSpellOnlyMana(ManaColor.BLUE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Restricted blue mana pays for an artifact cast from the top of the library")
    void restrictedManaPaysForArtifactFromLibraryTop() {
        addMmmenonAndReadyOrnithopter();
        harness.activateAbility(player1, 1, 0, null, null);
        Card artifact = new Bonesplitter();
        harness.setLibrary(player1, List.of(artifact));

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Bonesplitter");
        assertThat(gd.playerManaPools.get(player1.getId()).getNonHandSpellOnlyManaTotal()).isZero();
    }

    @Test
    @DisplayName("Restricted blue mana cannot pay for a spell cast from hand")
    void restrictedManaCannotPayForHandSpell() {
        addMmmenonAndReadyOrnithopter();
        harness.activateAbility(player1, 1, 0, null, null);
        Card artifact = new Bonesplitter();
        harness.setHand(player1, List.of(artifact));

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getNonHandSpellOnlyMana(ManaColor.BLUE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("The top-library permission applies only to artifact spells")
    void onlyArtifactsCanBeCastFromLibraryTop() {
        harness.addToBattlefield(player1, new MmmenonTheRightHand());
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(creature);
    }

    @Test
    void looksAtNonartifactTopCardPrivatelyDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new MmmenonTheRightHand());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Grizzly Bears") && message.contains("}],[]]"));
        assertThat(harness.getConn2().getSentMessages()).anyMatch(message ->
                message.contains("\"revealedLibraryTopCards\":[[],[]]"));
    }

    @Test
    void canCastSuccessiveArtifactCreaturesFromLibraryTop() {
        harness.addToBattlefield(player1, new MmmenonTheRightHand());
        harness.setLibrary(player1, List.of(new Ornithopter(), new Ornithopter()));

        harness.castAndResolveFromLibraryTop(player1);
        harness.castAndResolveFromLibraryTop(player1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof Ornithopter).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void libraryPermissionDoesNotGrantFlash() {
        harness.addToBattlefield(player1, new MmmenonTheRightHand());
        Card artifact = new Ornithopter();
        harness.setLibrary(player1, List.of(artifact));
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(artifact);
    }

    @Test
    void noncreatureArtifactsCanUseGrantedManaAbilityImmediately() {
        harness.addToBattlefield(player1, new MmmenonTheRightHand());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());

        harness.activateAbility(player1, 1, 1, null, null);

        assertThat(equipment.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getNonHandSpellOnlyMana(ManaColor.BLUE))
                .isEqualTo(1);
    }

    @Test
    void artifactCreaturesNeedToOvercomeSummoningSicknessForGrantedTapAbility() {
        harness.addToBattlefield(player1, new MmmenonTheRightHand());
        harness.addToBattlefield(player1, new Ornithopter());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getNonHandSpellOnlyManaTotal()).isZero();
    }

    @Test
    void opponentsArtifactsDoNotGainManaAbility() {
        harness.addToBattlefield(player1, new MmmenonTheRightHand());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        artifact.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player2.getId()).getNonHandSpellOnlyManaTotal()).isZero();
    }

    @Test
    void restrictedManaCannotPayEquipCost() {
        addMmmenonAndReadyOrnithopter();
        harness.addToBattlefield(player1, new Bonesplitter());
        harness.activateAbility(player1, 1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 2, 0, null,
                harness.getPermanentId(player1, "Ornithopter")))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getNonHandSpellOnlyMana(ManaColor.BLUE))
                .isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private void addMmmenonAndReadyOrnithopter() {
        harness.addToBattlefield(player1, new MmmenonTheRightHand());
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        ornithopter.setSummoningSick(false);
    }
}
