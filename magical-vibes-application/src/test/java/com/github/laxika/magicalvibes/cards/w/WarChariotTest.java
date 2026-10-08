package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({WarChariot.class, BalduvianBears.class, Forest.class, Disenchant.class})
class WarChariotTest extends BaseCardTest {

    @Test
    @DisplayName("Activating grants trample to the target and taps War Chariot")
    void grantsTrample() {
        Permanent chariot = addCreatureReady(player1, new WarChariot());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(chariot.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Trample wears off at end of turn")
    void trampleWearsOff() {
        addCreatureReady(player1, new WarChariot());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Activating can target an opponent's creature")
    void targetsOpponentCreature() {
        Permanent chariot = addCreatureReady(player1, new WarChariot());
        Permanent bears = addCreatureReady(player2, new BalduvianBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(chariot.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Targeting a non-creature is rejected")
    void illegalTargetRejected() {
        addCreatureReady(player1, new WarChariot());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A newly entered noncreature War Chariot can activate immediately")
    void newlyEnteredArtifactCanActivate() {
        Permanent chariot = harness.addToBattlefieldAndReturn(player1, new WarChariot());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        assertThat(chariot.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Activation requires all three mana")
    void insufficientManaRejected() {
        Permanent chariot = addCreatureReady(player1, new WarChariot());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(chariot.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("A tapped War Chariot cannot activate again")
    void tappedChariotCannotActivateAgain() {
        addCreatureReady(player1, new WarChariot());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability resolves even if War Chariot is destroyed in response")
    void abilitySurvivesSourceRemoval() {
        Permanent chariot = addCreatureReady(player1, new WarChariot());
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.castAndResolveInstant(player2, 0, chariot.getId());
        harness.assertNotOnBattlefield(player1, "War Chariot");
        harness.assertInGraveyard(player1, "War Chariot");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }
}
