package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OkoTheRingleader.class, GrizzlyBears.class, Forest.class, Shock.class})
class OkoTheRingleaderTest extends BaseCardTest {

    @Test
    @DisplayName("Beginning-of-combat trigger copies a creature and grants hexproof until end of turn")
    void copiesCreatureAndGrantsHexproofUntilEndOfTurn() {
        Permanent oko = addReadyOko(player1, 3);
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, oko)).isTrue();
        assertThat(gqs.hasKeyword(gd, oko, Keyword.HEXPROOF)).isTrue();

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        assertThat(gqs.isPlaneswalker(gd, oko)).isTrue();
        assertThat(gqs.hasKeyword(gd, oko, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.isCreature(gd, bears)).isTrue();
    }

    @Test
    @DisplayName("+1 draws two cards and discards two without a crime")
    void plusOneWithoutCrimeDiscardsTwo() {
        Permanent oko = addReadyOko(player1, 3);
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(oko), 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(oko.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("+1 draws two cards and discards one after committing a crime")
    void plusOneAfterCrimeDiscardsOne() {
        Permanent oko = addReadyOko(player1, 3);
        harness.setHand(player1, List.of(new Shock(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(oko), 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(oko.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("-1 creates a 3/3 green Elk")
    void minusOneCreatesElk() {
        Permanent oko = addReadyOko(player1, 3);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(oko), 1, null, null);
        harness.passBothPriorities();

        Permanent elk = findPermanent(player1, "Elk");
        assertThat(elk.getCard().isToken()).isTrue();
        assertThat(elk.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(elk.getCard().getPower()).isEqualTo(3);
        assertThat(elk.getCard().getToughness()).isEqualTo(3);
        assertThat(oko.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("-5 copies each other nonland permanent but not lands or Oko")
    void minusFiveCopiesEachOtherNonlandPermanent() {
        Permanent oko = addReadyOko(player1, 5);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(oko), 2, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
        assertThat(findPermanents(player1, "Forest")).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat trigger without a creature to copy does not grant hexproof")
    void noCreatureToCopyDoesNotGrantHexproof() {
        Permanent oko = addReadyOko(player1, 3);

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();

        assertThat(gqs.isPlaneswalker(gd, oko)).isTrue();
        assertThat(gqs.hasKeyword(gd, oko, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Combat trigger with an illegal target does not grant hexproof")
    void removedTargetDoesNotGrantHexproof() {
        Permanent oko = addReadyOko(player1, 3);
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.handlePermanentChosen(player1, bears.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.isPlaneswalker(gd, oko)).isTrue();
        assertThat(gqs.hasKeyword(gd, oko, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("A token copying Oko's creature form inherits hexproof permanently")
    void copyOfCreatureFormRetainsHexproofAfterCleanup() {
        Permanent transformedOko = addReadyOko(player1, 3);
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        Permanent secondOko = addReadyOko(player1, 6);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(secondOko), 2, null, null);
        harness.passBothPriorities();

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> gqs.hasKeyword(gd, permanent, Keyword.HEXPROOF))
                .findFirst().orElseThrow();
        assertThat(gqs.isCreature(gd, copy)).isTrue();

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        assertThat(gqs.isPlaneswalker(gd, transformedOko)).isTrue();
        assertThat(gqs.isCreature(gd, copy)).isTrue();
        assertThat(gqs.hasKeyword(gd, copy, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Declining the optional combat target does not grant hexproof")
    void decliningCreatureCopyDoesNotGrantHexproof() {
        Permanent oko = addReadyOko(player1, 3);
        addReadyCreature(player1, new GrizzlyBears());
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.isPlaneswalker(gd, oko)).isTrue();
        assertThat(gqs.hasKeyword(gd, oko, Keyword.HEXPROOF)).isFalse();
    }

    private Permanent addReadyOko(Player player, int loyalty) {
        OkoTheRingleader okoCard = new OkoTheRingleader();
        Permanent oko = harness.addToBattlefieldAndReturn(player, okoCard);
        oko.setCounterCount(CounterType.LOYALTY, loyalty);
        oko.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return oko;
    }

    private Permanent addReadyCreature(Player player, GrizzlyBears card) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, card);
        creature.setSummoningSick(false);
        return creature;
    }
}
