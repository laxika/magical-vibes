package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArabaMothrider;
import com.github.laxika.magicalvibes.cards.i.InnerChamberGuard;
import com.github.laxika.magicalvibes.cards.s.SpiralingEmbers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MichikoKondaTruthSeeker.class, SpiralingEmbers.class, InnerChamberGuard.class,
        MirenTheMoaningWell.class, ArabaMothrider.class})
class MichikoKondaTruthSeekerTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's noncombat damage makes that opponent sacrifice a permanent")
    void opponentSpellDamageCausesControllerToSacrifice() {
        harness.addToBattlefield(player1, new MichikoKondaTruthSeeker());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new MirenTheMoaningWell());
        Permanent guard = harness.addToBattlefieldAndReturn(player2, new InnerChamberGuard());
        harness.setHand(player2, List.of(
                new SpiralingEmbers(), new SpiralingEmbers(), new SpiralingEmbers(), new SpiralingEmbers()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player2, 0, 0, player1.getId());
        resolveAllTriggers();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactly(land.getId(), guard.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(land.getId()));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .containsExactly(guard.getId());
    }

    @Test
    @DisplayName("Damage from a source you control does not trigger Michiko Konda")
    void ownSourceDamageDoesNotTrigger() {
        harness.addToBattlefield(player1, new MichikoKondaTruthSeeker());
        harness.addToBattlefield(player1, new InnerChamberGuard());
        harness.setHand(player1, List.of(
                new SpiralingEmbers(), new SpiralingEmbers(), new SpiralingEmbers(), new SpiralingEmbers()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castAndResolveSorcery(player1, 0, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent's combat damage makes that opponent sacrifice a permanent")
    void opponentCombatDamageCausesControllerToSacrifice() {
        harness.addToBattlefield(player1, new MichikoKondaTruthSeeker());
        Permanent attacker = addCreatureReady(player2, new ArabaMothrider());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new MirenTheMoaningWell());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);
        resolveAllTriggers();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactly(attacker.getId(), land.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(land.getId()));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(Permanent::getId)
                .containsExactly(attacker.getId());
    }
}
