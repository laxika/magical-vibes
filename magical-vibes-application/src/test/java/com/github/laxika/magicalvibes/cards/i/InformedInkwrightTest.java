package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InformedInkwright.class, HillGiant.class, Shock.class, Blaze.class})
class InformedInkwrightTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant that targets a creature creates a 1/1 flying Inkling token")
    void reparteeCreatesToken() {
        harness.addToBattlefield(player1, new InformedInkwright());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        harness.castAndResolveInstant(player1, 0, giantId);
        harness.passBothPriorities();

        Permanent inkling = findPermanent(player1, "Inkling");
        assertThat(gqs.getEffectivePower(gd, inkling)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, inkling)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, inkling, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Casting a spell that targets a player does not trigger Repartee")
    void doesNotTriggerWhenTargetingPlayer() {
        harness.addToBattlefield(player1, new InformedInkwright());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
        harness.assertNotOnBattlefield(player1, "Inkling");
    }

    @Test
    void sorceryTargetingOpponentsCreatureCreatesOneTokenBeforeResolving() {
        harness.addToBattlefield(player1, new InformedInkwright());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 1, giant.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Inkling"))).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Inkling");
        harness.passBothPriorities();
    }

    @Test
    void opponentsInstantDoesNotTriggerRepartee() {
        harness.addToBattlefield(player1, new InformedInkwright());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, giant.getId());

        harness.assertNotOnBattlefield(player1, "Inkling");
        harness.assertNotOnBattlefield(player2, "Inkling");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void triggerStillCreatesTokenAfterSourceIsDestroyed() {
        Permanent inkwright = harness.addToBattlefieldAndReturn(player1, new InformedInkwright());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player1, 0, giant.getId());
        harness.castAndResolveInstant(player2, 0, inkwright.getId());
        harness.assertInGraveyard(player1, "Informed Inkwright");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Inkling");
        harness.passBothPriorities();
    }
}
