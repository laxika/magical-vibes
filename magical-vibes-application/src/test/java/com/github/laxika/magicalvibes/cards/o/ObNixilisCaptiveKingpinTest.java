package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GutShot;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.VaultSkirge;
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

@CardUsed({ObNixilisCaptiveKingpin.class, Forest.class, GutShot.class, Shock.class, VaultSkirge.class})
class ObNixilisCaptiveKingpinTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers on exactly one life loss, adds a counter, and exiles the top card for play")
    void triggersOnExactlyOneLifeLoss() {
        Permanent obNixilis = harness.addToBattlefieldAndReturn(player1, new ObNixilisCaptiveKingpin());
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new GutShot()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(obNixilis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        assertThat(gd.exilePlayPermissions).containsEntry(topCard.getId(), player1.getId());
    }

    @Test
    @DisplayName("Does not trigger when an opponent loses more than one life")
    void doesNotTriggerOnMoreThanOneLifeLoss() {
        Permanent obNixilis = harness.addToBattlefieldAndReturn(player1, new ObNixilisCaptiveKingpin());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(obNixilis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void doesNotTriggerWhenItsControllerLosesOneLife() {
        Permanent obNixilis = harness.addToBattlefieldAndReturn(player1, new ObNixilisCaptiveKingpin());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new GutShot()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 19);
        assertThat(obNixilis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void addsCounterEvenWithAnEmptyLibrary() {
        Permanent obNixilis = harness.addToBattlefieldAndReturn(player1, new ObNixilisCaptiveKingpin());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new GutShot()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(obNixilis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void separateOneLifeLossEventsEachTrigger() {
        Permanent obNixilis = harness.addToBattlefieldAndReturn(player1, new ObNixilisCaptiveKingpin());
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new GutShot(), new GutShot()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(obNixilis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
    }

    @Test
    void canPlayAnExiledLandButCannotPlayASecondLand() {
        harness.addToBattlefield(player1, new ObNixilisCaptiveKingpin());
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new GutShot(), new GutShot()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.castFromExile(player1, first.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(first.getId()));
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(second);
    }

    @Test
    void exiledSpellStillRequiresItsManaCost() {
        harness.addToBattlefield(player1, new ObNixilisCaptiveKingpin());
        Shock topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new GutShot()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, topCard.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    void permissionExpiresWhenTheControllersNextEndStepBegins() {
        harness.addToBattlefield(player1, new ObNixilisCaptiveKingpin());
        Shock topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new GutShot()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
    }

    @Test
    void permissionGrantedDuringEndStepLastsThroughTheFollowingTurn() {
        harness.addToBattlefield(player1, new ObNixilisCaptiveKingpin());
        Shock topCard = new Shock();
        harness.setLibrary(player1, List.of(topCard, new Forest()));
        harness.setHand(player1, List.of(new GutShot()));
        harness.setHand(player2, List.of());
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, topCard.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void simultaneousCombatDamageFromTwoOnePowerCreaturesDoesNotTrigger() {
        Permanent obNixilis = harness.addToBattlefieldAndReturn(player1, new ObNixilisCaptiveKingpin());
        addCreatureReady(player1, new VaultSkirge());
        addCreatureReady(player1, new VaultSkirge());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(player1, List.of(1, 2));
        resolveCombat(player1);
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(obNixilis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void triggerStillExilesAndGrantsPermissionIfObNixilisLeavesBeforeResolution() {
        Permanent obNixilis = harness.addToBattlefieldAndReturn(player1, new ObNixilisCaptiveKingpin());
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new GutShot(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, obNixilis.getId());
        harness.castAndResolveInstant(player1, 0, obNixilis.getId());
        harness.assertNotOnBattlefield(player1, "Ob Nixilis, Captive Kingpin");
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(topCard);
        harness.castFromExile(player1, topCard.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(topCard.getId()));
    }
}
