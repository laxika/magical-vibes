package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
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

class AppealAuthorityTest extends BaseCardTest {

    @Test
    @DisplayName("Appeal gives target +X/+X and trample where X = creatures you control")
    void appealPumpsAndGrantsTrample() {
        Permanent own1 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent own2 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AppealAuthority()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, own1.getId());
        harness.passBothPriorities();

        // X = 2 (own1 + own2); opponent creature ignored
        assertThat(own1.getPowerModifier()).isEqualTo(2);
        assertThat(own1.getToughnessModifier()).isEqualTo(2);
        assertThat(own1.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(own2.getPowerModifier()).isEqualTo(0);
        assertThat(own2.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Appeal boost and trample wear off at end of turn")
    void appealEffectsWearOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AppealAuthority()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Appeal cannot target a non-creature")
    void appealCannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new AppealAuthority()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Authority taps two opponent creatures and grants vigilance, then exiles")
    void authorityTapsGrantsVigilanceAndExiles() {
        Permanent opp1 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opp2 = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new AppealAuthority()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0, List.of(opp1.getId(), opp2.getId()));
        harness.passBothPriorities();

        assertThat(opp1.isTapped()).isTrue();
        assertThat(opp2.isTapped()).isTrue();
        assertThat(own.hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(opp1.hasKeyword(Keyword.VIGILANCE)).isFalse();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Appeal") || c.getName().equals("Authority"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Appeal"));
    }

    @Test
    @DisplayName("Authority may tap zero creatures and still grants vigilance")
    void authorityWithZeroTargetsStillGrantsVigilance() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new AppealAuthority()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(own.hasKeyword(Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Authority cannot target your own creature")
    void authorityCannotTargetOwnCreature() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new AppealAuthority()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, List.of(own.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Authority vigilance wears off at end of turn")
    void authorityVigilanceWearsOffAtEndOfTurn() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new AppealAuthority()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0, List.of());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(own.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Authority requires sorcery timing")
    void authorityRequiresSorceryTiming() {
        Permanent opp = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new AppealAuthority()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, List.of(opp.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery-speed");
    }
}
