package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.e.EnlightenedManiac;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LayClaim;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Displace.class, GrizzlyBears.class, AngelOfMercy.class, LayClaim.class,
        SoulWarden.class, EnlightenedManiac.class})
class DisplaceTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles and immediately returns up to two targeted creatures")
    void flickersTwoCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AngelOfMercy());
        harness.setHand(player1, List.of(new Displace()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID angelId = harness.getPermanentId(player1, "Angel of Mercy");

        harness.castAndResolveInstant(player1, 0, List.of(bearsId, angelId));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Angel of Mercy");
        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(bearsId);
        assertThat(harness.getPermanentId(player1, "Angel of Mercy")).isNotEqualTo(angelId);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Returns a stolen creature under its owner's control")
    void returnsUnderOwnerControl() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new LayClaim()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castEnchantment(player1, 0, bearsId);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Displace()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAndResolveInstant(player1, 0, bearsId);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Allows choosing no creatures")
    void allowsNoTargets() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Displace()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, List.of());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Displace");
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Displace()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID opponentBearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentBearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creatures returning together see each other's entry regardless of target order")
    void returningSoulWardenSeesOtherReturningCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SoulWarden());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Displace()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID wardenId = harness.getPermanentId(player1, "Soul Warden");
        harness.castAndResolveInstant(player1, 0, List.of(bearsId, wardenId));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Still flickers the remaining legal target when the other became a new object")
    void resolvesWithOneIllegalTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SoulWarden());
        harness.setHand(player1, List.of(new Displace(), new Displace()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID wardenId = harness.getPermanentId(player1, "Soul Warden");
        harness.castInstant(player1, 0, List.of(bearsId, wardenId));
        harness.castAndResolveInstant(player1, 0, wardenId);
        UUID newWardenId = harness.getPermanentId(player1, "Soul Warden");
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(bearsId);
        assertThat(harness.getPermanentId(player1, "Soul Warden")).isEqualTo(newWardenId);
        harness.assertInGraveyard(player1, "Displace");
    }

    @Test
    @DisplayName("Does not flicker a replacement permanent when its only target became a new object")
    void doesNotResolveWithAllTargetsIllegal() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Displace(), new Displace()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.castAndResolveInstant(player1, 0, bearsId);
        UUID newBearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isEqualTo(newBearsId);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Displace");
    }

    @Test
    @DisplayName("Returns an untapped new object without its previous counters")
    void clearsTappedStateAndCounters() {
        var bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.tap();
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new Displace()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        var returned = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(returned.getId()).isNotEqualTo(bears.getId());
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An exiled creature token does not return")
    void exilesTokenPermanently() {
        harness.setHand(player1, List.of(new EnlightenedManiac()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        UUID tokenId = harness.getPermanentId(player1, "Eldrazi Horror");
        harness.setHand(player1, List.of(new Displace()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0, tokenId);

        harness.assertNotOnBattlefield(player1, "Eldrazi Horror");
        harness.assertOnBattlefield(player1, "Enlightened Maniac");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot choose more than two creatures")
    void rejectsThreeTargets() {
        var first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        var second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        var third = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Displace()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
