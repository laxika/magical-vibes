package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
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

        harness.castFromLibraryTop(player1);
        harness.passBothPriorities();

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

    private void addMmmenonAndReadyOrnithopter() {
        harness.addToBattlefield(player1, new MmmenonTheRightHand());
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        ornithopter.setSummoningSick(false);
    }
}
