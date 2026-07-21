package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ArchangelAvacynTest extends BaseCardTest {

    @Test
    @DisplayName("ETB grants indestructible to creatures you control until end of turn")
    void etbGrantsIndestructible() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castAvacyn();
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB

        Permanent avacyn = findAvacyn();
        assertThat(avacyn.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
        assertThat(bears.getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("ETB indestructible wears off at end of turn")
    void etbIndestructibleWearsOff() {
        castAvacyn();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findAvacyn().getGrantedKeywords()).contains(Keyword.INDESTRUCTIBLE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findAvacyn().getGrantedKeywords()).doesNotContain(Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("Non-Angel death schedules transform at next upkeep")
    void nonAngelDeathTransformsAtNextUpkeep() {
        Permanent avacyn = harness.addToBattlefieldAndReturn(player1, new ArchangelAvacyn());
        harness.addToBattlefield(player1, new GrizzlyBears());

        killWithShock(player2, player1, "Grizzly Bears");
        harness.passBothPriorities(); // resolve delayed-register trigger

        assertThat(avacyn.isTransformed()).isFalse();

        advanceToUpkeep(player1);
        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities(); // resolve delayed transform

        assertThat(avacyn.isTransformed()).isTrue();
        assertThat(avacyn.getCard().getName()).isEqualTo("Avacyn, the Purifier");
    }

    @Test
    @DisplayName("Angel death does not schedule transform")
    void angelDeathDoesNotTransform() {
        Permanent avacyn = harness.addToBattlefieldAndReturn(player1, new ArchangelAvacyn());
        harness.addToBattlefield(player1, new AngelicPage());

        killWithShock(player2, player1, "Angelic Page");
        assertThat(gd.stack).isEmpty();

        advanceToUpkeep(player1);
        assertThat(avacyn.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Transform deals 3 to each other creature and each opponent, not self or controller")
    void transformDamagesOthersAndOpponents() {
        Permanent avacyn = harness.addToBattlefieldAndReturn(player1, new ArchangelAvacyn());
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        killWithShock(player2, player1, fodder.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // transform
        harness.passBothPriorities(); // transform damage

        assertThat(avacyn.isTransformed()).isTrue();
        assertThat(avacyn.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    private void castAvacyn() {
        harness.setHand(player1, List.of(new ArchangelAvacyn()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }

    private Permanent findAvacyn() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Archangel Avacyn"))
                .findFirst()
                .orElseThrow();
    }

    private void killWithShock(Player caster, Player targetController, String targetName) {
        killWithShock(caster, targetController, harness.getPermanentId(targetController, targetName));
    }

    private void killWithShock(Player caster, Player targetController, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
