package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CarnageTyrant;
import com.github.laxika.magicalvibes.cards.k.KalonianBehemoth;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({ArcaneLighthouse.class, CarnageTyrant.class, KalonianBehemoth.class, Shock.class})
class ArcaneLighthouseTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping it adds one colorless mana")
    void tappingAddsColorlessMana() {
        Permanent lighthouse = harness.addToBattlefieldAndReturn(player1, new ArcaneLighthouse());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(lighthouse.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Its activated ability removes hexproof and shroud from opponents' creatures until end of turn")
    void removesOpponentHexproofAndShroudUntilEndOfTurn() {
        Permanent ownHexproof = harness.addToBattlefieldAndReturn(player1, new CarnageTyrant());
        Permanent lighthouse = harness.addToBattlefieldAndReturn(player1, new ArcaneLighthouse());
        Permanent opponentHexproof = harness.addToBattlefieldAndReturn(player2, new CarnageTyrant());
        Permanent opponentShroud = harness.addToBattlefieldAndReturn(player2, new KalonianBehemoth());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lighthouseIndex = gd.playerBattlefields.get(player1.getId()).indexOf(lighthouse);
        harness.activateAbility(player1, lighthouseIndex, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownHexproof, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentHexproof, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentShroud, Keyword.SHROUD)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, opponentHexproof, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentShroud, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("After activation, its controller can target opponents' hexproof and shroud creatures")
    void controllerCanTargetOpponentHexproofAndShroudCreatures() {
        Permanent lighthouse = harness.addToBattlefieldAndReturn(player1, new ArcaneLighthouse());
        Permanent opponentHexproof = harness.addToBattlefieldAndReturn(player2, new CarnageTyrant());
        Permanent opponentShroud = harness.addToBattlefieldAndReturn(player2, new KalonianBehemoth());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 2);

        int lighthouseIndex = gd.playerBattlefields.get(player1.getId()).indexOf(lighthouse);
        harness.activateAbility(player1, lighthouseIndex, 1, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.castInstant(player1, 0, opponentHexproof.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, opponentShroud.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Without activation, opponents' hexproof creatures remain untargetable")
    void opponentHexproofStillBlocksWithoutActivation() {
        Permanent opponentHexproof = harness.addToBattlefieldAndReturn(player2, new CarnageTyrant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentHexproof.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }
}
