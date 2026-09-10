package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TigerTribeHunter.class, GrizzlyBears.class, HillGiant.class})
class TigerTribeHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Pack tactics sacrifices another creature and deals its power to a creature")
    void sacrificesAnotherCreatureAndDealsItsPowerToCreature() {
        addCreatureReady(player1, new TigerTribeHunter());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent sacrificed = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new HillGiant());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrificed);
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Pack tactics does not trigger below total attacking power six")
    void doesNotTriggerBelowThreshold() {
        addCreatureReady(player1, new TigerTribeHunter());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The reflexive trigger offers only creature targets")
    void offersOnlyCreatureTargets() {
        addCreatureReady(player1, new TigerTribeHunter());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent sacrificed = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new HillGiant());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrificed.getId());

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).contains(target.getId());
        assertThat(choice.validPlayerIds()).isEmpty();
    }
}
