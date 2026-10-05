package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.ScrapyardMongrel;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ParagonOfFierceDefiance.class, ScrapyardMongrel.class, RuneclawBear.class, LightningStrike.class})
class ParagonOfFierceDefianceTest extends BaseCardTest {

    @Test
    @DisplayName("Other red creatures you control get +1/+1")
    void buffsOtherRedCreaturesYouControl() {
        Permanent paragon = addCreatureReady(player1, new ParagonOfFierceDefiance());
        Permanent giant = addCreatureReady(player1, new ScrapyardMongrel());

        assertThat(gqs.getEffectivePower(gd, paragon)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, paragon)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not buff nonred or opponent creatures")
    void onlyBuffsOwnRedCreatures() {
        addCreatureReady(player1, new ParagonOfFierceDefiance());
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        Permanent opponentGiant = addCreatureReady(player2, new ScrapyardMongrel());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentGiant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentGiant)).isEqualTo(3);
    }

    @Test
    @DisplayName("Activating grants haste to another red creature you control")
    void grantsHasteToAnotherRedCreature() {
        Permanent paragon = addCreatureReady(player1, new ParagonOfFierceDefiance());
        Permanent giant = addCreatureReady(player1, new ScrapyardMongrel());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, indexOf(player1, paragon), 0, giant.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, giant, Keyword.HASTE)).isTrue();
        assertThat(paragon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Haste wears off at end of turn")
    void hasteWearsOffAtEndOfTurn() {
        Permanent paragon = addCreatureReady(player1, new ParagonOfFierceDefiance());
        Permanent giant = addCreatureReady(player1, new ScrapyardMongrel());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, indexOf(player1, paragon), 0, giant.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, giant, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, giant, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target itself, a nonred creature, or an opponent's creature")
    void restrictsActivationTarget() {
        Permanent paragon = addCreatureReady(player1, new ParagonOfFierceDefiance());
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        Permanent opponentGiant = addCreatureReady(player2, new ScrapyardMongrel());
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, paragon), 0, paragon.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another red creature");
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, paragon), 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another red creature");
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, paragon), 0, opponentGiant.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another red creature");
    }

    @Test
    @DisplayName("Two Paragons boost each other and their bonuses stack")
    void multipleParagonsBoostEachOther() {
        Permanent first = addCreatureReady(player1, new ParagonOfFierceDefiance());
        Permanent second = addCreatureReady(player1, new ParagonOfFierceDefiance());
        Permanent mongrel = addCreatureReady(player1, new ScrapyardMongrel());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, mongrel)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, mongrel)).isEqualTo(5);
    }

    @Test
    @DisplayName("Can grant haste to another Paragon")
    void grantsHasteToAnotherParagon() {
        Permanent source = addCreatureReady(player1, new ParagonOfFierceDefiance());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ParagonOfFierceDefiance());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, indexOf(player1, source), 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, source, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("A summoning-sick Paragon cannot pay its tap cost")
    void cannotActivateWhileSummoningSick() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ParagonOfFierceDefiance());
        Permanent target = addCreatureReady(player1, new ScrapyardMongrel());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, source), 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("A creature no longer controlled by you is illegal on resolution")
    void rechecksTargetControllerOnResolution() {
        Permanent source = addCreatureReady(player1, new ParagonOfFierceDefiance());
        Permanent target = addCreatureReady(player1, new ScrapyardMongrel());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, indexOf(player1, source), 0, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing the source ends its anthem but not its activated ability")
    void abilityResolvesAfterSourceDies() {
        Permanent source = addCreatureReady(player1, new ParagonOfFierceDefiance());
        Permanent target = addCreatureReady(player1, new ScrapyardMongrel());
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, indexOf(player1, source), 0, target.getId());
        harness.castInstant(player1, 0, source.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
