package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlsquadHeavy;
import com.github.laxika.magicalvibes.cards.j.JourneyToNowhere;
import com.github.laxika.magicalvibes.cards.r.RelicOfProgenitus;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KetramoseTheNewDawn.class, Forest.class, GrizzlyBears.class,
        JourneyToNowhere.class, RelicOfProgenitus.class, Shock.class, HowlsquadHeavy.class})
class KetramoseTheNewDawnTest extends BaseCardTest {

    @Test
    void cannotAttackWithFewerThanSevenCardsInExile() {
        harness.setExile(player2, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        addCreatureReady(player1, new KetramoseTheNewDawn());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canAttackWithSevenCardsInExile() {
        harness.setExile(player2, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        addCreatureReady(player1, new KetramoseTheNewDawn());

        declareAttackers(player1, List.of(0));
    }

    @Test
    void drawsAndLosesLifeWhenCardIsExiledFromAGraveyardDuringYourTurn() {
        harness.addToBattlefield(player1, new KetramoseTheNewDawn());
        harness.addToBattlefield(player1, new RelicOfProgenitus());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 1, null, player2.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Shock");
        harness.assertLife(player1, 19);
    }

    @Test
    void drawsAndLosesLifeWhenPermanentIsExiledDuringYourTurn() {
        harness.addToBattlefield(player1, new KetramoseTheNewDawn());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new JourneyToNowhere()));
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        resolveAllTriggers();

        harness.assertInHand(player1, "Shock");
        harness.assertLife(player1, 19);
    }

    @Test
    void cannotBlockWithSixCardsInExile() {
        harness.setExile(player2, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player1, new KetramoseTheNewDawn());

        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBlockWithSevenCardsSplitBetweenOwners() {
        harness.setExile(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setExile(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        addCreatureReady(player2, new GrizzlyBears());
        var ketramose = addCreatureReady(player1, new KetramoseTheNewDawn());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))));

        assertThat(ketramose.isBlocking()).isTrue();
    }

    @Test
    void cannotBeBlockedByOnlyOneCreature() {
        harness.setExile(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));
        addCreatureReady(player1, new KetramoseTheNewDawn());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void gainsLifeAndSurvivesLethalCombatDamageFromTwoBlockers() {
        harness.setExile(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));
        addCreatureReady(player1, new KetramoseTheNewDawn());
        var firstBlocker = addCreatureReady(player2, new GrizzlyBears());
        var secondBlocker = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 20);

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(firstBlocker.getId(), 2, secondBlocker.getId(), 2));

        harness.assertOnBattlefield(player1, "Ketramose, the New Dawn");
        harness.assertLife(player1, 24);
        assertThat(countPermanents(player2, "Grizzly Bears")).isZero();
    }

    @Test
    void remainsAnAttackerWhenExileCountFallsBelowSeven() {
        harness.setExile(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));
        var ketramose = addCreatureReady(player1, new KetramoseTheNewDawn());
        harness.setLife(player2, 20);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(player1, List.of(0)));
        gd.removeFromExile(gd.exiledCards.getFirst().card().getId());
        assertThat(ketramose.isAttacking()).isTrue();
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 16);
    }

    @Test
    void doesNotTriggerForGraveyardExileOnOpponentsTurn() {
        harness.addToBattlefield(player1, new KetramoseTheNewDawn());
        harness.addToBattlefield(player1, new RelicOfProgenitus());
        harness.setGraveyard(player2, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 1, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void triggersWhenOpponentExilesYourGraveyardOnYourTurn() {
        harness.addToBattlefield(player1, new KetramoseTheNewDawn());
        harness.addToBattlefield(player2, new RelicOfProgenitus());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player2, 0, null, player1.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Forest");
        harness.assertLife(player1, 19);
    }

    @Test
    void emptyGraveyardDoesNotTrigger() {
        harness.addToBattlefield(player1, new KetramoseTheNewDawn());
        harness.addToBattlefield(player1, new RelicOfProgenitus());
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 1, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    void multipleCardsFromOneGraveyardTriggerOnceSeparatelyFromRelicsExileCost() {
        harness.addToBattlefield(player1, new KetramoseTheNewDawn());
        harness.addToBattlefield(player1, new RelicOfProgenitus());
        harness.setGraveyard(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 1, null, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertLife(player1, 18);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void simultaneousExileFromBothGraveyardsTriggersOnceSeparatelyFromRelicsExileCost() {
        harness.addToBattlefield(player1, new KetramoseTheNewDawn());
        harness.addToBattlefield(player1, new RelicOfProgenitus());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setGraveyard(player2, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 1, null, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertLife(player1, 18);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void triggersWhenKetramoseItselfIsExiledDuringYourTurn() {
        harness.addToBattlefield(player1, new KetramoseTheNewDawn());
        harness.setHand(player1, List.of(new JourneyToNowhere()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, harness.getPermanentId(player1, "Ketramose, the New Dawn"));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Ketramose, the New Dawn");
        harness.assertInHand(player1, "Forest");
        harness.assertLife(player1, 19);
    }

    @Test
    void exilingOnlyATokenDoesNotTrigger() {
        harness.addToBattlefield(player1, new KetramoseTheNewDawn());
        harness.addToBattlefield(player1, new HowlsquadHeavy());
        harness.setHand(player1, List.of(new JourneyToNowhere()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, this::resolveAllTriggers);
        var token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, token.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
    }
}
