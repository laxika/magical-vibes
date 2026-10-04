package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ElvishSpiritGuide.class)
class ElvishSpiritGuideTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling from hand adds {G} to the pool")
    void exilingFromHandAddsGreenMana() {
        harness.setHand(player1, List.of(new ElvishSpiritGuide()));

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("The card is exiled, not discarded, and the ability never uses the stack")
    void cardIsExiledAndAbilityDoesNotUseTheStack() {
        harness.setHand(player1, List.of(new ElvishSpiritGuide()));

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .containsExactly("Elvish Spirit Guide");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The added mana can pay for a spell")
    void addedManaPaysForASpell() {
        harness.setHand(player1, List.of(new ElvishSpiritGuide(), new ElvishSpiritGuide()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Elvish Spirit Guide");
    }

    @Test
    @DisplayName("Mana ability resolves immediately while a creature spell is on the stack")
    void activatesWithSpellOnStack() {
        ElvishSpiritGuide spell = new ElvishSpiritGuide();
        ElvishSpiritGuide manaSource = new ElvishSpiritGuide();
        harness.setHand(player1, List.of(spell, manaSource));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        var stackBeforeActivation = List.copyOf(gd.stack);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.stack).containsExactlyElementsOf(stackBeforeActivation);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(entry -> entry.card()).containsExactly(manaSource);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Elvish Spirit Guide");
    }

    @Test
    @DisplayName("The nonactive player can exile a Spirit Guide for their own mana")
    void activatesDuringOpponentsTurn() {
        ElvishSpiritGuide manaSource = new ElvishSpiritGuide();
        harness.setHand(player2, List.of(manaSource));
        harness.forceActivePlayer(player1);

        harness.activateHandAbility(player2, 0, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(entry -> entry.card()).containsExactly(manaSource);
        assertThat(gd.stack).isEmpty();
    }
}
