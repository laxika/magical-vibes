package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WoollySpider;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpinneretAndSpiderling.class, WoollySpider.class, GrizzlyBears.class, Forest.class, Unsummon.class})
class SpinneretAndSpiderlingTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when at least two Spiders attack")
    void putsCounterWhenTwoSpidersAttack() {
        Permanent spinneret = addCreatureReady(player1, new SpinneretAndSpiderling());
        addCreatureReady(player1, new WoollySpider());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(spinneret.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when fewer than two Spiders attack")
    void doesNotTriggerWithFewerThanTwoSpiders() {
        Permanent spinneret = addCreatureReady(player1, new SpinneretAndSpiderling());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(spinneret.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Exiles the top card after dealing at least four damage")
    void exilesTopCardAfterDealingAtLeastFourDamage() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        Permanent spinneret = addCreatureReady(player1, new SpinneretAndSpiderling());
        spinneret.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        spinneret.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
    }

    @Test
    @DisplayName("Does not trigger from dealing less than four damage")
    void doesNotTriggerFromLessThanFourDamage() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        Permanent spinneret = addCreatureReady(player1, new SpinneretAndSpiderling());
        spinneret.setAttacking(true);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Triggers when two other Spiders attack while Spinneret stays back")
    void triggersWithoutAttackingItself() {
        Permanent spinneret = addCreatureReady(player1, new SpinneretAndSpiderling());
        addCreatureReady(player1, new WoollySpider());
        addCreatureReady(player1, new WoollySpider());

        declareAttackers(List.of(1, 2));
        resolveAllTriggers();

        assertThat(spinneret.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Still puts the counter on itself if another attacking Spider leaves")
    void attackEventIsNotRecheckedAtResolution() {
        Permanent spinneret = addCreatureReady(player1, new SpinneretAndSpiderling());
        Permanent otherSpider = addCreatureReady(player1, new WoollySpider());
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0, 1)));
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, otherSpider.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(otherSpider.getCard());
        assertThat(spinneret.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The exiled land can be played during the controller's main phase")
    void canPlayExiledLand() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        Permanent spinneret = addCreatureReady(player1, new SpinneretAndSpiderling());
        spinneret.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        spinneret.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN,
                () -> harness.castFromExile(player1, topCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(topCard.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Exiled spells require their normal mana cost")
    void exiledSpellRequiresMana() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        Permanent spinneret = addCreatureReady(player1, new SpinneretAndSpiderling());
        spinneret.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        spinneret.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.withAutoStop(TurnStep.POSTCOMBAT_MAIN, () -> {
            harness.castFromExile(player1, topCard.getId());
            resolveAllTriggers();
        });

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(topCard.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Play permission lasts through the controller's next turn")
    void permissionExpiresAfterNextTurn() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard, new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        Permanent spinneret = addCreatureReady(player1, new SpinneretAndSpiderling());
        spinneret.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        spinneret.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        resolveAllTriggers();

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gd.exilePlayPermissions).containsKey(topCard.getId());
        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThat(gd.exilePlayPermissions).containsKey(topCard.getId());
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Dealing more than four damage to a creature also exiles a card")
    void triggersFromDamageToCreature() {
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        Permanent spinneret = addCreatureReady(player1, new SpinneretAndSpiderling());
        Permanent blocker = addCreatureReady(player2, new WoollySpider());
        spinneret.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        spinneret.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(spinneret.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("An empty library does not prevent the damage trigger from resolving")
    void emptyLibraryDoesNothing() {
        harness.setLibrary(player1, List.of());
        Permanent spinneret = addCreatureReady(player1, new SpinneretAndSpiderling());
        spinneret.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        spinneret.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
