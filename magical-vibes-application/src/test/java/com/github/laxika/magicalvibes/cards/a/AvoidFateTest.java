package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.c.ChainLightning;
import com.github.laxika.magicalvibes.cards.g.GiantStrength;
import com.github.laxika.magicalvibes.cards.k.KoboldsOfKherKeep;
import com.github.laxika.magicalvibes.cards.m.ManaDrain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvoidFate.class, Boomerang.class, ChainLightning.class, GiantStrength.class,
        KoboldsOfKherKeep.class, ManaDrain.class})
class AvoidFateTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an instant that targets a permanent you control")
    void countersInstantTargetingYourPermanent() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new KoboldsOfKherKeep()).getId();

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new AvoidFate()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, boomerang.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Boomerang");
        harness.assertOnBattlefield(player2, "Kobolds of Kher Keep");
        harness.assertInGraveyard(player2, "Avoid Fate");
    }

    @Test
    @DisplayName("Counters an Aura spell that targets a permanent you control")
    void countersAuraTargetingYourPermanent() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new KoboldsOfKherKeep()).getId();

        GiantStrength giantStrength = new GiantStrength();
        harness.setHand(player1, List.of(giantStrength));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.setHand(player2, List.of(new AvoidFate()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, targetId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, giantStrength.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Giant Strength");
        harness.assertOnBattlefield(player2, "Kobolds of Kher Keep");
        harness.assertInGraveyard(player2, "Avoid Fate");
    }

    @Test
    @DisplayName("Cannot target an instant that targets an opponent's permanent")
    void cannotTargetInstantTargetingOpponentsPermanent() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new KoboldsOfKherKeep()).getId();

        Boomerang boomerang = new Boomerang();
        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.setHand(player2, List.of(new AvoidFate()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, boomerang.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a sorcery even when it targets a permanent you control")
    void cannotTargetSorcery() {
        UUID opponentTargetId = harness.addToBattlefieldAndReturn(player2, new KoboldsOfKherKeep()).getId();

        ChainLightning chainLightning = new ChainLightning();
        harness.setHand(player1, List.of(chainLightning));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.setHand(player2, List.of(new AvoidFate()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, List.of(opponentTargetId));
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, chainLightning.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an instant that targets a spell instead of a permanent")
    void cannotTargetInstantTargetingSpell() {
        ChainLightning chainLightning = new ChainLightning();
        harness.setHand(player1, List.of(chainLightning, new AvoidFate()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        ManaDrain manaDrain = new ManaDrain();
        harness.setHand(player2, List.of(manaDrain));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, chainLightning.getId());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, manaDrain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
