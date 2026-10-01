package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Squirrel;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheOddAcornGang.class, Squirrel.class, GrizzlyBears.class})
class TheOddAcornGangTest extends BaseCardTest {

    @Test
    @DisplayName("Squirrels you control can tap to boost a target Squirrel")
    void squirrelsGainBoostAbility() {
        Permanent squirrel = addCreatureReady(player1, new Squirrel());
        Permanent target = addCreatureReady(player1, new Squirrel());
        harness.addToBattlefield(player1, new TheOddAcornGang());

        int squirrelIndex = gd.playerBattlefields.get(player1.getId()).indexOf(squirrel);
        harness.activateAbility(player1, squirrelIndex, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(squirrel.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The boost ability cannot target a non-Squirrel creature")
    void boostAbilityRejectsNonSquirrelTarget() {
        Permanent squirrel = addCreatureReady(player1, new Squirrel());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new TheOddAcornGang());

        int squirrelIndex = gd.playerBattlefields.get(player1.getId()).indexOf(squirrel);
        assertThatThrownBy(() -> harness.activateAbility(player1, squirrelIndex, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The boost ability is sorcery speed")
    void boostAbilityIsSorcerySpeed() {
        Permanent squirrel = addCreatureReady(player1, new Squirrel());
        Permanent target = addCreatureReady(player1, new Squirrel());
        harness.addToBattlefield(player1, new TheOddAcornGang());
        harness.forceStep(TurnStep.UPKEEP);

        int squirrelIndex = gd.playerBattlefields.get(player1.getId()).indexOf(squirrel);
        assertThatThrownBy(() -> harness.activateAbility(player1, squirrelIndex, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("One or more Squirrels dealing combat damage draws one card")
    void squirrelsDealCombatDamageDrawsOneCard() {
        Permanent gang = addCreatureReady(player1, new TheOddAcornGang());
        Permanent squirrel = addCreatureReady(player1, new Squirrel());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(gang),
                gd.playerBattlefields.get(player1.getId()).indexOf(squirrel)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }
}
