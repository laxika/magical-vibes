package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
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

@CardUsed({CaseOfTheLockedHothouse.class, CaseOfTheUneatenFeast.class, Forest.class, GrizzlyBears.class, Opt.class})
class CaseOfTheLockedHothouseTest extends BaseCardTest {

    @Test
    @DisplayName("Allows one additional land play while unsolved")
    void allowsAdditionalLandPlayWhileUnsolved() {
        harness.addToBattlefield(player1, new CaseOfTheLockedHothouse());
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playLand(player1, 0);
        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Forest")))
                .hasSize(2);
    }

    @Test
    @DisplayName("Solves at the beginning of the end step with seven lands")
    void solvesWithSevenLands() {
        Permanent hothouse = harness.addToBattlefieldAndReturn(player1, new CaseOfTheLockedHothouse());
        addSevenLands();

        resolveEndStepTriggers();

        assertThat(hothouse.isSolved()).isTrue();
    }

    @Test
    @DisplayName("Does not allow creature spells from the top while unsolved")
    void doesNotAllowCreatureSpellsBeforeSolved() {
        harness.addToBattlefield(player1, new CaseOfTheLockedHothouse());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Allows creature and enchantment spells from the top once solved")
    void allowsCreatureAndEnchantmentSpellsOnceSolved() {
        harness.addToBattlefield(player1, new CaseOfTheLockedHothouse());
        addSevenLands();
        resolveEndStepTriggers();

        harness.setLibrary(player1, List.of(new GrizzlyBears(), new CaseOfTheUneatenFeast()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveFromLibraryTop(player1);
        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Case of the Uneaten Feast");
    }

    @Test
    @DisplayName("Does not allow instant spells from the top once solved")
    void doesNotAllowInstantSpellsFromTop() {
        harness.addToBattlefield(player1, new CaseOfTheLockedHothouse());
        addSevenLands();
        resolveEndStepTriggers();
        harness.setLibrary(player1, List.of(new Opt()));

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addSevenLands() {
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
    }

    private void resolveEndStepTriggers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
