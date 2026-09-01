package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AbundantGrowth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PreyUpon;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RingOfImmortals.class, AbundantGrowth.class, Forest.class, GrizzlyBears.class, PreyUpon.class, Shock.class})
class RingOfImmortalsTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an instant that targets a permanent you control")
    void countersInstantTargetingYourPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.addToBattlefield(player2, new RingOfImmortals());
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, targetId);
        harness.passPriority(player1);
        harness.activateAbility(player2, 1, null, shock.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Ring of Immortals");
    }

    @Test
    @DisplayName("Counters an Aura spell that targets a permanent you control")
    void countersAuraTargetingYourPermanent() {
        harness.addToBattlefield(player2, new Forest());
        UUID targetId = harness.getPermanentId(player2, "Forest");

        AbundantGrowth growth = new AbundantGrowth();
        harness.setHand(player1, List.of(growth));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.addToBattlefield(player2, new RingOfImmortals());
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, targetId);
        harness.passPriority(player1);
        harness.activateAbility(player2, 1, null, growth.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Abundant Growth");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player2, "Ring of Immortals");
    }

    @Test
    @DisplayName("Cannot target an instant that targets an opponent's permanent")
    void cannotTargetInstantTargetingOpponentsPermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.addToBattlefield(player2, new RingOfImmortals());
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, targetId);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a sorcery even when it targets a permanent you control")
    void cannotTargetSorcery() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID ownTargetId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID opponentTargetId = harness.getPermanentId(player2, "Grizzly Bears");

        PreyUpon preyUpon = new PreyUpon();
        harness.setHand(player1, List.of(preyUpon));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.addToBattlefield(player2, new RingOfImmortals());
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, List.of(ownTargetId, opponentTargetId));
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 1, null, preyUpon.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
