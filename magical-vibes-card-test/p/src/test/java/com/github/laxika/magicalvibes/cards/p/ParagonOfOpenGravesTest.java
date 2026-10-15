package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.ChildOfNight;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ParagonOfOpenGraves.class, ChildOfNight.class, RuneclawBear.class})
class ParagonOfOpenGravesTest extends BaseCardTest {

    @Test
    @DisplayName("Other black creatures you control get +1/+1")
    void buffsOtherBlackCreaturesYouControl() {
        Permanent paragon = addReady(player1, new ParagonOfOpenGraves());
        Permanent child = addReady(player1, new ChildOfNight());

        assertThat(gqs.getEffectivePower(gd, paragon)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, paragon)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, child)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, child)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff nonblack or opponent creatures")
    void onlyBuffsOwnBlackCreatures() {
        addReady(player1, new ParagonOfOpenGraves());
        Permanent bears = addReady(player1, new RuneclawBear());
        Permanent opponentChild = addReady(player2, new ChildOfNight());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentChild)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentChild)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating grants deathtouch to another black creature you control")
    void grantsDeathtouchToAnotherBlackCreature() {
        Permanent paragon = addReady(player1, new ParagonOfOpenGraves());
        Permanent child = addReady(player1, new ChildOfNight());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, indexOf(player1, paragon), 0, child.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, child, Keyword.DEATHTOUCH)).isTrue();
        assertThat(paragon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Deathtouch wears off at end of turn")
    void deathtouchWearsOffAtEndOfTurn() {
        Permanent paragon = addReady(player1, new ParagonOfOpenGraves());
        Permanent child = addReady(player1, new ChildOfNight());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, indexOf(player1, paragon), 0, child.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, child, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, child, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Cannot target itself, a nonblack creature, or an opponent's creature")
    void restrictsActivationTarget() {
        Permanent paragon = addReady(player1, new ParagonOfOpenGraves());
        Permanent bears = addReady(player1, new RuneclawBear());
        Permanent opponentChild = addReady(player2, new ChildOfNight());
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, paragon), 0, paragon.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another black creature");
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, paragon), 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another black creature");
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, paragon), 0, opponentChild.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another black creature");
    }

    @Test
    @DisplayName("Two Paragons boost each other and both boost another black creature")
    void multipleParagonsStackTheirBoosts() {
        Permanent first = addReady(player1, new ParagonOfOpenGraves());
        Permanent second = addReady(player1, new ParagonOfOpenGraves());
        Permanent child = addReady(player1, new ChildOfNight());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, child)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, child)).isEqualTo(3);
    }

    @Test
    @DisplayName("The activated ability resolves after its source leaves the battlefield")
    void abilitySurvivesSourceLeavingBattlefield() {
        Permanent paragon = addReady(player1, new ParagonOfOpenGraves());
        Permanent child = addReady(player1, new ChildOfNight());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, indexOf(player1, paragon), 0, child.getId());
        gd.playerBattlefields.get(player1.getId()).remove(paragon);
        gd.playerGraveyards.get(player1.getId()).add(paragon.getCard());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, child, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.getEffectivePower(gd, child)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, child)).isEqualTo(1);
    }

    @Test
    @DisplayName("A target that changes controller before resolution does not gain deathtouch")
    void targetMustStillBeControlledAtResolution() {
        Permanent paragon = addReady(player1, new ParagonOfOpenGraves());
        Permanent child = addReady(player1, new ChildOfNight());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, indexOf(player1, paragon), 0, child.getId());
        gd.playerBattlefields.get(player1.getId()).remove(child);
        gd.playerBattlefields.get(player2.getId()).add(child);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, child, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.getEffectivePower(gd, child)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, child)).isEqualTo(1);
    }

    @Test
    @DisplayName("A summoning-sick Paragon cannot pay its tap cost")
    void summoningSicknessPreventsActivation() {
        Permanent paragon = harness.addToBattlefieldAndReturn(player1, new ParagonOfOpenGraves());
        paragon.setSummoningSick(true);
        Permanent child = addReady(player1, new ChildOfNight());
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, paragon), 0, child.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(paragon.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, child, Keyword.DEATHTOUCH)).isFalse();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
