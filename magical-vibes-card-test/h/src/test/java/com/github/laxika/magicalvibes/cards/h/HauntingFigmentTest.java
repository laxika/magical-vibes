package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HauntingFigment.class, DarkRitual.class, Divination.class, GrizzlyBears.class})
class HauntingFigmentTest extends BaseCardTest {

    @Test
    @DisplayName("Can be blocked before its controller casts an instant or sorcery")
    void canBeBlockedBeforeCastingInstantOrSorcery() {
        Permanent figment = addCreatureReady(player1, new HauntingFigment());

        assertThat(gqs.hasCantBeBlocked(gd, figment)).isFalse();
    }

    @Test
    @DisplayName("Can't be blocked after its controller casts an instant")
    void cantBeBlockedAfterCastingInstant() {
        Permanent figment = addCreatureReady(player1, new HauntingFigment());
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gqs.hasCantBeBlocked(gd, figment)).isTrue();
    }

    @Test
    @DisplayName("Can't be blocked after its controller casts a sorcery")
    void cantBeBlockedAfterCastingSorcery() {
        Permanent figment = addCreatureReady(player1, new HauntingFigment());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasCantBeBlocked(gd, figment)).isTrue();
    }

    @Test
    @DisplayName("A creature spell does not make it unblockable")
    void creatureSpellDoesNotEnableUnblockability() {
        Permanent figment = addCreatureReady(player1, new HauntingFigment());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasCantBeBlocked(gd, figment)).isFalse();
    }

    @Test
    @DisplayName("Casting an instant enables unblockability before the spell resolves")
    void instantOnStackEnablesUnblockability() {
        Permanent figment = addCreatureReady(player1, new HauntingFigment());
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.hasCantBeBlocked(gd, figment)).isTrue();
    }

    @Test
    @DisplayName("An opponent's instant does not enable unblockability")
    void opponentsInstantDoesNotEnableUnblockability() {
        Permanent figment = addCreatureReady(player1, new HauntingFigment());
        harness.setHand(player2, List.of(new DarkRitual()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.ensurePriority(player2);

        harness.castAndResolveInstant(player2, 0);

        assertThat(gqs.hasCantBeBlocked(gd, figment)).isFalse();
    }

    @Test
    @DisplayName("An instant cast before Figment enters still enables unblockability")
    void instantCastBeforeEnteringEnablesUnblockability() {
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0);

        Permanent figment = harness.enterBattlefieldAndReturn(player1, new HauntingFigment());

        assertThat(gqs.hasCantBeBlocked(gd, figment)).isTrue();
    }

    @Test
    @DisplayName("Unblockability expires when the turn changes")
    void unblockabilityExpiresOnNextTurn() {
        Permanent figment = addCreatureReady(player1, new HauntingFigment());
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0);
        assertThat(gqs.hasCantBeBlocked(gd, figment)).isTrue();
        harness.setLibrary(player2, List.of(new HauntingFigment()));

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasCantBeBlocked(gd, figment)).isFalse();
    }

    @Test
    @DisplayName("Only Figment becomes unblockable and vigilance leaves it untapped when attacking")
    void unblockabilityIsSelfOnlyAndAttackDoesNotTap() {
        Permanent figment = addCreatureReady(player1, new HauntingFigment());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        assertThat(bls.canBlockAttacker(gd, blocker, figment,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(figment.isTapped()).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, otherCreature)).isFalse();
        assertThat(bls.canBlockAttacker(gd, blocker, figment,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }
}
