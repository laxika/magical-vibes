package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.r.Resize;
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

@CardUsed({FrostRaptor.class, Resize.class})
class FrostRaptorTest extends BaseCardTest {

    @Test
    @DisplayName("A tapped, summoning-sick raptor can activate its ability")
    void tappedSummoningSickRaptorCanActivate() {
        Permanent raptor = harness.addToBattlefieldAndReturn(player1, new FrostRaptor());
        raptor.setSummoningSick(true);
        raptor.tap();
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, raptor, Keyword.SHROUD)).isTrue();
        assertThat(raptor.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Shroud in response makes a targeted spell fail to resolve")
    void shroudInResponseStopsTargetedSpell() {
        Permanent raptor = addCreatureReady(player1, new FrostRaptor());
        harness.setHand(player2, List.of(new Resize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castInstant(player2, 0, raptor.getId());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.BLUE, 1);
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, raptor, Keyword.SHROUD)).isTrue();
        assertThat(gqs.getEffectivePower(gd, raptor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, raptor)).isEqualTo(2);
        harness.assertInGraveyard(player2, "Resize");
    }

    @Test
    @DisplayName("Shroud does not prevent activating the ability again")
    void canActivateWhileShrouded() {
        Permanent raptor = addCreatureReady(player1, new FrostRaptor());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.BLUE, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, raptor, Keyword.SHROUD)).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isZero();
    }

    @Test
    @DisplayName("One snow mana plus regular mana cannot pay the activation cost")
    void mixedSnowAndRegularManaCannotPayCost() {
        addCreatureReady(player1, new FrostRaptor());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Two snow mana grants shroud until end of turn")
    void snowManaGrantsShroud() {
        Permanent raptor = addCreatureReady(player1, new FrostRaptor());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, raptor, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("One snow mana cannot pay the two-snow activation cost")
    void oneSnowManaCannotPayActivationCost() {
        addCreatureReady(player1, new FrostRaptor());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Regular mana cannot pay the snow activation cost")
    void regularManaCannotPaySnowCost() {
        addCreatureReady(player1, new FrostRaptor());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Shroud wears off at end of turn")
    void shroudWearsOffAtEndOfTurn() {
        Permanent raptor = addCreatureReady(player1, new FrostRaptor());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, raptor, Keyword.SHROUD)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, raptor, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Granted shroud prevents spells from targeting the raptor")
    void grantedShroudPreventsSpellTargeting() {
        Permanent raptor = addCreatureReady(player1, new FrostRaptor());
        gd.playerManaPools.get(player1.getId()).addSnowMana(ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Resize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, raptor.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }
}
