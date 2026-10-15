package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AdantoVanguard;
import com.github.laxika.magicalvibes.cards.a.ArguelsBloodFast;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Manalith;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrandAbolisher.class, Shock.class, AdantoVanguard.class, LlanowarElves.class,
        Forest.class, Manalith.class, TurnToFrog.class, ArguelsBloodFast.class})
class GrandAbolisherTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent can't cast spells during the controller's turn")
    void opponentCantCastDuringControllersTurn() {
        harness.addToBattlefield(player1, new GrandAbolisher());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player2.getId())).isEmpty();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponent can cast spells during their own turn")
    void opponentCanCastOnOwnTurn() {
        harness.addToBattlefield(player1, new GrandAbolisher());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player2.getId())).contains(0);
    }

    @Test
    @DisplayName("Controller is not restricted by their own Grand Abolisher")
    void controllerCanCastOnOwnTurn() {
        harness.addToBattlefield(player1, new GrandAbolisher());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        GameActionAvailabilityService availability = harness.getGameActionAvailabilityService();
        assertThat(availability.getPlayableCardIndices(gd, player1.getId())).contains(0);
    }

    @Test
    @DisplayName("Opponent can't activate a creature's ability during the controller's turn")
    void opponentCantActivateCreatureAbility() {
        harness.addToBattlefield(player1, new GrandAbolisher());
        Permanent vanguard = harness.addToBattlefieldAndReturn(player2, new AdantoVanguard());
        vanguard.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifacts, creatures");
    }

    @Test
    @DisplayName("Opponent can't tap a creature for mana during the controller's turn")
    void opponentCantTapManaCreature() {
        harness.addToBattlefield(player1, new GrandAbolisher());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        elves.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.tapPermanent(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifacts, creatures");
        assertThat(elves.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent can still tap lands for mana during the controller's turn")
    void opponentCanStillTapLands() {
        harness.addToBattlefield(player1, new GrandAbolisher());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.tapPermanent(player2, 0);

        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent can activate creature abilities on their own turn")
    void opponentCanActivateOnOwnTurn() {
        harness.addToBattlefield(player1, new GrandAbolisher());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        elves.setSummoningSick(false);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.tapPermanent(player2, 0);

        assertThat(elves.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent can't activate an artifact mana ability during the controller's turn")
    void opponentCantActivateArtifactManaAbility() {
        harness.addToBattlefield(player1, new GrandAbolisher());
        Permanent manalith = harness.addToBattlefieldAndReturn(player2, new Manalith());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifacts, creatures");
        assertThat(manalith.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent can't activate an enchantment ability during the controller's turn")
    void opponentCantActivateEnchantmentAbility() {
        harness.addToBattlefield(player1, new GrandAbolisher());
        harness.addToBattlefield(player2, new ArguelsBloodFast());
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifacts, creatures");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A spell already on the stack resolves after Grand Abolisher enters")
    void alreadyCastSpellStillResolves() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());

        harness.addToBattlefield(player1, new GrandAbolisher());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent can cast again as soon as Grand Abolisher leaves the battlefield")
    void restrictionEndsWhenAbolisherDies() {
        Permanent abolisher = harness.addToBattlefieldAndReturn(player1, new GrandAbolisher());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, abolisher.getId());
        harness.assertInGraveyard(player1, "Grand Abolisher");
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Removing Grand Abolisher's abilities permits opponents to cast and activate")
    void restrictionEndsWhenAbolisherLosesAbilities() {
        Permanent abolisher = harness.addToBattlefieldAndReturn(player1, new GrandAbolisher());
        Permanent elves = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        elves.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, abolisher.getId());
        harness.tapPermanent(player2, 0);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(elves.isTapped()).isTrue();
        harness.assertLife(player1, 18);
    }
}
