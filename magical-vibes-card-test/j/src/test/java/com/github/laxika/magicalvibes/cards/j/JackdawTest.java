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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        Permanent jackdaw = addCreatureReady(player1, new Jackdaw());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent memnite = addCreatureReady(player1, new Memnite());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, jackdaw)).isTrue();
        assertThat(bears.isTapped()).isTrue();
        assertThat(memnite.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An empty hand can be discarded to draw for each artifact")
    void drawsWithEmptyHand() {
        Card first = new Forest();
        Card second = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second));
        addReadyJackdawWithCrew();

        resolveCombatDamage();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Opposing artifacts do not increase the number of cards drawn")
    void excludesOpponentsArtifacts() {
        Card discarded = new Forest();
        Card first = new Forest();
        Card second = new Forest();
        Card remaining = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(first, second, remaining));
        addReadyJackdawWithCrew();
        harness.addToBattlefield(player2, new Memnite());

        resolveCombatDamage();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
    }

    @Test
    @DisplayName("Artifact count is determined when the trigger resolves")
    void countsArtifactsAtResolution() {
        Card discarded = new Forest();
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(first, second, third));
        addReadyJackdawWithCrew();

        resolveCombat();
        harness.addToBattlefield(player1, new Memnite());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
    }

    @Test
    @DisplayName("Summoning-sick creatures can pay crew 3")
    void summoningSickCreaturesCanCrew() {
        Permanent jackdaw = harness.addToBattlefieldAndReturn(player1, new Jackdaw());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent memnite = harness.addToBattlefieldAndReturn(player1, new Memnite());
        bears.setSummoningSick(true);
        memnite.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, jackdaw)).isTrue();
        assertThat(bears.isTapped()).isTrue();
        assertThat(memnite.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Crew 3 cannot be paid with only two power")
    void rejectsInsufficientCrewPower() {
        Permanent jackdaw = harness.addToBattlefieldAndReturn(player1, new Jackdaw());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gqs.isCreature(gd, jackdaw)).isFalse();
        assertThat(bears.isTapped()).isFalse();
    }

    private void addReadyJackdawWithCrew() {
        Permanent jackdaw = addCreatureReady(player1, new Jackdaw());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new Memnite());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        jackdaw.setAttacking(true);
    }

    private void resolveCombatDamage() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
