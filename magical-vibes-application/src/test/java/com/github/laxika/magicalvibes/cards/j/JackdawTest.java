package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Jackdaw.class, Forest.class, GrizzlyBears.class, Memnite.class})
class JackdawTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage may discard the hand and draw for each artifact")
    void acceptsCombatDamageTrigger() {
        Card discardedForest = new Forest();
        Card discardedBear = new GrizzlyBears();
        Card drawnForest = new Forest();
        Card drawnBear = new GrizzlyBears();
        harness.setHand(player1, List.of(discardedForest, discardedBear));
        harness.setLibrary(player1, List.of(drawnForest, drawnBear));
        addReadyJackdawWithCrew();

        resolveCombatDamage();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnForest, drawnBear);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(discardedForest, discardedBear);
    }

    @Test
    @DisplayName("Combat damage may be declined")
    void declinesCombatDamageTrigger() {
        Card keptForest = new Forest();
        Card keptBear = new GrizzlyBears();
        Card libraryCard = new Forest();
        harness.setHand(player1, List.of(keptForest, keptBear));
        harness.setLibrary(player1, List.of(libraryCard));
        addReadyJackdawWithCrew();

        resolveCombatDamage();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keptForest, keptBear);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(keptForest, keptBear);
    }

    @Test
    @DisplayName("Crew 3 animates Jackdaw and taps the crew")
    void crewAnimatesJackdaw() {
        Permanent jackdaw = harness.addToBattlefieldAndReturn(player1, new Jackdaw());
        jackdaw.setSummoningSick(false);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent memnite = harness.addToBattlefieldAndReturn(player1, new Memnite());
        bears.setSummoningSick(false);
        memnite.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, jackdaw)).isTrue();
        assertThat(bears.isTapped()).isTrue();
        assertThat(memnite.isTapped()).isTrue();
    }

    private void addReadyJackdawWithCrew() {
        Permanent jackdaw = harness.addToBattlefieldAndReturn(player1, new Jackdaw());
        jackdaw.setSummoningSick(false);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent memnite = harness.addToBattlefieldAndReturn(player1, new Memnite());
        bears.setSummoningSick(false);
        memnite.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        jackdaw.setAttacking(true);
    }

    private void resolveCombatDamage() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
