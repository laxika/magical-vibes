package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AssaultZeppelid;
import com.github.laxika.magicalvibes.cards.b.BeaconHawk;
import com.github.laxika.magicalvibes.cards.b.BraceForImpact;
import com.github.laxika.magicalvibes.cards.c.Carom;
import com.github.laxika.magicalvibes.cards.v.Voidslime;
import com.github.laxika.magicalvibes.cards.w.WreckingBall;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Demonfire.class, Voidslime.class, BraceForImpact.class, AssaultZeppelid.class,
        Carom.class, BeaconHawk.class, WreckingBall.class})
class DemonfireTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to any target")
    void dealsXDamageToPlayer() {
        harness.setHand(player1, List.of(new Demonfire()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("A non-hellbent Demonfire can be countered")
    void nonHellbentCanBeCountered() {
        Demonfire demonfire = new Demonfire();
        harness.setHand(player1, List.of(demonfire, new Voidslime()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player2, List.of(new Voidslime()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, demonfire.getId());

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Demonfire");
    }

    @Test
    @DisplayName("A hellbent Demonfire can't be countered")
    void hellbentCannotBeCountered() {
        Demonfire demonfire = new Demonfire();
        harness.setHand(player1, List.of(demonfire));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player2, List.of(new Voidslime()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, demonfire.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player2, "Voidslime");
    }

    @Test
    @DisplayName("Non-hellbent damage can be prevented")
    void nonHellbentDamageCanBePrevented() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        harness.setHand(player1, List.of(new Demonfire(), new Voidslime()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setHand(player2, List.of(new BraceForImpact()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 3, target.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Assault Zeppelid");
    }

    @Test
    @DisplayName("Hellbent damage can't be prevented")
    void hellbentDamageCannotBePrevented() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        harness.setHand(player1, List.of(new Demonfire()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player2, List.of(new BraceForImpact()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 2, target.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Assault Zeppelid");
    }

    @Test
    @DisplayName("A creature dealt lethal Demonfire damage is exiled")
    void lethalDamageExilesCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        harness.setHand(player1, List.of(new Demonfire()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 3, target.getId());

        harness.assertNotInGraveyard(player2, "Assault Zeppelid");
        harness.assertNotOnBattlefield(player2, "Assault Zeppelid");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Assault Zeppelid"));
    }

    @Test
    @DisplayName("Redirected Demonfire damage exiles the creature that receives it")
    void redirectedDamageExilesActualRecipient() {
        Permanent originalTarget = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        Permanent redirectedTarget = harness.addToBattlefieldAndReturn(player2, new BeaconHawk());
        Demonfire demonfire = new Demonfire();

        harness.setHand(player1, List.of(demonfire));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setHand(player2, List.of(new Carom()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 1, originalTarget.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, List.of(originalTarget.getId(), redirectedTarget.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Assault Zeppelid");
        harness.assertNotInGraveyard(player2, "Beacon Hawk");
        harness.assertNotOnBattlefield(player2, "Beacon Hawk");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Beacon Hawk"));
    }

    @Test
    @DisplayName("Nonlethal Demonfire damage exiles a creature destroyed later that turn")
    void nonlethalDamageExilesOnLaterDestruction() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        harness.setHand(player1, List.of(new Demonfire(), new WreckingBall()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 1, target.getId());
        harness.assertOnBattlefield(player2, "Assault Zeppelid");
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Assault Zeppelid");
        harness.assertNotInGraveyard(player2, "Assault Zeppelid");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Assault Zeppelid"));
    }

    @Test
    @DisplayName("Zero damage does not exile a creature destroyed later")
    void zeroDamageDoesNotCreateExileReplacement() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        harness.setHand(player1, List.of(new Demonfire(), new WreckingBall()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Assault Zeppelid");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Assault Zeppelid"));
    }

    @Test
    @DisplayName("Fully prevented damage does not exile a creature destroyed later")
    void preventedDamageDoesNotCreateExileReplacement() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        harness.setHand(player1, List.of(new Demonfire(), new WreckingBall()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new BraceForImpact()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 1, target.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Assault Zeppelid");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Assault Zeppelid"));
    }

    @Test
    @DisplayName("Drawing a card while Demonfire is on the stack removes its counter protection")
    void drawingCardRemovesHellbentCounterProtection() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AssaultZeppelid());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BeaconHawk());
        Demonfire demonfire = new Demonfire();
        harness.setHand(player1, List.of(demonfire, new Carom()));
        harness.setLibrary(player1, List.of(new BeaconHawk()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new Voidslime()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.castAndResolveInstant(player1, 0, List.of(first.getId(), second.getId()));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, demonfire.getId());

        harness.assertInGraveyard(player1, "Demonfire");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Emptying the hand in response to a counter protects Demonfire")
    void emptyingHandAddsHellbentCounterProtection() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AssaultZeppelid());
        Demonfire demonfire = new Demonfire();
        harness.setHand(player1, List.of(demonfire, new WreckingBall()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new Voidslime()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, demonfire.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player2, "Voidslime");
    }
}
