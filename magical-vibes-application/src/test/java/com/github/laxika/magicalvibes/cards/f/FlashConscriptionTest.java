package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
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

@CardUsed({FlashConscription.class, GrizzlyBears.class, Pacifism.class})
class FlashConscriptionTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps, steals, and grants haste to the target creature")
    void untapsStealsAndGrantsHaste() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        castFlashConscription(target, false);

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("White mana grants life equal to the stolen creature's combat damage")
    void whiteManaGrantsCombatDamageLifeGain() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castFlashConscription(target, true);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(target)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Without white mana, the stolen creature does not grant life from combat damage")
    void noWhiteManaMeansNoCombatDamageLifeGain() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castFlashConscription(target, false);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(target)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Control and haste expire at end of turn")
    void temporaryEffectsExpireAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castFlashConscription(target, true);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent enchantment = new Permanent(new Pacifism());
        gd.playerBattlefields.get(player2.getId()).add(enchantment);
        harness.setHand(player1, List.of(new FlashConscription()));
        addMana(false);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void castFlashConscription(Permanent target, boolean whiteSpent) {
        harness.setHand(player1, List.of(new FlashConscription()));
        addMana(whiteSpent);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addMana(boolean whiteSpent) {
        harness.addMana(player1, ManaColor.RED, 1);
        if (whiteSpent) {
            harness.addMana(player1, ManaColor.WHITE, 5);
        } else {
            harness.addMana(player1, ManaColor.COLORLESS, 5);
        }
    }
}
