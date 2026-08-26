package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.ArcTrail;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EleshNorn.class, ArcTrail.class, GrizzlyBears.class, LightningBolt.class})
class EleshNornTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent source damage makes its controller lose life when they decline to pay")
    void opponentSourceDamageCausesLifeLossWhenPaymentIsDeclined() {
        harness.addToBattlefield(player1, new EleshNorn());
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Opponent source damage triggers once for each permanent damaged")
    void opponentSourceDamageTriggersForEachDamagedPermanent() {
        harness.addToBattlefield(player1, new EleshNorn());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new ArcTrail()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player2, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Sacrificing three other creatures returns Elesh Norn transformed")
    void sacrificesThreeOtherCreaturesAndReturnsTheArgentEtchings() {
        Permanent elesh = addCreatureReady(player1, new EleshNorn());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent third = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(elesh), null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent saga = findPermanent(player1, "The Argent Etchings");
        assertThat(saga).isNotNull();
        assertThat(saga.isTransformed()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(first.getCard(), second.getCard(), third.getCard());
    }

    @Test
    @DisplayName("Chapter I creates and transforms five Incubator tokens")
    void chapterICreatesAndTransformsIncubators() {
        Permanent saga = addBackFaceSaga(0);

        advanceSagaToNextChapter();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Incubator")).isEmpty();
        List<Permanent> phyrexians = findPermanents(player1, "Phyrexian");
        assertThat(phyrexians).hasSize(5);
        assertThat(phyrexians).allSatisfy(token -> {
            assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
            assertThat(gqs.isCreature(gd, token)).isTrue();
            assertThat(gqs.isArtifact(gd, token)).isTrue();
        });
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Chapter II boosts creatures and grants double strike")
    void chapterIIBoostsAndGrantsDoubleStrike() {
        Permanent saga = addBackFaceSaga(1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        advanceSagaToNextChapter();
        harness.passBothPriorities();

        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);
        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bears, com.github.laxika.magicalvibes.model.Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Chapter III destroys other nonartifact nonland non-Phyrexian permanents and returns front face up")
    void chapterIIIDestroysTheAllowedPermanentsAndReturnsEleshNorn() {
        addBackFaceSaga(2);
        Permanent destroyedByChapter = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Card artifactCard = card("Artifact", CardType.ARTIFACT);
        Card landCard = card("Land", CardType.LAND);
        Card phyrexianCard = card("Phyrexian", CardType.CREATURE);
        phyrexianCard.setSubtypes(List.of(CardSubtype.PHYREXIAN));
        phyrexianCard.setPower(2);
        phyrexianCard.setToughness(2);
        harness.addToBattlefield(player1, artifactCard);
        harness.addToBattlefield(player1, landCard);
        Permanent phyrexian = harness.addToBattlefieldAndReturn(player1, phyrexianCard);

        advanceSagaToNextChapter();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Elesh Norn")).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(destroyedByChapter);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(phyrexian);
        assertThat(findPermanent(player1, "Artifact")).isNotNull();
        assertThat(findPermanent(player1, "Land")).isNotNull();
    }

    private Permanent addBackFaceSaga(int lore) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new EleshNorn());
        saga.setCard(saga.getOriginalCard().getBackFaceCard());
        saga.setTransformed(true);
        saga.setCounterCount(CounterType.LORE, lore);
        return saga;
    }

    private void advanceSagaToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Card card(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setToken(true);
        return card;
    }
}
