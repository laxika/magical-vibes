package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FortuneTellersTalent.class, Forest.class, GrizzlyBears.class, Shock.class})
class FortuneTellersTalentTest extends BaseCardTest {

    @Test
    @DisplayName("At level 1, does not allow playing from the top")
    void levelOneDoesNotAllowPlayingFromTop() {
        harness.addToBattlefield(player1, new FortuneTellersTalent());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("At level 2, only allows playing from the top after casting a spell")
    void levelTwoRequiresSpellCastThisTurn() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new FortuneTellersTalent());
        levelUpToTwo(talent);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("At level 2, allows playing lands and casting spells from the top")
    void levelTwoAllowsLandsAndSpellsFromTop() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new FortuneTellersTalent());
        levelUpToTwo(talent);
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.castFromLibraryTop(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("At level 3, reduces non-hand spell costs by two")
    void levelThreeReducesNonHandSpellCostOnly() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new FortuneTellersTalent());
        levelUpToThree(talent);
        GrizzlyBears topBears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topBears));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveFromLibraryTop(player1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void levelUpToTwo(Permanent talent) {
        prepareForSorcery();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, battlefieldIndex(talent), 0, null, null);
        harness.passBothPriorities();
    }

    private void levelUpToThree(Permanent talent) {
        levelUpToTwo(talent);
        prepareForSorcery();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, battlefieldIndex(talent), 1, null, null);
        harness.passBothPriorities();
    }

    private void prepareForSorcery() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int battlefieldIndex(Permanent talent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(talent);
    }
}
