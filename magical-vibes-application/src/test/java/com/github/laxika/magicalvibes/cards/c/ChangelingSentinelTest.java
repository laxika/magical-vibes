package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FieldMarshal;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChangelingSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Changeling gets boost from Field Marshal (Soldier lord) due to being every creature type")
    void changelingGetsSubtypeBoost() {
        harness.addToBattlefield(player1, new FieldMarshal());
        harness.addToBattlefield(player1, new ChangelingSentinel());

        GameData gd = harness.getGameData();
        Permanent changeling = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Changeling Sentinel"))
                .findFirst()
                .orElseThrow();

        assertThat(gqs.getEffectivePower(gd, changeling)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, changeling)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, changeling, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Vigilance: Changeling Sentinel does not tap when declared as attacker")
    void vigilancePreventsTapWhenAttacking() {
        Permanent sentinel = new Permanent(new ChangelingSentinel());
        sentinel.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(sentinel);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(sentinel.isTapped()).isFalse();
    }
}
