package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HowlsquadHeavyTest extends BaseCardTest {

    @Test
    void otherGoblinsYouControlHaveHaste() {
        harness.addToBattlefield(player1, new HowlsquadHeavy());
        Permanent piker = harness.addToBattlefieldAndReturn(player1, new GoblinPiker());

        assertThat(gqs.hasKeyword(gd, piker, Keyword.HASTE)).isTrue();
    }

    @Test
    void createsTokenThatMustAttackThisCombat() {
        harness.addToBattlefield(player1, new HowlsquadHeavy());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(gqs.hasKeyword(gd, tokens.getFirst(), Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    void maxSpeedAbilityAddsRedManaForEachGoblinYouControl() {
        Permanent heavy = harness.addToBattlefieldAndReturn(player1, new HowlsquadHeavy());
        harness.addToBattlefield(player1, new GoblinPiker());
        harness.addToBattlefield(player1, new GoblinPiker());
        heavy.setSummoningSick(false);
        gd.playerSpeeds.put(player1.getId(), 4);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
        assertThat(heavy.isTapped()).isTrue();
    }

    @Test
    void maxSpeedAbilityRequiresMaxSpeed() {
        Permanent heavy = harness.addToBattlefieldAndReturn(player1, new HowlsquadHeavy());
        heavy.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("max speed");
        assertThat(heavy.isTapped()).isFalse();
    }
}
