package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchangelAvacyn.class, GrizzlyBears.class, Shock.class, AngelicPage.class})
class ArchangelAvacynTest extends BaseCardTest {

    @Test
    @DisplayName("ETB grants indestructible to creatures you control until end of turn")
    void etbGrantsIndestructible() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castAvacyn();
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB

        Permanent avacyn = findPermanent(player1, "Archangel Avacyn");
        assertThat(PermanentGrants.keywordsOf(gd, avacyn)).contains(Keyword.INDESTRUCTIBLE);
        assertThat(PermanentGrants.keywordsOf(gd, bears)).contains(Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("ETB indestructible wears off at end of turn")
    void etbIndestructibleWearsOff() {
        castAvacyn();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(PermanentGrants.keywordsOf(gd, findPermanent(player1, "Archangel Avacyn"))).contains(Keyword.INDESTRUCTIBLE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.keywordsOf(gd, findPermanent(player1, "Archangel Avacyn"))).doesNotContain(Keyword.INDESTRUCTIBLE);
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

        killWithShock(player2, fodder.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // transform
        harness.passBothPriorities(); // transform damage

        assertThat(avacyn.isTransformed()).isTrue();
        assertThat(avacyn.getMarkedDamage()).isZero();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Flash lets it be cast during an opponent's upkeep")
    void flashCanBeCastDuringOpponentsUpkeep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        castAvacyn();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Archangel Avacyn");
        assertThat(PermanentGrants.keywordsOf(gd, findPermanent(player1, "Archangel Avacyn")))
                .contains(Keyword.INDESTRUCTIBLE);
    }

    @Test
    @DisplayName("Indestructible protects only the creatures there as the ability resolves")
    void indestructibleProtectsOnlyCreaturesPresentWhenAbilityResolves() {
        Permanent protectedBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castAvacyn();
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent laterBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        killWithShock(player2, protectedBears.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(protectedBears);
        killWithShock(player2, laterBears.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(laterBears);
        killWithShock(player1, opposingBears.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingBears);
    }

    @Test
    @DisplayName("An opponent's creature dying schedules no transform")
    void opponentsCreatureDeathDoesNotScheduleTransform() {
        Permanent avacyn = harness.addToBattlefieldAndReturn(player1, new ArchangelAvacyn());
        harness.addToBattlefield(player2, new GrizzlyBears());

        killWithShock(player1, player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        advanceToUpkeep(player2);

        assertThat(avacyn.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Several deaths transform it only once, at the opponent's upkeep")
    void multipleDeathsTransformOnlyOnceDuringOpponentsUpkeep() {
        Permanent avacyn = harness.addToBattlefieldAndReturn(player1, new ArchangelAvacyn());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player2, 20);

        killWithShock(player2, first.getId());
        harness.passBothPriorities();
        killWithShock(player2, second.getId());
        harness.passBothPriorities();
        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(avacyn.isTransformed()).isTrue();
        harness.assertLife(player2, 17);
        assertThat(avacyn.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The delayed transform does nothing once Avacyn has died")
    void delayedTransformDoesNothingAfterAvacynDies() {
        Permanent avacyn = harness.addToBattlefieldAndReturn(player1, new ArchangelAvacyn());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player2, 20);

        killWithShock(player2, player1, "Grizzly Bears");
        harness.passBothPriorities();
        killWithShock(player2, avacyn.getId());
        killWithShock(player2, avacyn.getId());
        harness.assertInGraveyard(player1, "Archangel Avacyn");

        advanceToUpkeep(player2);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Avacyn, the Purifier");
        harness.assertLife(player2, 20);
    }

    private void castAvacyn() {
        harness.setHand(player1, List.of(new ArchangelAvacyn()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }

    private void killWithShock(Player caster, Player targetController, String targetName) {
        killWithShock(caster, harness.getPermanentId(targetController, targetName));
    }

    private void killWithShock(Player caster, UUID targetId) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castAndResolveInstant(caster, 0, targetId);
    }
}
