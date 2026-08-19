package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MirrexTest extends BaseCardTest {

    @Test
    @DisplayName("First ability adds one colorless mana")
    void tapsForColorless() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mirrex());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability adds a chosen color only during the turn Mirrex entered")
    void conditionalAnyColorMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mirrex());
        gd.permanentsEnteredBattlefieldThisTurn.put(
                player1.getId(), new ArrayList<>(List.of(land.getCard())));

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);

        advanceTurn();
        advanceTurn();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Activate only if this land entered this turn");
    }

    @Test
    @DisplayName("Third ability creates a toxic Mite that cannot block")
    void createsToxicMite() {
        harness.addToBattlefield(player1, new Mirrex());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        Permanent mite = findPermanent(player1, "Mite");
        assertThat(mite.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(mite.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(mite.getCard().getSubtypes()).containsExactlyInAnyOrder(
                CardSubtype.PHYREXIAN, CardSubtype.MITE);
        assertThat(mite.hasKeyword(Keyword.TOXIC)).isTrue();
        assertThat(bls.canBlock(gd, mite)).isFalse();

        mite.setAttacking(true);
        resolveCombat(player1);

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
