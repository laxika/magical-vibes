package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HaloForager.class, LightningBolt.class, Murder.class, Shock.class})
class HaloForagerTest extends BaseCardTest {

    @Test
    void paysXThenCastsMatchingSpellFromOwnGraveyardForFree() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        castHaloForager();

        harness.handleXValueChosen(player1, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertNotInGraveyard(player1, "Shock");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName()).contains("Shock");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void canTargetAnInstantInAnOpponentsGraveyard() {
        LightningBolt bolt = new LightningBolt();
        harness.setGraveyard(player2, List.of(bolt));
        castHaloForager();

        harness.handleXValueChosen(player1, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        harness.assertNotInGraveyard(player2, "Lightning Bolt");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName()).contains("Lightning Bolt");
    }

    @Test
    void choosesAmongMatchingCardsAfterPayingX() {
        Shock shock = new Shock();
        LightningBolt bolt = new LightningBolt();
        harness.setGraveyard(player1, List.of(shock));
        harness.setGraveyard(player2, List.of(bolt));
        castHaloForager();

        harness.handleXValueChosen(player1, 1);

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0, 1);
        harness.handleGraveyardCardChosen(player1, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
        harness.assertNotInGraveyard(player2, "Lightning Bolt");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName()).contains("Lightning Bolt");
    }

    @Test
    void decliningTheCastLeavesTheTargetInItsGraveyard() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        castHaloForager();

        harness.handleXValueChosen(player1, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName()).doesNotContain("Shock");
    }

    @Test
    void noMatchingManaValueCreatesNoReflexiveTrigger() {
        harness.setGraveyard(player1, List.of(new Murder()));
        castHaloForager();

        harness.handleXValueChosen(player1, 1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Murder");
    }

    private void castHaloForager() {
        harness.setHand(player1, List.of(new HaloForager()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
