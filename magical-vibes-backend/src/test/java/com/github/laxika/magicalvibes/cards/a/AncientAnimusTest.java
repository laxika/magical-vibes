package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MirriCatWarrior;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AncientAnimusTest extends BaseCardTest {

    @Test
    @DisplayName("Legendary creature gets +1/+1 counter before fighting")
    void legendaryCreatureGetsCounterBeforeFighting() {
        // Mirri, Cat Warrior (2/3 legendary) fights Llanowar Elves (1/1)
        // Mirri gets +1/+1 counter -> becomes 3/4, fights Elves
        // Mirri deals 3 to Elves (lethal), Elves deal 1 to Mirri (survives)
        harness.addToBattlefield(player1, new MirriCatWarrior());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new AncientAnimus()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID mirriId = harness.getPermanentId(player1, "Mirri, Cat Warrior");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, List.of(mirriId, elvesId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Mirri, Cat Warrior");

        Permanent mirri = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(mirri.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Non-legendary creature does NOT get +1/+1 counter")
    void nonLegendaryCreatureDoesNotGetCounter() {
        // Grizzly Bears (2/2 non-legendary) fights Llanowar Elves (1/1)
        // No counter — Bears stay 2/2, fight normally
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new AncientAnimus()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, List.of(bearId, elvesId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getPlusOnePlusOneCounters()).isZero();
    }

    @Test
    @DisplayName("Legendary counter helps creature survive fight it would otherwise lose")
    void legendaryCounterHelpsCreatureSurvive() {
        // Mirri (2/3 legendary) fights Hill Giant (3/3)
        // Without counter: Mirri deals 2 (Hill Giant survives), Hill Giant deals 3 (Mirri dies)
        // With counter: Mirri becomes 3/4, deals 3 (Hill Giant dies), Hill Giant deals 3 (Mirri survives at 1)
        harness.addToBattlefield(player1, new MirriCatWarrior());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new AncientAnimus()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID mirriId = harness.getPermanentId(player1, "Mirri, Cat Warrior");
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, List.of(mirriId, giantId));
        harness.passBothPriorities();

        // Both should trade: Mirri 3/4 with counter, takes 3 damage -> survives at 1
        // Hill Giant 3/3, takes 3 damage -> dies
        harness.assertOnBattlefield(player1, "Mirri, Cat Warrior");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Non-legendary creature fights and both die with equal stats")
    void nonLegendaryFightBothDie() {
        // Grizzly Bears (2/2 non-legendary) fights Grizzly Bears (2/2) — both die
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AncientAnimus()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID myBearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID theirBearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, List.of(myBearId, theirBearId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target opponent's creature as first target")
    void cannotTargetOpponentCreatureAsFirstTarget() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new AncientAnimus()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID theirBearId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID theirElvesId = harness.getPermanentId(player2, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(theirBearId, theirElvesId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("Cannot target own creature as second target")
    void cannotTargetOwnCreatureAsSecondTarget() {
        GrizzlyBears bear1 = new GrizzlyBears();
        GrizzlyBears bear2 = new GrizzlyBears();
        harness.addToBattlefield(player1, bear1);
        harness.addToBattlefield(player1, bear2);
        harness.setHand(player1, List.of(new AncientAnimus()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        UUID id1 = bf.get(0).getId();
        UUID id2 = bf.get(1).getId();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(id1, id2)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Neither creature fights when first target removed before resolution (701.14b)")
    void neitherFightsWhenFirstTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new AncientAnimus()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, List.of(bearId, elvesId));

        // Remove first target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        // Llanowar Elves should survive — neither creature fights per 701.14b
        harness.assertOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Neither creature fights when second target removed before resolution (701.14b)")
    void neitherFightsWhenSecondTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new AncientAnimus()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castInstant(player1, 0, List.of(bearId, elvesId));

        // Remove second target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        // Grizzly Bears should survive undamaged
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getMarkedDamage()).isZero();
    }
}
