package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BroadsideBombardiers.class, GrizzlyBears.class, Spellbook.class})
class BroadsideBombardiersTest extends BaseCardTest {

    @Test
    @DisplayName("Boast sacrifices another creature and deals 2 plus its mana value to any target")
    void boastSacrificesCreatureAndDealsManaValueDamage() {
        Permanent bombardiers = addReady(new BroadsideBombardiers());
        Permanent sacrificed = addReady(new GrizzlyBears());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, battlefieldIndex(bombardiers), null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 4);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Boast can sacrifice an artifact and excludes this creature")
    void boastSacrificesArtifactAndExcludesSource() {
        Permanent bombardiers = addReady(new BroadsideBombardiers());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, battlefieldIndex(bombardiers), null, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("Boast requires an attack and can be activated only once each turn")
    void boastRequiresAttackAndIsOncePerTurn() {
        Permanent bombardiers = addReady(new BroadsideBombardiers());
        addReady(new GrizzlyBears());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(bombardiers), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked this turn");

        declareAttackers(List.of(0));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, battlefieldIndex(bombardiers), null, player2.getId());
        harness.passBothPriorities();

        addReady(new GrizzlyBears());
        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(bombardiers), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("once each turn");
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
