package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WalkThePlank;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ElendaSaintOfDuskTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 and menace above starting life, then an additional +5/+5 at 30 life")
    void lifeThresholdsAreDynamic() {
        Permanent elenda = harness.addToBattlefieldAndReturn(player1, new ElendaSaintOfDusk());

        assertThat(gqs.getEffectivePower(gd, elenda)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, elenda, Keyword.MENACE)).isFalse();

        harness.setLife(player1, 21);
        assertThat(gqs.getEffectivePower(gd, elenda)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, elenda)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, elenda, Keyword.MENACE)).isTrue();

        harness.setLife(player1, 30);
        assertThat(gqs.getEffectivePower(gd, elenda)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, elenda)).isEqualTo(10);

        harness.setLife(player1, 29);
        assertThat(gqs.getEffectivePower(gd, elenda)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, elenda, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Opponent instant spells cannot target Elenda")
    void opponentInstantCannotTarget() {
        harness.addToBattlefield(player1, new ElendaSaintOfDusk());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0,
                harness.getPermanentId(player1, "Elenda, Saint of Dusk")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof from instants");
    }

    @Test
    @DisplayName("Opponent sorcery spells can target Elenda")
    void opponentSorceryCanTarget() {
        harness.addToBattlefield(player1, new ElendaSaintOfDusk());
        harness.setHand(player2, List.of(new WalkThePlank()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player2, 0,
                harness.getPermanentId(player1, "Elenda, Saint of Dusk"));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Elenda, Saint of Dusk"));
    }

    @Test
    @DisplayName("The controller's instant spells can target Elenda")
    void controllerInstantCanTarget() {
        harness.addToBattlefield(player1, new ElendaSaintOfDusk());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0,
                harness.getPermanentId(player1, "Elenda, Saint of Dusk"));

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Shock"));
    }
}
