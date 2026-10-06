package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RunAwayTogether.class, GrizzlyBears.class, LlanowarElves.class, RayOfCommand.class, Unsummon.class})
class RunAwayTogetherTest extends BaseCardTest {

    @Test
    @DisplayName("Returns two creatures controlled by different players to their owners' hands")
    void bouncesCreaturesControlledByDifferentPlayers() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new RunAwayTogether()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");
        harness.castAndResolveInstant(player1, 0, List.of(bearId, elvesId));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInHand(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot target two creatures controlled by the same player")
    void cannotTargetCreaturesControlledBySamePlayer() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new RunAwayTogether()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID firstId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID secondId = harness.getPermanentId(player1, "Llanowar Elves");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(firstId, secondId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires two targets even when only one creature is available")
    void cannotCastWithOnlyOneTarget() {
        UUID bearId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new RunAwayTogether()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bearId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not resolve when both targets become controlled by the same player")
    void doesNotBounceWhenTargetsGainSameController() {
        UUID bearId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        UUID elvesId = harness.addToBattlefieldAndReturn(player2, new LlanowarElves()).getId();
        harness.setHand(player1, List.of(new RunAwayTogether(), new RayOfCommand()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castInstant(player1, 0, List.of(bearId, elvesId));
        harness.castAndResolveInstant(player1, 0, elvesId);
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertNotInHand(player2, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Run Away Together");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Returns the remaining legal creature when either target leaves the battlefield")
    void returnsRemainingCreatureWhenOneTargetLeaves(int departingTargetIndex) {
        UUID bearId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        UUID elvesId = harness.addToBattlefieldAndReturn(player2, new LlanowarElves()).getId();
        List<UUID> targets = List.of(bearId, elvesId);
        harness.setHand(player1, List.of(new RunAwayTogether(), new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, targets);
        harness.castAndResolveInstant(player1, 0, targets.get(departingTargetIndex));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Run Away Together");
    }

    @Test
    @DisplayName("Uses a departed target's last controller to reject the remaining target")
    void doesNotBounceWhenDepartedTargetLastSharedController() {
        UUID bearId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        UUID elvesId = harness.addToBattlefieldAndReturn(player2, new LlanowarElves()).getId();
        harness.setHand(player1, List.of(new RunAwayTogether(), new RayOfCommand(), new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castInstant(player1, 0, List.of(bearId, elvesId));
        harness.castAndResolveInstant(player1, 0, elvesId);
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        harness.castAndResolveInstant(player1, 0, elvesId);
        harness.assertInHand(player2, "Llanowar Elves");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Run Away Together");
    }

    @Test
    @DisplayName("Returns creatures to their owners even when both cards have the same owner")
    void returnsStolenCreatureToOwner() {
        GrizzlyBears bear = new GrizzlyBears();
        bear.setOwnerId(player2.getId());
        Permanent stolenBear = harness.addToBattlefieldAndReturn(player1, bear);
        gd.stolenCreatures.put(stolenBear.getId(), player2.getId());
        UUID elvesId = harness.addToBattlefieldAndReturn(player2, new LlanowarElves()).getId();
        harness.setHand(player1, List.of(new RunAwayTogether()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, List.of(stolenBear.getId(), elvesId));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Llanowar Elves");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }
}
