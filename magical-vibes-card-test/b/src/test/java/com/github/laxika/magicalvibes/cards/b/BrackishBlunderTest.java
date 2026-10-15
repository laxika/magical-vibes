package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CaptainStormCosmiumRaider;
import com.github.laxika.magicalvibes.cards.s.SpyglassSiren;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrackishBlunder.class, GrizzlyBears.class, Forest.class,
        CaptainStormCosmiumRaider.class, SpyglassSiren.class})
class BrackishBlunderTest extends BaseCardTest {

    @Test
    @DisplayName("Returns an untapped creature without creating a Map")
    void returnsUntappedCreatureWithoutMap() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(target);

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Map")).isEmpty();
    }

    @Test
    @DisplayName("Returns a tapped creature and creates a Map")
    void returnsTappedCreatureAndCreatesMap() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();

        cast(target);

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Map")).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new BrackishBlunder()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bouncedCaptainDoesNotSeeMapEnter() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new CaptainStormCosmiumRaider());
        harness.addToBattlefield(player1, new SpyglassSiren());
        captain.tap();

        cast(captain);

        harness.assertInHand(player1, "Captain Storm, Cosmium Raider");
        harness.assertNotOnBattlefield(player1, "Captain Storm, Cosmium Raider");
        assertThat(findPermanents(player1, "Map")).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void createsMapWhenCreatureBecomesTappedBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpyglassSiren());
        prepareCast();
        harness.castInstant(player1, 0, target.getId());
        target.tap();

        harness.passBothPriorities();

        harness.assertInHand(player2, "Spyglass Siren");
        harness.assertNotOnBattlefield(player2, "Spyglass Siren");
        assertThat(findPermanents(player1, "Map")).hasSize(1);
        assertThat(findPermanents(player2, "Map")).isEmpty();
    }

    @Test
    void doesNotCreateMapWhenCreatureBecomesUntappedBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpyglassSiren());
        target.tap();
        prepareCast();
        harness.castInstant(player1, 0, target.getId());
        target.untap();

        harness.passBothPriorities();

        harness.assertInHand(player2, "Spyglass Siren");
        assertThat(findPermanents(player1, "Map")).isEmpty();
    }

    @Test
    void doesNotCreateMapWhenTargetHasAlreadyLeftBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpyglassSiren());
        target.tap();
        prepareCast();
        harness.setHand(player1, List.of(new BrackishBlunder(), new BrackishBlunder()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(findPermanents(player1, "Map")).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player2, "Spyglass Siren");
        assertThat(findPermanents(player1, "Map")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void createdMapCanBeSacrificedToExplore() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpyglassSiren());
        target.tap();
        Permanent explorer = harness.addToBattlefieldAndReturn(player1, new SpyglassSiren());
        harness.setLibrary(player1, List.of(new Forest()));
        cast(target);
        Permanent map = findPermanents(player1, "Map").getFirst();
        int mapIndex = gd.playerBattlefields.get(player1.getId()).indexOf(map);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, mapIndex, null, explorer.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(findPermanents(player1, "Map")).isEmpty();
        harness.assertOnBattlefield(player1, "Spyglass Siren");
    }

    private void cast(Permanent target) {
        prepareCast();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void prepareCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BrackishBlunder()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
