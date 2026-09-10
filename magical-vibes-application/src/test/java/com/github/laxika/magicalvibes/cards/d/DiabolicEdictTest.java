package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FightingDrake;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.cards.w.Wasteland;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DiabolicEdict.class, FightingDrake.class, TrainedArmodon.class, Wasteland.class})
class DiabolicEdictTest extends BaseCardTest {

    @Test
    @DisplayName("Target player's lone creature is sacrificed")
    void loneCreatureIsSacrificed() {
        harness.addToBattlefield(player2, new TrainedArmodon());

        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertNotOnBattlefield(player2, "Trained Armodon");
        harness.assertInGraveyard(player2, "Trained Armodon");
    }

    @Test
    @DisplayName("Target player chooses which creature to sacrifice")
    void targetPlayerChooses() {
        harness.addToBattlefield(player2, new TrainedArmodon());
        Permanent drake = harness.addToBattlefieldAndReturn(player2, new FightingDrake());

        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.context()).isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);

        harness.handlePermanentChosen(player2, drake.getId());

        harness.assertNotOnBattlefield(player2, "Fighting Drake");
        harness.assertOnBattlefield(player2, "Trained Armodon");
        harness.assertInGraveyard(player2, "Fighting Drake");
    }

    @Test
    @DisplayName("Controller's own creatures are unaffected when an opponent is targeted")
    void controllerCreaturesUnaffected() {
        harness.addToBattlefield(player1, new TrainedArmodon());
        harness.addToBattlefield(player2, new FightingDrake());

        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertOnBattlefield(player1, "Trained Armodon");
        harness.assertNotOnBattlefield(player2, "Fighting Drake");
    }

    @Test
    @DisplayName("Only creatures are offered as sacrifice choices")
    void onlyCreaturesAreSacrificeChoices() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new TrainedArmodon());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new FightingDrake());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Wasteland());

        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(creature.getId(), otherCreature.getId());

        harness.handlePermanentChosen(player2, creature.getId());

        harness.assertInGraveyard(player2, "Trained Armodon");
        assertThat(harness.getGameData().playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getId())
                .containsExactlyInAnyOrder(otherCreature.getId(), land.getId());
    }

    @Test
    @DisplayName("Nothing happens when the target player controls no creatures")
    void noCreaturesNoSacrifice() {
        harness.setHand(player1, List.of(new DiabolicEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertInGraveyard(player1, "Diabolic Edict");
    }
}
