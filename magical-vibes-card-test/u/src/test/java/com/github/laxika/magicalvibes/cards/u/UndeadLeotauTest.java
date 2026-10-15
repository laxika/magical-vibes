package com.github.laxika.magicalvibes.cards.u;

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

@CardUsed({UndeadLeotau.class})
@DisplayName("Undead Leotau")
class UndeadLeotauTest extends BaseCardTest {

    private Permanent addLeotauReady() {
        return addCreatureReady(player1, new UndeadLeotau());
    }

    @Test
    @DisplayName("{R} gives +1/-1 until end of turn")
    void abilityBoosts() {
        Permanent leotau = addLeotauReady();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(leotau.getPowerModifier()).isEqualTo(1);
        assertThat(leotau.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent leotau = addLeotauReady();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(leotau.getPowerModifier()).isEqualTo(0);
        assertThat(leotau.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Unearth returns Undead Leotau to the battlefield with haste")
    void unearthReturnsWithHaste() {
        UndeadLeotau card = new UndeadLeotau();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Undead Leotau");
        assertThat(gqs.hasKeyword(gd, perm, Keyword.HASTE)).isTrue();
        harness.assertNotInGraveyard(player1, "Undead Leotau");
    }

    @Test
    @DisplayName("Unearthed Undead Leotau is exiled at the next end step")
    void unearthExiledAtEndStep() {
        UndeadLeotau card = new UndeadLeotau();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Undead Leotau");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Undead Leotau");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Undead Leotau"));
    }

    @Test
    @DisplayName("Repeated red activations stack and zero toughness puts Leotau in the graveyard")
    void repeatedBoostsEventuallyKillLeotau() {
        Permanent leotau = addLeotauReady();
        harness.addMana(player1, ManaColor.RED, 4);

        for (int i = 1; i <= 3; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
            assertThat(leotau.getPowerModifier()).isEqualTo(i);
            assertThat(leotau.getToughnessModifier()).isEqualTo(-i);
            harness.assertOnBattlefield(player1, "Undead Leotau");
        }
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Undead Leotau");
        harness.assertInGraveyard(player1, "Undead Leotau");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An unearthed Leotau with zero toughness is exiled instead of dying")
    void unearthedLeotauIsExiledInsteadOfDying() {
        UndeadLeotau card = new UndeadLeotau();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 4);

        for (int i = 0; i < 4; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        harness.assertNotOnBattlefield(player1, "Undead Leotau");
        harness.assertNotInGraveyard(player1, "Undead Leotau");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    @Test
    @DisplayName("Unearth cannot be activated outside a main phase")
    void unearthRequiresMainPhase() {
        harness.setGraveyard(player1, List.of(new UndeadLeotau()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Undead Leotau");
        harness.assertNotOnBattlefield(player1, "Undead Leotau");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Unearth cannot be activated while another ability is on the stack")
    void unearthRequiresEmptyStack() {
        addLeotauReady();
        harness.setGraveyard(player1, List.of(new UndeadLeotau()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Undead Leotau");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Unearth cannot be activated during the opponent's main phase")
    void unearthRequiresOwnTurn() {
        harness.setGraveyard(player2, List.of(new UndeadLeotau()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player2, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player2, "Undead Leotau");
        harness.assertNotOnBattlefield(player2, "Undead Leotau");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Colorless mana cannot pay the red activation cost")
    void boostRequiresRedMana() {
        Permanent leotau = addLeotauReady();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(leotau.getPowerModifier()).isZero();
        assertThat(leotau.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
