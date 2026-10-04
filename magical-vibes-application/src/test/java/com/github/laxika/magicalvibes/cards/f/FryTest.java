package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.c.CloudkinSeer;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MuYanlingSkyDancer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Fry.class, Cancel.class, EliteVanguard.class, GrizzlyBears.class,
        MuYanlingSkyDancer.class, CloudkinSeer.class, Unsummon.class})
class FryTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage to a white creature")
    void dealsDamageToWhiteCreature() {
        harness.addToBattlefield(player2, new EliteVanguard());
        harness.setHand(player1, List.of(new Fry()));
        addFryMana();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Elite Vanguard"));

        harness.assertNotOnBattlefield(player2, "Elite Vanguard");
    }

    @Test
    @DisplayName("Deals 5 damage to a blue planeswalker")
    void dealsDamageToBluePlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new MuYanlingSkyDancer());
        planeswalker.setCounterCount(CounterType.LOYALTY, 7);
        harness.setHand(player1, List.of(new Fry()));
        addFryMana();

        harness.castAndResolveInstant(player1, 0, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature that is not white or blue")
    void cannotTargetNonWhiteOrBlueCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new EliteVanguard());
        harness.setHand(player1, List.of(new Fry()));
        addFryMana();

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be countered")
    void cannotBeCountered() {
        harness.addToBattlefield(player2, new EliteVanguard());
        Fry fry = new Fry();
        harness.setHand(player1, List.of(fry));
        addFryMana();
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Elite Vanguard"));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, fry.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Elite Vanguard");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Can target its controller's blue creature")
    void canTargetOwnBlueCreature() {
        harness.addToBattlefield(player1, new CloudkinSeer());
        harness.setHand(player1, List.of(new Fry()));
        addFryMana();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Cloudkin Seer"));

        harness.assertNotOnBattlefield(player1, "Cloudkin Seer");
        harness.assertInGraveyard(player1, "Cloudkin Seer");
    }

    @Test
    @DisplayName("Lethal damage removes a planeswalker")
    void lethalDamageRemovesPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new MuYanlingSkyDancer());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new Fry()));
        addFryMana();

        harness.castAndResolveInstant(player1, 0, planeswalker.getId());

        harness.assertNotOnBattlefield(player2, "Mu Yanling, Sky Dancer");
        harness.assertInGraveyard(player2, "Mu Yanling, Sky Dancer");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.addToBattlefield(player2, new CloudkinSeer());
        harness.setHand(player1, List.of(new Fry()));
        addFryMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not resolve when its only target leaves the battlefield")
    void doesNotResolveWhenTargetLeavesBattlefield() {
        harness.addToBattlefield(player2, new CloudkinSeer());
        var targetId = harness.getPermanentId(player2, "Cloudkin Seer");
        harness.setHand(player1, List.of(new Fry()));
        harness.setHand(player2, List.of(new Unsummon()));
        addFryMana();
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Cloudkin Seer");
        harness.assertNotInGraveyard(player2, "Cloudkin Seer");
        harness.assertInGraveyard(player1, "Fry");
        assertThat(gd.stack).isEmpty();
    }
    private void addFryMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
