package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.FangFearlessLCie;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagnarokDivineDeliverance;
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

@CardUsed({VanilleCheerfulLCie.class, RagnarokDivineDeliverance.class, FangFearlessLCie.class,
        Forest.class, GrizzlyBears.class})
class VanilleCheerfulLCieTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by milling two cards and returning a permanent card to hand")
    void millsAndReturnsPermanentToHand() {
        Forest milledFirst = new Forest();
        Forest milledSecond = new Forest();
        GrizzlyBears returned = new GrizzlyBears();
        harness.setLibrary(player1, List.of(milledFirst, milledSecond));
        harness.setGraveyard(player1, List.of(returned));
        harness.setHand(player1, List.of(new VanilleCheerfulLCie()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.mandatory()).isTrue();
        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(returned));

        assertThat(gd.playerHands.get(player1.getId())).contains(returned);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(milledFirst, milledSecond);
    }

    @Test
    @DisplayName("Melds with Fang after paying {3}{B}{G}")
    void meldsWithFang() {
        harness.setLibrary(player1, List.of());
        Permanent vanille = harness.addToBattlefieldAndReturn(player1, new VanilleCheerfulLCie());
        Permanent fang = harness.addToBattlefieldAndReturn(player1, new FangFearlessLCie());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent == vanille || permanent == fang);
        Permanent ragnarok = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof RagnarokDivineDeliverance)
                .findFirst().orElseThrow();
        assertThat(ragnarok.getMeldComponentCards()).hasSize(2);
        assertThat(ragnarok.isTapped()).isFalse();
    }

    @Test
    @DisplayName("On death, destroys a permanent and returns a nonlegendary permanent card")
    void deathAbilityDestroysAndReturns() {
        Forest returned = new Forest();
        harness.setGraveyard(player1, List.of(returned));
        Permanent ragnarok = harness.addToBattlefieldAndReturn(player1, new RagnarokDivineDeliverance());
        Permanent destroyed = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, ragnarok));
        harness.runStateBasedActions();
        assertThat(gd.pendingInteractions).isNotEmpty();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, destroyed.getId());
        PendingInteraction.MultiGraveyardChoice graveyardChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(graveyardChoice).isNotNull();
        assertThat(graveyardChoice.validCardIds()).containsExactly(returned.getId());
        harness.handleMultipleCardsChosen(player1, List.of(returned.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Forest");
    }
}
