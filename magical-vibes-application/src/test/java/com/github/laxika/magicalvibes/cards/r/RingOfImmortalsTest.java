package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AlabasterPotion;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GiantStrength;
import com.github.laxika.magicalvibes.cards.p.PsychicPurge;
import com.github.laxika.magicalvibes.cards.t.TundraWolves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RingOfImmortals.class, AlabasterPotion.class, Boomerang.class, GiantStrength.class,
        PsychicPurge.class, TundraWolves.class})
class RingOfImmortalsTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an instant that targets a permanent you control")
    void countersInstantTargetingYourPermanent() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new TundraWolves()).getId();

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.addToBattlefield(player2, new RingOfImmortals());
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, targetId);
        harness.passPriority(player1);
        harness.activateAbility(player2, 1, null, boomerang.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Boomerang");
        harness.assertOnBattlefield(player2, "Tundra Wolves");
        harness.assertOnBattlefield(player2, "Ring of Immortals");
        assertThat(findPermanent(player2, "Ring of Immortals").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Counters an Aura spell that targets a permanent you control")
    void countersAuraTargetingYourPermanent() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new TundraWolves()).getId();

        GiantStrength giantStrength = new GiantStrength();
        harness.setHand(player1, List.of(giantStrength));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.addToBattlefield(player2, new RingOfImmortals());
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, targetId);
        harness.passPriority(player1);
        harness.activateAbility(player2, 1, null, giantStrength.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Giant Strength");
        harness.assertOnBattlefield(player2, "Tundra Wolves");
        harness.assertOnBattlefield(player2, "Ring of Immortals");
    }

    @Test
    @DisplayName("Cannot target an instant that targets an opponent's permanent")
    void cannotTargetInstantTargetingOpponentsPermanent() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new TundraWolves()).getId();

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.addToBattlefield(player2, new RingOfImmortals());
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, targetId);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, boomerang.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a sorcery even when it targets a permanent you control")
    void cannotTargetSorcery() {
        UUID ownTargetId = harness.addToBattlefieldAndReturn(player2, new TundraWolves()).getId();

        PsychicPurge psychicPurge = new PsychicPurge();
        harness.setHand(player1, List.of(psychicPurge));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.addToBattlefield(player2, new RingOfImmortals());
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, ownTargetId);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 1, null, psychicPurge.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an instant that targets a player")
    void cannotTargetInstantTargetingPlayer() {
        AlabasterPotion potion = new AlabasterPotion();
        harness.setHand(player1, List.of(potion));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.addToBattlefield(player2, new RingOfImmortals());
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castModalInstantForX(player1, 0, 0, 0, player2.getId());
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, potion.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
