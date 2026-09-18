package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TesharAncestorsApostle;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WhatMustBeDone.class, FountainOfYouth.class, Forest.class, GloriousAnthem.class,
        GrizzlyBears.class, TesharAncestorsApostle.class})
class WhatMustBeDoneTest extends BaseCardTest {

    @Test
    @DisplayName("Let the World Burn destroys all artifacts and creatures")
    void letTheWorldBurnDestroysArtifactsAndCreatures() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.addToBattlefield(player2, new Forest());

        cast(0, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Glorious Anthem", "Forest");
    }

    @Test
    @DisplayName("Release Juno returns a historic permanent and adds counters to a creature")
    void releaseJunoReturnsHistoricCreatureWithCounters() {
        Card historicCreature = new TesharAncestorsApostle();
        harness.setGraveyard(player1, List.of(historicCreature));

        cast(1, List.of(historicCreature.getId()));

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(historicCreature.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Teshar, Ancestor's Apostle");
    }

    @Test
    @DisplayName("Release Juno returns a historic artifact without creature counters")
    void releaseJunoReturnsHistoricArtifactWithoutCounters() {
        Card historicArtifact = new FountainOfYouth();
        harness.setGraveyard(player1, List.of(historicArtifact));

        cast(1, List.of(historicArtifact.getId()));

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(historicArtifact.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Release Juno cannot target a nonhistoric card")
    void releaseJunoRejectsNonhistoricCard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        prepareCard();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int mode, List<java.util.UUID> targetIds) {
        prepareCard();
        if (targetIds.isEmpty()) {
            harness.castSorcery(player1, 0, mode);
        } else {
            harness.castSorcery(player1, 0, mode, targetIds.getFirst());
        }
        harness.passBothPriorities();
    }

    private void prepareCard() {
        harness.setHand(player1, List.of(new WhatMustBeDone()));
        harness.addMana(player1, ManaColor.WHITE, 5);
    }
}
