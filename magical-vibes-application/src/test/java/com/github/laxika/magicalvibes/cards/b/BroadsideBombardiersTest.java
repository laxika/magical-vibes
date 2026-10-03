package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.s.SatyaAetherfluxGenius;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BroadsideBombardiers.class, GrizzlyBears.class, Spellbook.class, SatyaAetherfluxGenius.class})
class BroadsideBombardiersTest extends BaseCardTest {

    @Test
    @DisplayName("Boast sacrifices another creature and deals 2 plus its mana value to any target")
    void boastSacrificesCreatureAndDealsManaValueDamage() {
        Permanent bombardiers = addCreatureReady(player1, new BroadsideBombardiers());
        addCreatureReady(player1, new GrizzlyBears());
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
        Permanent bombardiers = addCreatureReady(player1, new BroadsideBombardiers());
        harness.addToBattlefield(player1, new Spellbook());
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
        Permanent bombardiers = addCreatureReady(player1, new BroadsideBombardiers());
        addCreatureReady(player1, new GrizzlyBears());
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

        addCreatureReady(player1, new GrizzlyBears());
        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(bombardiers), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("once each turn");
    }

    @Test
    @DisplayName("Boast cannot sacrifice its source or an opponent's permanent")
    void boastCannotSacrificeSourceOrOpponentsPermanent() {
        Permanent bombardiers = addCreatureReady(player1, new BroadsideBombardiers());
        harness.addToBattlefield(player2, new GrizzlyBears());
        declareAttackers(List.of(0));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(bombardiers), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
        harness.assertOnBattlefield(player1, "Broadside Bombardiers");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Boast uses the selected artifact's zero mana value when several sacrifices are available")
    void boastUsesSelectedSacrificeManaValue() {
        Permanent bombardiers = addCreatureReady(player1, new BroadsideBombardiers());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        declareAttackers(List.of(0));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, battlefieldIndex(bombardiers), null, player2.getId());
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Broadside Bombardiers");
    }

    @Test
    @DisplayName("Boast can kill a creature before combat damage")
    void boastCanTargetCreatureDuringCombat() {
        Permanent bombardiers = addCreatureReady(player1, new BroadsideBombardiers());
        harness.addToBattlefield(player1, new Spellbook());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        harness.activateAbility(player1, battlefieldIndex(bombardiers), null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A Bombardiers copy entering attacking cannot boast without being declared as an attacker")
    void enteringAttackingDoesNotEnableBoast() {
        addCreatureReady(player1, new SatyaAetherfluxGenius());
        Permanent original = addCreatureReady(player1, new BroadsideBombardiers());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, original.getId());
            resolveAllTriggers();
        });
        Permanent token = findPermanents(player1, "Broadside Bombardiers").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(token), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked this turn");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(original, token);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
