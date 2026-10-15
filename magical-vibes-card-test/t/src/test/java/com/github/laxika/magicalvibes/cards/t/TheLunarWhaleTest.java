package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MidnightCrusaderShuttle;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.s.ScorpionSentinel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheLunarWhale.class, Forest.class, GrizzlyBears.class, Opt.class,
        ScorpionSentinel.class, MidnightCrusaderShuttle.class})
class TheLunarWhaleTest extends BaseCardTest {

    @Test
    void cannotPlayFromTopBeforeItAttacks() {
        addWhaleReady();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    void attackingLetsControllerPlayLandAndCastSpellFromTop() {
        Permanent whale = addWhaleReady();
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, whale)).isTrue();
        assertThat(crew.isTapped()).isTrue();

        Forest forest = new Forest();
        Opt opt = new Opt();
        harness.setLibrary(player1, List.of(forest, opt));

        declareAttackers(List.of(0));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromLibraryTop(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Opt");
    }

    private Permanent addWhaleReady() {
        return addCreatureReady(player1, new TheLunarWhale());
    }

    @Test
    void topCardIsVisibleOnlyToControllerBeforeAttackingDuringOpponentsTurn() {
        addWhaleReady();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[{")
                        && message.contains("Forest"));
        assertThat(harness.getConn2().getSentMessages())
                .anyMatch(message -> message.contains("\"revealedLibraryTopCards\":[[],[]]"));
    }

    @Test
    void cannotCastSpellFromTopBeforeAttackingEvenWithEnoughMana() {
        addWhaleReady();
        ScorpionSentinel sentinel = new ScorpionSentinel();
        harness.setLibrary(player1, List.of(sentinel));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sentinel);
    }

    @Test
    void attackingDoesNotAllowAnExtraLandPlay() {
        crewAndAttack();
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castFromLibraryTop(player1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    void spellsFromTopStillRequireManaAndNormalTiming() {
        crewAndAttack();
        ScorpionSentinel sentinel = new ScorpionSentinel();
        harness.setLibrary(player1, List.of(sentinel));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castAndResolveFromLibraryTop(player1);
        harness.assertOnBattlefield(player1, "Scorpion Sentinel");
    }

    @Test
    void permissionExpiresAtNextTurn() {
        Permanent whale = crewAndAttack();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(whale.isAttackedThisTurn()).isFalse();
        assertThat(gqs.isCreature(gd, whale)).isFalse();
        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void summoningSickCreatureCanCrewWithoutGrantingLibraryPermission() {
        Permanent whale = addWhaleReady();
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new ScorpionSentinel());
        crew.setSummoningSick(true);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, whale)).isTrue();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void becomingAttackingWithoutBeingDeclaredDoesNotGrantLibraryPermission() {
        addCreatureReady(player1, new MidnightCrusaderShuttle());
        addCreatureReady(player1, new ScorpionSentinel());
        addCreatureReady(player1, new ScorpionSentinel());
        Permanent whale = addCreatureReady(player2, new TheLunarWhale());
        harness.addToBattlefield(player2, new ScorpionSentinel());
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.handlePermanentChosen(player1, whale.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(whale);
        assertThat(whale.isAttacking()).isTrue();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent crewAndAttack() {
        Permanent whale = addWhaleReady();
        addCreatureReady(player1, new ScorpionSentinel());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        return whale;
    }
}
