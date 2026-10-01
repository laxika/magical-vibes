package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AzoriusChancery;
import com.github.laxika.magicalvibes.cards.t.TrygonPredator;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SandstormEidolon.class, AzoriusChancery.class, SilkwingScout.class, TrygonPredator.class})
class SandstormEidolonTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Sandstorm Eidolon makes a target creature unable to block this turn")
    void sacrificeAbilityPreventsBlocking() {
        addCreatureReady(player1, new SandstormEidolon());
        Permanent target = addCreatureReady(player2, new SilkwingScout());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sandstorm Eidolon");
        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Casting a multicolored spell may return Sandstorm Eidolon from the graveyard")
    void multicoloredSpellReturnsEidolonToHand() {
        SandstormEidolon eidolon = new SandstormEidolon();
        harness.setGraveyard(player1, List.of(eidolon));
        harness.castFromHand(player1, new TrygonPredator(), "{1}{G}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(eidolon);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(eidolon);
    }

    @Test
    @DisplayName("Declining the multicolored spell trigger keeps Sandstorm Eidolon in the graveyard")
    void decliningReturnKeepsEidolonInGraveyard() {
        SandstormEidolon eidolon = new SandstormEidolon();
        harness.setGraveyard(player1, List.of(eidolon));
        harness.castFromHand(player1, new TrygonPredator(), "{1}{G}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(eidolon);
    }

    @Test
    @DisplayName("A monocolored spell does not trigger Sandstorm Eidolon's graveyard ability")
    void monocoloredSpellDoesNotTriggerReturn() {
        SandstormEidolon eidolon = new SandstormEidolon();
        harness.setGraveyard(player1, List.of(eidolon));
        harness.castFromHand(player1, new SilkwingScout(), "{2}{U}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(eidolon);
    }

    @Test
    @DisplayName("An opponent's multicolored spell does not trigger Sandstorm Eidolon")
    void opponentsMulticoloredSpellDoesNotTriggerReturn() {
        SandstormEidolon eidolon = new SandstormEidolon();
        harness.setGraveyard(player1, List.of(eidolon));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new TrygonPredator(), "{1}{G}{U}");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(eidolon);
    }

    @Test
    @DisplayName("The activated ability cannot target a noncreature permanent")
    void activatedAbilityCannotTargetNoncreature() {
        Permanent eidolon = addCreatureReady(player1, new SandstormEidolon());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new AzoriusChancery());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(eidolon);
    }
}
