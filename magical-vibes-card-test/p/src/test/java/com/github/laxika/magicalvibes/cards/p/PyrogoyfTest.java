package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.Lhurgoyf;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Pyrogoyf.class, Lhurgoyf.class, Forest.class, Shock.class, GrizzlyBears.class, Conspiracy.class})
class PyrogoyfTest extends BaseCardTest {

    @Test
    @DisplayName("Pyrogoyf counts distinct card types in all graveyards")
    void countsDistinctCardTypesInAllGraveyards() {
        Permanent pyrogoyf = addCreatureReady(player1, new Pyrogoyf());
        harness.setGraveyard(player1, List.of(new Forest(), new Shock()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new Forest()));

        assertThat(gqs.getEffectivePower(gd, pyrogoyf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, pyrogoyf)).isEqualTo(4);
    }

    @Test
    @DisplayName("The entering Lhurgoyf deals damage equal to its own power")
    void lhurgoyfEntryDealsItsPowerToAnyTarget() {
        harness.addToBattlefield(player1, new Pyrogoyf());
        setThreeCardTypesInGraveyards();
        harness.castFromHand(player1, new Lhurgoyf(), "{2}{G}{G}");
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Lhurgoyf"))).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Pyrogoyf triggers for itself entering the battlefield")
    void selfEntryDealsItsPowerToAnyTarget() {
        setThreeCardTypesInGraveyards();
        harness.castFromHand(player1, new Pyrogoyf(), "{3}{R}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Pyrogoyf does not trigger for a non-Lhurgoyf creature")
    void doesNotTriggerForNonLhurgoyf() {
        harness.addToBattlefield(player1, new Pyrogoyf());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void selfEntryStillTriggersWhenConspiracyReplacesItsCreatureType() {
        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());
        setThreeCardTypesInGraveyards();

        harness.castFromHand(player1, new Pyrogoyf(), "{3}{R}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void damageUsesPowerAtResolutionAfterGraveyardsChange() {
        setThreeCardTypesInGraveyards();
        harness.castFromHand(player1, new Pyrogoyf(), "{3}{R}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setGraveyard(player2, List.of());

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void doesNotTriggerForOpponentsLhurgoyf() {
        harness.addToBattlefield(player1, new Pyrogoyf());
        setThreeCardTypesInGraveyards();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new Lhurgoyf(), "{2}{G}{G}");

        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void entryDamageCanDestroyTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        setThreeCardTypesInGraveyards();
        harness.castFromHand(player1, new Pyrogoyf(), "{3}{R}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, findPermanent(player2, "Grizzly Bears").getId());

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void emptyGraveyardsCauseNoEntryDamage() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.castFromHand(player1, new Pyrogoyf(), "{3}{R}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player2.getId());

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    private void setThreeCardTypesInGraveyards() {
        harness.setGraveyard(player1, List.of(new Forest(), new Shock()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
    }

}
