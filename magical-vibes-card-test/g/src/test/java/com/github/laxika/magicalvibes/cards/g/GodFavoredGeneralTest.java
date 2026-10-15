package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GodFavoredGeneral.class})
class GodFavoredGeneralTest extends BaseCardTest {

    @Test
    void payingManaCreatesTwoSoldierEnchantmentCreatureTokens() {
        Permanent general = addTappedGeneral();

        advanceToUntapStep();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        assertThat(general.isTapped()).isFalse();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Soldier"))
                .hasSize(2)
                .allSatisfy(token -> {
                    assertThat(token.getCard().getPower()).isEqualTo(1);
                    assertThat(token.getCard().getToughness()).isEqualTo(1);
                    assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SOLDIER);
                    assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
                    assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ENCHANTMENT);
                });
    }

    @Test
    void decliningInspiredAbilityCreatesNoTokens() {
        addTappedGeneral();

        advanceToUntapStep();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Soldier"));
    }

    @Test
    void colorlessManaCannotPayTheWhitePartOfInspiredCost() {
        addTappedGeneral();

        advanceToUntapStep();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Soldier"));
    }

    @Test
    void alreadyUntappedGeneralDoesNotTriggerDuringUntapStep() {
        harness.addToBattlefield(player1, new GodFavoredGeneral());

        advanceToUntapStep();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Soldier"));
    }

    @Test
    void untappingOneGeneralDoesNotTriggerAnotherGeneral() {
        addTappedGeneral();
        harness.addToBattlefield(player1, new GodFavoredGeneral());

        advanceToUntapStep();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Soldier"))
                .hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addTappedGeneral() {
        Permanent general = harness.addToBattlefieldAndReturn(player1, new GodFavoredGeneral());
        general.setSummoningSick(false);
        general.tap();
        return general;
    }

    private void advanceToUntapStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.UPKEEP);
    }
}
