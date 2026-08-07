package com.github.laxika.magicalvibes.cards.b;

import java.util.List;
import java.util.Set;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlindWithAngerTest extends BaseCardTest {

    private void castAt(Permanent target) {
        harness.setHand(player1, List.of(new BlindWithAnger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castInstant(player1, 0, target.getId());
    }

    @Test
    @DisplayName("Untaps the target, steals it and gives it haste")
    void untapsStealsAndGrantsHaste() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        castAt(target);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
    }

    @Test
    @DisplayName("Control and haste wear off at end of turn")
    void controlAndHasteExpire() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        castAt(target);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
    }

    @Test
    @DisplayName("Cannot target a legendary creature")
    void cannotTargetLegendaryCreature() {
        addCreatureReady(player1, new GrizzlyBears()); // legal target so the spell is playable
        GrizzlyBears legendaryBears = new GrizzlyBears();
        legendaryBears.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        Permanent legendary = addCreatureReady(player2, legendaryBears);

        harness.setHand(player1, List.of(new BlindWithAnger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, legendary.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonlegendary creature");
    }
}
