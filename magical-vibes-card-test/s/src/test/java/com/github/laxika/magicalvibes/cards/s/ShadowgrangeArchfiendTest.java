package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.r.RavensCrime;
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

@CardUsed({ShadowgrangeArchfiend.class, GrizzlyBears.class, HillGiant.class, RavensCrime.class})
class ShadowgrangeArchfiendTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by making each opponent sacrifice their greatest-power creature and gains that much life")
    void sacrificesGreatestPowerCreatureAndGainsLife() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new ShadowgrangeArchfiend()));
        addNormalMana(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Lets an opponent choose among tied greatest-power creatures")
    void letsOpponentChooseTiedGreatestPowerCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent firstGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent secondGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new ShadowgrangeArchfiend()));
        addNormalMana(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstGiant.getId(), secondGiant.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(firstGiant.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .contains(secondGiant.getId());
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Madness costs {2}{B} and 8 life")
    void madnessPaysManaAndLife() {
        ShadowgrangeArchfiend archfiend = discardViaRavensCrime();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(archfiend.getId()));
    }

    private void addNormalMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.COLORLESS, 6);
    }

    private ShadowgrangeArchfiend discardViaRavensCrime() {
        ShadowgrangeArchfiend archfiend = new ShadowgrangeArchfiend();
        harness.setHand(player1, List.of(archfiend));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        return archfiend;
    }
}
