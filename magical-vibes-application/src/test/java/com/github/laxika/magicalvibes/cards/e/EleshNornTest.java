package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.ArcTrail;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.p.PhyrexianAwakening;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EleshNorn.class, ArcTrail.class, GrizzlyBears.class, LightningBolt.class,
        PhyrexianAwakening.class, Opalescence.class})
class EleshNornTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent source damage makes its controller lose life when they decline to pay")
    void opponentSourceDamageCausesLifeLossWhenPaymentIsDeclined() {
        harness.addToBattlefield(player1, new EleshNorn());
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());
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

        harness.castAndResolveSorcery(player2, 0, List.of(first.getId(), second.getId()));
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

    @Test
    @DisplayName("Paying one mana prevents the opponent's life loss")
    void opponentCanPayToAvoidLifeLoss() {
        harness.addToBattlefield(player1, new EleshNorn());
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An opponent unable to pay automatically loses two life")
    void opponentWithoutManaLosesLife() {
        harness.addToBattlefield(player1, new EleshNorn());
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Damage from your own source does not trigger Elesh Norn")
    void ownSourceDamageDoesNotTrigger() {
        harness.addToBattlefield(player1, new EleshNorn());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 17);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Chapter I leaves opposing Incubator tokens untransformed")
    void chapterIDoesNotTransformOpposingIncubators() {
        harness.enterBattlefieldAndReturn(player2, new PhyrexianAwakening());
        harness.passBothPriorities();
        Permanent opposingIncubator = findPermanent(player2, "Incubator");
        addBackFaceSaga(0);

        advanceSagaToNextChapter();
        harness.passBothPriorities();

        assertThat(opposingIncubator.isTransformed()).isFalse();
        assertThat(gqs.isCreature(gd, opposingIncubator)).isFalse();
        assertThat(findPermanents(player1, "Phyrexian")).hasSize(5);
    }

    @Test
    @DisplayName("Chapter I also transforms Incubator tokens created earlier")
    void chapterITransformsExistingOwnIncubators() {
        harness.enterBattlefieldAndReturn(player1, new PhyrexianAwakening());
        harness.passBothPriorities();
        Permanent existingIncubator = findPermanent(player1, "Incubator");
        addBackFaceSaga(0);

        advanceSagaToNextChapter();
        harness.passBothPriorities();

        assertThat(existingIncubator.isTransformed()).isTrue();
        assertThat(existingIncubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(findPermanents(player1, "Phyrexian")).hasSize(6);
    }

    @Test
    @DisplayName("Chapter II grants double strike to the Saga itself when it is a creature")
    void chapterIIIncludesAnimatedSaga() {
        Permanent saga = addBackFaceSaga(1);
        saga.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addToBattlefield(player1, new Opalescence());
        assertThat(gqs.isCreature(gd, saga)).isTrue();

        advanceSagaToNextChapter();
        harness.passBothPriorities();

        assertThat(saga.getPowerModifier()).isEqualTo(1);
        assertThat(saga.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, saga,
                com.github.laxika.magicalvibes.model.Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Chapter II only affects your creatures present when it resolves")
    void chapterIIExcludesOpponentsAndLaterCreatures() {
        addBackFaceSaga(1);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        advanceSagaToNextChapter();
        harness.passBothPriorities();
        Permanent laterCreature = addCreatureReady(player1, new GrizzlyBears());

        assertThat(ownCreature.getPowerModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, ownCreature,
                com.github.laxika.magicalvibes.model.Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(opposingCreature.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, opposingCreature,
                com.github.laxika.magicalvibes.model.Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(laterCreature.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, laterCreature,
                com.github.laxika.magicalvibes.model.Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Elesh Norn cannot count herself toward the three sacrificed creatures")
    void activationRequiresThreeOtherCreatures() {
        Permanent elesh = addCreatureReady(player1, new EleshNorn());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(elesh), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The transform ability cannot be activated during upkeep")
    void activationIsRestrictedToSorceryTiming() {
        Permanent elesh = addCreatureReady(player1, new EleshNorn());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(elesh), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("A borrowed Elesh Norn returns transformed under her owner's control")
    void activationReturnsSagaToOwner() {
        EleshNorn card = new EleshNorn();
        card.setOwnerId(player2.getId());
        Permanent elesh = addCreatureReady(player1, card);
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(elesh), null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "The Argent Etchings");
        assertThat(findPermanent(player2, "The Argent Etchings").isTransformed()).isTrue();
        assertThat(findPermanents(player2, "Phyrexian")).hasSize(5);
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
