package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Mirrex.class})
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
        harness.enterBattlefieldAndReturn(player1, new Mirrex());

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
        assertThat(mite.getCard().isToken()).isTrue();
        assertThat(mite.getCard().getPower()).isEqualTo(1);
        assertThat(mite.getCard().getToughness()).isEqualTo(1);
        assertThat(mite.getCard().getColor()).isNull();
        assertThat(mite.getCard().getSubtypes()).containsExactlyInAnyOrder(
                CardSubtype.PHYREXIAN, CardSubtype.MITE);
        assertThat(mite.hasKeyword(Keyword.TOXIC)).isTrue();
        assertThat(bls.canBlock(gd, mite)).isFalse();

        mite.setSummoningSick(false);
        mite.setAttacking(true);
        resolveCombat(player1);

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mite toxic gives poison immediately with combat damage without using the stack")
    void toxicIsAnImmediateResultOfCombatDamage() {
        harness.addToBattlefield(player1, new Mirrex());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        Permanent mite = findPermanent(player1, "Mite");
        mite.setSummoningSick(false);
        mite.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Token ability pays three mana and taps Mirrex before resolving")
    void tokenCreationUsesTheStackAndPaysItsCosts() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mirrex());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(findPermanent(player1, "Mite").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Any-color mana is available when Mirrex entered during an opponent's turn")
    void anyColorManaOnOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.enterBattlefieldAndReturn(player1, new Mirrex());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
