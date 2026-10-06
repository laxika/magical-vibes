package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AimHigh;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GreaterWerewolf;
import com.github.laxika.magicalvibes.cards.h.HowlpackWolf;
import com.github.laxika.magicalvibes.cards.r.RabidBite;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilverfurPartisan.class, GiantGrowth.class, GrizzlyBears.class,
        GreaterWerewolf.class, HowlpackWolf.class, AimHigh.class, RabidBite.class, TurnToFrog.class})
class SilverfurPartisanTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Wolf token when a Wolf becomes the target of an instant")
    void createsTokenForTargetedWolf() {
        harness.addToBattlefield(player1, new SilverfurPartisan());
        harness.addToBattlefield(player1, new HowlpackWolf());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Howlpack Wolf"));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Wolf")).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates a Wolf token when a Werewolf becomes the target of an instant")
    void createsTokenForTargetedWerewolf() {
        harness.addToBattlefield(player1, new SilverfurPartisan());
        harness.addToBattlefield(player1, new GreaterWerewolf());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Greater Werewolf"));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Wolf")).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not create a token when a non-Wolf creature becomes the target")
    void doesNotCreateTokenForOtherCreature() {
        harness.addToBattlefield(player1, new SilverfurPartisan());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(countPermanents(player1, "Wolf")).isZero();
    }

    @Test
    void triggersForItselfBeforeTheTargetingSpellResolves() {
        harness.addToBattlefield(player1, new SilverfurPartisan());
        harness.setHand(player1, List.of(new AimHigh()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Silverfur Partisan"));

        assertThat(countPermanents(player1, "Wolf")).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
    }

    @Test
    void triggersForOpponentSpellButNotOpponentWolf() {
        harness.addToBattlefield(player1, new SilverfurPartisan());
        harness.addToBattlefield(player1, new HowlpackWolf());
        harness.addToBattlefield(player2, new HowlpackWolf());
        harness.setHand(player2, List.of(new AimHigh(), new AimHigh()));
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Howlpack Wolf"));
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Wolf")).isEqualTo(1);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player2, "Howlpack Wolf"));
        assertThat(countPermanents(player1, "Wolf")).isEqualTo(1);
    }

    @Test
    void sorceryTriggersOnlyForTheWolfItsControllerControls() {
        harness.addToBattlefield(player1, new SilverfurPartisan());
        harness.addToBattlefield(player1, new HowlpackWolf());
        harness.addToBattlefield(player2, new HowlpackWolf());
        harness.setHand(player1, List.of(new RabidBite()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, List.of(
                harness.getPermanentId(player1, "Howlpack Wolf"),
                harness.getPermanentId(player2, "Howlpack Wolf")));

        assertThat(countPermanents(player1, "Wolf")).isEqualTo(1);
        harness.passBothPriorities();
    }

    @Test
    void doesNotTriggerAfterLosingAllAbilities() {
        harness.addToBattlefield(player1, new SilverfurPartisan());
        harness.addToBattlefield(player1, new HowlpackWolf());
        harness.setHand(player1, List.of(new TurnToFrog(), new AimHigh()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Silverfur Partisan"));
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Wolf")).isEqualTo(1);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Howlpack Wolf"));

        assertThat(countPermanents(player1, "Wolf")).isEqualTo(1);
    }
}
