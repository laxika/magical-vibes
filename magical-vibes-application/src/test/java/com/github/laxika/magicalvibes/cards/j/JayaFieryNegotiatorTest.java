package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.i.InThrallToThePit;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.s.ShoreUp;
import com.github.laxika.magicalvibes.cards.s.SunbathingRootwalla;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JayaFieryNegotiator.class, SunbathingRootwalla.class, LightningStrike.class,
        InThrallToThePit.class, ShoreUp.class})
class JayaFieryNegotiatorTest extends BaseCardTest {

    @Test
    @DisplayName("+1 creates a red Monk token with prowess")
    void plusOneCreatesMonkWithProwess() {
        addReadyJaya(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent monk = findPermanents(player1, "Monk").getFirst();
        assertThat(gqs.hasKeyword(gd, monk, Keyword.PROWESS)).isTrue();
    }

    @Test
    @DisplayName("-1 exiles two cards and grants play permission to the chosen card")
    void minusOneExilesAndPermitsChosenCard() {
        addReadyJaya(player1);
        Card first = new LightningStrike();
        Card second = new SunbathingRootwalla();
        harness.setLibrary(player1, List.of(first, second));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ExiledCardMayPlayChoice.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second);

        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));

        assertThat(gd.exilePlayPermissions).containsEntry(first.getId(), player1.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(second.getId());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castFromExile(player1, first.getId(), player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("-2 deals damage equal to the number of attacking creatures")
    void minusTwoDealsAttackerCountDamage() {
        addReadyJaya(player1);
        addCreatureReady(player1, new SunbathingRootwalla());
        addCreatureReady(player1, new SunbathingRootwalla());
        Permanent target = addCreatureReady(player2, new SunbathingRootwalla());

        harness.activateAbility(player1, 0, 2, null, target.getId());
        harness.passBothPriorities();
        declareAttackers(List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("-8 creates one trigger that copies a red instant or sorcery twice")
    void minusEightCreatesOneCopyTrigger() {
        Permanent jaya = addReadyJaya(player1);
        jaya.setCounterCount(CounterType.LOYALTY, 8);

        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).filteredOn(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && "Jaya, Fiery Negotiator's emblem".equals(e.getDescription()))
                .hasSize(1);
    }

    private Permanent addReadyJaya(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new JayaFieryNegotiator());
        permanent.setCounterCount(CounterType.LOYALTY, 5);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    @Test
    void monkProwessTriggersWhenCastingNoncreatureSpell() {
        addReadyJaya(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        Permanent monk = findPermanent(player1, "Monk");
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(2);
    }

    @Test
    void minusOneWithEmptyLibraryFinishesWithoutChoice() {
        addReadyJaya(player1);
        harness.setLibrary(player1, List.of());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.exilePlayPermissions).isEmpty();
    }

    @Test
    void minusOneWithOneCardAllowsChoosingThatCard() {
        addReadyJaya(player1);
        Card card = new SunbathingRootwalla();
        harness.setLibrary(player1, List.of(card));
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(card);
        assertThat(gd.exilePlayPermissions).containsEntry(card.getId(), player1.getId());
    }

    @Test
    void delayedDamageCountsAttackersAtResolution() {
        addReadyJaya(player1);
        Permanent attacker = addCreatureReady(player1, new SunbathingRootwalla());
        addCreatureReady(player1, new SunbathingRootwalla());
        Permanent chosen = addCreatureReady(player2, new SunbathingRootwalla());
        harness.activateAbility(player1, 0, 2, null, chosen.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);
        declareAttackers(List.of(1, 2));
        harness.castInstant(player1, 0, attacker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(chosen);
        assertThat(chosen.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void delayedDamageStillAffectsChosenPermanentAfterControlChanges() {
        addReadyJaya(player1);
        addCreatureReady(player1, new SunbathingRootwalla());
        Permanent chosen = addCreatureReady(player2, new SunbathingRootwalla());
        harness.activateAbility(player1, 0, 2, null, chosen.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new InThrallToThePit()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, chosen.getId());
        harness.passBothPriorities();
        declareAttackers(List.of(1));
        harness.passBothPriorities();
        assertThat(chosen.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void delayedDamageDoesNotTargetChosenCreatureAgain() {
        addReadyJaya(player1);
        addCreatureReady(player1, new SunbathingRootwalla());
        Permanent chosen = addCreatureReady(player2, new SunbathingRootwalla());
        harness.activateAbility(player1, 0, 2, null, chosen.getId());
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new ShoreUp()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        declareAttackers(List.of(1));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, chosen.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(chosen.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void emblemDoesNotCopyCreatureSpells() {
        Permanent jaya = addReadyJaya(player1);
        jaya.setCounterCount(CounterType.LOYALTY, 8);
        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new SunbathingRootwalla()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Sunbathing Rootwalla")).isEqualTo(1);
    }

}
