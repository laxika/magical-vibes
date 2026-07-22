package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageEqualToManaSpentToCastToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChandraDressedToKillTest extends BaseCardTest {

    @Test
    @DisplayName("+1 adds {R} and deals 1 damage to target player")
    void plusOneAddsManaAndDealsDamage() {
        Permanent chandra = addReadyChandra(player1);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        int redBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.RED);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(redBefore + 1);
    }

    @Test
    @DisplayName("+1 still adds {R} when no damage target is chosen")
    void plusOneAllowsDecliningDamageTarget() {
        Permanent chandra = addReadyChandra(player1);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        int redBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.RED);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(redBefore + 1);
    }

    @Test
    @DisplayName("+1 exile grants cast permission when the top card is red")
    void secondPlusOneExilesRedWithCastPermission() {
        Permanent chandra = addReadyChandra(player1);
        Card shock = putColoredSpellOnTop(player1, "Shock", CardColor.RED, "{R}");

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(shock.getId()));
        assertThat(gd.exilePlayPermissions.get(shock.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(shock.getId());
    }

    @Test
    @DisplayName("+1 exile does not grant cast permission when the top card is not red")
    void secondPlusOneExilesNonRedWithoutCastPermission() {
        Permanent chandra = addReadyChandra(player1);
        Card opt = putColoredSpellOnTop(player1, "Opt", CardColor.BLUE, "{U}");

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(opt.getId()));
        assertThat(gd.exilePlayPermissions).doesNotContainKey(opt.getId());
    }

    @Test
    @DisplayName("−7 exiles five cards, grants cast permission for red ones, and creates emblem")
    void minusSevenExilesFiveAndCreatesEmblem() {
        Permanent chandra = addReadyChandra(player1);
        chandra.setCounterCount(CounterType.LOYALTY, 7);

        Card red1 = putColoredSpellOnTop(player1, "Red One", CardColor.RED, "{R}");
        Card blue = putColoredSpellOnTop(player1, "Blue One", CardColor.BLUE, "{U}");
        Card red2 = putColoredSpellOnTop(player1, "Red Two", CardColor.RED, "{1}{R}");
        Card green = putColoredSpellOnTop(player1, "Green One", CardColor.GREEN, "{G}");
        Card red3 = putColoredSpellOnTop(player1, "Red Three", CardColor.RED, "{R}");

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Chandra, Dressed to Kill"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .contains(red1.getId(), blue.getId(), red2.getId(), green.getId(), red3.getId());
        assertThat(gd.exilePlayPermissions.get(red1.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissions.get(red2.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissions.get(red3.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(blue.getId());
        assertThat(gd.exilePlayPermissions).doesNotContainKey(green.getId());

        assertThat(gd.emblems).hasSize(1);
        Emblem emblem = gd.emblems.getFirst();
        assertThat(emblem.controllerId()).isEqualTo(player1.getId());
        assertThat(emblem.staticEffects().getFirst())
                .isInstanceOf(DealDamageEqualToManaSpentToCastToAnyTargetEffect.class);
    }

    @Test
    @DisplayName("Emblem deals mana spent to cast a red spell to any target")
    void emblemDealsManaSpentDamageOnRedSpell() {
        addReadyChandra(player1);
        gd.emblems.add(new Emblem(player1.getId(), List.of(
                new DealDamageEqualToManaSpentToCastToAnyTargetEffect(
                        new CardColorPredicate(CardColor.RED))
        ), new ChandraDressedToKill()));

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        // Shock the controller so emblem damage to the opponent is unambiguous
        harness.castInstant(player1, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null).isTrue();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        // Shock costs {R} — emblem deals 1 to the opponent
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Emblem does not trigger on a non-red spell")
    void emblemIgnoresNonRedSpells() {
        addReadyChandra(player1);
        gd.emblems.add(new Emblem(player1.getId(), List.of(
                new DealDamageEqualToManaSpentToCastToAnyTargetEffect(
                        new CardColorPredicate(CardColor.RED))
        ), new ChandraDressedToKill()));

        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.pendingInteractions).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate −7 with insufficient loyalty")
    void cannotActivateMinusSevenWithInsufficientLoyalty() {
        addReadyChandra(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    private Permanent addReadyChandra(Player player) {
        Permanent perm = new Permanent(new ChandraDressedToKill());
        perm.setCounterCount(CounterType.LOYALTY, 3);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Card putColoredSpellOnTop(Player player, String name, CardColor color, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.setColors(List.of(color));
        gd.playerDecks.get(player.getId()).addFirst(card);
        return card;
    }
}
