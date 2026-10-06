package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.SazhsChocobo;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.d.DisdainfulStroke;
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

@CardUsed({RestorationMagic.class, SazhsChocobo.class, Plains.class, DisdainfulStroke.class})
class RestorationMagicTest extends BaseCardTest {

    @Test
    @DisplayName("Cure grants hexproof and indestructible to the target without gaining life")
    void cureProtectsTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SazhsChocobo());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new SazhsChocobo());
        harness.setLife(player1, 10);
        prepareCard(0, 1, 0);

        harness.castModalInstant(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.HEXPROOF)).isFalse();
        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("Cura protects the target and gains 3 life")
    void curaProtectsTargetAndGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SazhsChocobo());
        harness.setLife(player1, 10);
        prepareCard(1, 1, 0);

        harness.castModalInstant(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
        harness.assertLife(player1, 13);
    }

    @Test
    @DisplayName("Curaga grants temporary protection to controlled permanents and gains 6 life")
    void curagaProtectsControlledPermanents() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new SazhsChocobo());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new SazhsChocobo());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new SazhsChocobo());
        harness.setLife(player1, 10);
        prepareCard(3, 1, 1);

        harness.castModalInstant(player1, 0, 2, List.of());
        harness.passBothPriorities();

        for (Permanent permanent : List.of(first, second)) {
            assertThat(gqs.hasKeyword(gd, permanent, Keyword.HEXPROOF)).isTrue();
            assertThat(gqs.hasKeyword(gd, permanent, Keyword.INDESTRUCTIBLE)).isTrue();
        }
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.INDESTRUCTIBLE)).isFalse();
        harness.assertLife(player1, 16);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, first, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, first, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Cure can protect an opponent's noncreature permanent")
    void cureProtectsOpposingLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        prepareCard(0, 1, 0);

        harness.castModalInstant(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Cura does not gain life when its only target leaves the battlefield")
    void curaDoesNotGainLifeWithMissingTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SazhsChocobo());
        harness.setLife(player1, 10);
        prepareCard(1, 1, 0);

        harness.castModalInstant(player1, 0, 1, List.of(target.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("Curaga protects lands present at resolution but not later arrivals")
    void curagaProtectsOnlyPermanentsPresentAtResolution() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        prepareCard(3, 1, 1);

        harness.castModalInstant(player1, 0, 2, List.of());
        harness.passBothPriorities();
        Permanent later = harness.addToBattlefieldAndReturn(player1, new Plains());

        assertThat(gqs.hasKeyword(gd, land, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, later, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, later, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Curaga gains life even with no controlled permanents")
    void curagaGainsLifeOnEmptyBattlefield() {
        harness.setLife(player1, 10);
        prepareCard(3, 1, 1);

        harness.castModalInstant(player1, 0, 2, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 16);
    }

    @Test
    @DisplayName("Disdainful Stroke cannot target Restoration Magic cast with Curaga")
    void curagaIsNotAnEligibleDisdainfulStrokeTarget() {
        prepareCard(3, 1, 1);
        harness.setHand(player2, List.of(new DisdainfulStroke()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castModalInstant(player1, 0, 2, List.of());

        assertThat(gd.stack).hasSize(1);
        var spellId = gd.stack.getFirst().getCard().getId();
        assertThatThrownBy(() -> harness.castInstant(player2, 0, spellId))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();
    }

    private void prepareCard(int colorless, int white, int extraWhite) {
        harness.setHand(player1, List.of(new RestorationMagic()));
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
        harness.addMana(player1, ManaColor.WHITE, white + extraWhite);
    }
}
