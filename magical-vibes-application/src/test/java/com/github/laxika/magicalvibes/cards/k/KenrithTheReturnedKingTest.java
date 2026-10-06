package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KenrithTheReturnedKing.class, GrizzlyBears.class, Forest.class, HolyDay.class})
class KenrithTheReturnedKingTest extends BaseCardTest {

    @Test
    @DisplayName("Red ability gives all creatures trample and haste until end of turn")
    void grantsTrampleAndHasteToAllCreatures() {
        Permanent kenrith = addReadyKenrith(player1);
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, kenrith, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, kenrith, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isTrue();

        GameTestEngineContext.get().getBean(TurnCleanupService.class).resetEndOfTurnModifiers(gd);

        assertThat(gqs.hasKeyword(gd, kenrith, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Green ability puts a +1/+1 counter on a target creature")
    void putsCounterOnTargetCreature() {
        addReadyKenrith(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Green ability rejects a noncreature target")
    void rejectsNoncreatureTarget() {
        addReadyKenrith(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("White ability makes the target player gain 5 life")
    void targetPlayerGainsLife() {
        addReadyKenrith(player1);
        int lifeBefore = harness.getGameData().getLife(player2.getId());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().getLife(player2.getId())).isEqualTo(lifeBefore + 5);
    }

    @Test
    @DisplayName("Blue ability makes the target player draw a card")
    void targetPlayerDrawsCard() {
        addReadyKenrith(player1);
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 3, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Black ability returns a target creature card under its owner's control")
    void returnsTargetCreatureUnderItsOwnersControl() {
        addReadyKenrith(player1);
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 4, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Red ability gives all creatures trample and haste until end of turn")
    void allCreaturesGainTrampleAndHasteUntilEndOfTurn() {
        Permanent kenrith = addReadyKenrith(player1);
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        prepareMainPhase();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, kenrith, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, kenrith, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, kenrith, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Black ability returns a target creature from any graveyard under its owner's control")
    void returnsTargetCreatureFromAnyGraveyardUnderItsOwnersControl() {
        addReadyKenrith(player1);
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 4, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() == creature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == creature);
    }

    @Test
    @DisplayName("Black ability rejects a noncreature graveyard target")
    void blackAbilityRejectsNoncreatureTarget() {
        addReadyKenrith(player1);
        Card instant = new HolyDay();
        harness.setGraveyard(player2, List.of(instant));
        prepareMainPhase();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 4, List.of(instant.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Player-targeting abilities reject permanent targets")
    void playerTargetingAbilitiesRejectPermanentTargets() {
        addReadyKenrith(player1);
        Permanent permanent = addCreatureReady(player2, new GrizzlyBears());
        prepareMainPhase();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, permanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyKenrith(Player player) {
        return addCreatureReady(player, new KenrithTheReturnedKing());
    }

    @Test
    @DisplayName("Red ability affects creatures present at resolution even after Kenrith leaves")
    void redAbilityUsesCreaturesPresentAtResolution() {
        Permanent kenrith = addReadyKenrith(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, 0, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(kenrith);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("White ability can target its controller while Kenrith is tapped and summoning sick")
    void gainsLifeForControllerWithoutTapOrSummoningRestriction() {
        Permanent kenrith = harness.addToBattlefieldAndReturn(player1, new KenrithTheReturnedKing());
        kenrith.setSummoningSick(true);
        kenrith.tap();
        int lifeBefore = gd.getLife(player1.getId());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 5);
    }

    @Test
    @DisplayName("Blue ability draws exactly one card for its controller")
    void drawsForController() {
        addReadyKenrith(player1);
        Card first = new GrizzlyBears();
        Card second = new HolyDay();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 3, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    @DisplayName("Black ability does not return a target that leaves the graveyard before resolution")
    void doesNotReturnMissingGraveyardTarget() {
        addReadyKenrith(player1);
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 4, List.of(creature.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player2, List.of(creature));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == creature);
    }

    @Test
    @DisplayName("Black ability returns a creature from its controller's graveyard untapped")
    void returnsCreatureFromOwnGraveyard() {
        addReadyKenrith(player1);
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 4, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == creature).findFirst().orElseThrow();
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.isSummoningSick()).isTrue();
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
