package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GarruksPackleader;
import com.github.laxika.magicalvibes.cards.h.HistoryOfBenalia;
import com.github.laxika.magicalvibes.cards.o.OnSerrasWings;
import com.github.laxika.magicalvibes.cards.t.TesharAncestorsApostle;
import com.github.laxika.magicalvibes.cards.u.UrzasRuinousBlast;
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
        GrizzlyBears.class, TesharAncestorsApostle.class, GarruksPackleader.class,
        HistoryOfBenalia.class, OnSerrasWings.class, UrzasRuinousBlast.class})
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

        Permanent returned = gqs.findPermanentById(gd,
                harness.getPermanentId(player1, "Teshar, Ancestor's Apostle"));
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Teshar, Ancestor's Apostle");
    }

    @Test
    @DisplayName("Release Juno returns a historic artifact without creature counters")
    void releaseJunoReturnsHistoricArtifactWithoutCounters() {
        Card historicArtifact = new FountainOfYouth();
        harness.setGraveyard(player1, List.of(historicArtifact));

        cast(1, List.of(historicArtifact.getId()));

        Permanent returned = gqs.findPermanentById(gd,
                harness.getPermanentId(player1, "Fountain of Youth"));
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

    @Test
    void releaseJunoCountersArePresentForEntryTriggers() {
        harness.addToBattlefield(player1, new GarruksPackleader());
        harness.setLibrary(player1, List.of(new Forest()));
        Card creature = new TesharAncestorsApostle();
        harness.setGraveyard(player1, List.of(creature));

        cast(1, List.of(creature.getId()));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void releaseJunoReturnsLegendaryAuraAttachedToChosenCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        java.util.UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        Card aura = new OnSerrasWings();
        harness.setGraveyard(player1, List.of(aura));

        cast(1, List.of(aura.getId()));
        harness.handlePermanentChosen(player1, creatureId);

        harness.assertOnBattlefield(player1, "On Serra's Wings");
        harness.assertNotInGraveyard(player1, "On Serra's Wings");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(aura.getId());
                    assertThat(permanent.getAttachedTo()).isEqualTo(creatureId);
                });
    }

    @Test
    void releaseJunoReturnsNonlegendarySagaWithoutCreatureCounters() {
        Card saga = new HistoryOfBenalia();
        harness.setGraveyard(player1, List.of(saga));

        cast(1, List.of(saga.getId()));

        harness.assertOnBattlefield(player1, "History of Benalia");
        harness.assertNotInGraveyard(player1, "History of Benalia");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(saga.getId());
                    assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
                });
    }

    @Test
    void releaseJunoRejectsHistoricNonpermanent() {
        Card sorcery = new UrzasRuinousBlast();
        harness.setGraveyard(player1, List.of(sorcery));
        prepareCard();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, sorcery.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void releaseJunoRejectsOpponentsGraveyard() {
        Card artifact = new FountainOfYouth();
        harness.setGraveyard(player2, List.of(artifact));
        prepareCard();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void releaseJunoDoesNotReturnTargetRemovedBeforeResolution() {
        Card artifact = new FountainOfYouth();
        harness.setGraveyard(player1, List.of(artifact));
        prepareCard();
        harness.castSorcery(player1, 0, 1, artifact.getId());
        harness.setGraveyard(player1, List.of());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
        harness.assertInGraveyard(player1, "What Must Be Done");
    }

    private void cast(int mode, List<java.util.UUID> targetIds) {
        prepareCard();
        if (targetIds.isEmpty()) {
            harness.castAndResolveSorcery(player1, 0, mode);
        } else {
            harness.castAndResolveSorcery(player1, 0, mode, targetIds.getFirst());
        }
    }

    private void prepareCard() {
        harness.setHand(player1, List.of(new WhatMustBeDone()));
        harness.addMana(player1, ManaColor.WHITE, 5);
    }
}
