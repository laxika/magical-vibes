package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SafeholdDuoTest extends BaseCardTest {

    @BeforeEach
    void setUp() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new SafeholdDuo());
    }

    private void castGreenSpell() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
    }

    private void castWhiteSpell() {
        harness.setHand(player1, List.of(new EliteVanguard()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
    }

    private Permanent duo() {
        GameData gd = harness.getGameData();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Safehold Duo"))
                .findFirst().orElseThrow();
    }

    @Test
    @DisplayName("Casting a green spell gives Safehold Duo +1/+1 until end of turn")
    void greenSpellBoosts() {
        castGreenSpell();
        harness.passBothPriorities(); // resolve the trigger

        assertThat(duo().getPowerModifier()).isEqualTo(1);
        assertThat(duo().getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a white spell gives Safehold Duo vigilance until end of turn")
    void whiteSpellGrantsVigilance() {
        assertThat(duo().hasKeyword(Keyword.VIGILANCE)).isFalse();

        castWhiteSpell();
        harness.passBothPriorities(); // resolve the trigger

        assertThat(duo().hasKeyword(Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Casting a non-green, non-white spell does not trigger either ability")
    void otherColorDoesNotTrigger() {
        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(duo().getPowerModifier()).isEqualTo(0);
        assertThat(duo().hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The +1/+1 boost wears off at end of turn")
    void boostWearsOff() {
        castGreenSpell();
        harness.passBothPriorities();
        assertThat(duo().getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(duo().getPowerModifier()).isEqualTo(0);
        assertThat(duo().getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Vigilance wears off at end of turn")
    void vigilanceWearsOff() {
        castWhiteSpell();
        harness.passBothPriorities();
        assertThat(duo().hasKeyword(Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(duo().hasKeyword(Keyword.VIGILANCE)).isFalse();
    }
}
