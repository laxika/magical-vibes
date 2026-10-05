package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.Compulsion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NantukoCalmer.class, Compulsion.class})
class NantukoCalmerTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 while its controller has seven cards in their graveyard")
    void getsBoostAtThreshold() {
        fillGraveyard(player1, 7);
        Permanent calmer = harness.addToBattlefieldAndReturn(player1, new NantukoCalmer());

        assertThat(gqs.getEffectivePower(gd, calmer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, calmer)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not get the threshold boost below seven cards")
    void noBoostBelowThreshold() {
        fillGraveyard(player1, 6);
        Permanent calmer = harness.addToBattlefieldAndReturn(player1, new NantukoCalmer());

        assertThat(gqs.getEffectivePower(gd, calmer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, calmer)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not get the threshold boost from an opponent's graveyard")
    void opponentGraveyardDoesNotEnableThreshold() {
        fillGraveyard(player2, 7);
        Permanent calmer = harness.addToBattlefieldAndReturn(player1, new NantukoCalmer());

        assertThat(gqs.getEffectivePower(gd, calmer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, calmer)).isEqualTo(3);
    }

    @Test
    @DisplayName("Pays mana, taps, and sacrifices itself to destroy an enchantment")
    void destroysTargetEnchantment() {
        Permanent calmer = addCreatureReady(player1, new NantukoCalmer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Compulsion());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(calmer), null,
                target.getId());
        harness.passBothPriorities();

        assertThat(calmer.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Nantuko Calmer");
        harness.assertInGraveyard(player1, "Nantuko Calmer");
        harness.assertNotOnBattlefield(player2, "Compulsion");
        harness.assertInGraveyard(player2, "Compulsion");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent calmer = addCreatureReady(player1, new NantukoCalmer());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NantukoCalmer());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(calmer),
                null,
                creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Threshold updates immediately when the graveyard grows and shrinks")
    void thresholdTracksGraveyardChanges() {
        Permanent calmer = harness.addToBattlefieldAndReturn(player1, new NantukoCalmer());
        fillGraveyard(player1, 6);
        assertThat(gqs.getEffectivePower(gd, calmer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, calmer)).isEqualTo(3);

        fillGraveyard(player1, 8);
        assertThat(gqs.getEffectivePower(gd, calmer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, calmer)).isEqualTo(4);

        fillGraveyard(player1, 6);
        assertThat(gqs.getEffectivePower(gd, calmer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, calmer)).isEqualTo(3);
    }

    @Test
    @DisplayName("Sacrifice is paid before resolution and can enable another Calmer's threshold")
    void sacrificeIsImmediateAndCanDestroyOwnEnchantment() {
        fillGraveyard(player1, 6);
        Permanent calmer = addCreatureReady(player1, new NantukoCalmer());
        Permanent otherCalmer = addCreatureReady(player1, new NantukoCalmer());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Compulsion());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(calmer), null,
                target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(calmer).contains(otherCalmer, target);
        harness.assertInGraveyard(player1, "Nantuko Calmer");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gqs.getEffectivePower(gd, otherCalmer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, otherCalmer)).isEqualTo(4);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Compulsion");
        harness.assertInGraveyard(player1, "Compulsion");
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent calmer = harness.addToBattlefieldAndReturn(player1, new NantukoCalmer());
        calmer.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Compulsion());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(calmer), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Nantuko Calmer");
        harness.assertOnBattlefield(player2, "Compulsion");
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent calmer = addCreatureReady(player1, new NantukoCalmer());
        calmer.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Compulsion());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(calmer), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Nantuko Calmer");
        harness.assertOnBattlefield(player2, "Compulsion");
    }

    @Test
    @DisplayName("Cannot activate without green mana")
    void cannotActivateWithoutGreenMana() {
        Permanent calmer = addCreatureReady(player1, new NantukoCalmer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Compulsion());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(calmer), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(calmer.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Nantuko Calmer");
        harness.assertOnBattlefield(player2, "Compulsion");
    }

    private void fillGraveyard(Player player, int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new NantukoCalmer());
        }
        harness.setGraveyard(player, cards);
    }
}
