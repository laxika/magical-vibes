package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LayClaim;
import com.github.laxika.magicalvibes.cards.s.ScrollOfAvacyn;
import com.github.laxika.magicalvibes.cards.t.ThatcherRevolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Cloudshift.class, GrizzlyBears.class, LayClaim.class, CathedralSanctifier.class,
        ScrollOfAvacyn.class, ThatcherRevolt.class, Clone.class})
class CloudshiftTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles and immediately returns the targeted creature")
    void flickersOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Cloudshift()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castAndResolveInstant(player1, 0, bearsId);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        // It is a new object, so summoning sickness applies again.
        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Returned creature comes back under the spell's controller, not the owner")
    void returnsUnderYourControl() {
        // Lay Claim leaves the bears owned by player2 but controlled by player1. Stealing it with a
        // real Aura keeps the ownership record and the layer-2 control effect in step; writing only
        // gd.stolenCreatures by hand would let state-based actions hand the bears back to player2.
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new LayClaim()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castEnchantment(player1, 0, bearsId);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        harness.setHand(player1, List.of(new Cloudshift()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, bearsId);

        // The Aura falls off the flickered creature, but it still returns under player1's control.
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature you do not control")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Cloudshift()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID opponentBearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentBearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the target leaves the battlefield before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Cloudshift()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castInstant(player1, 0, bearsId);

        Permanent bearsPerm = gqs.findPermanentById(gd, bearsId);
        gd.playerBattlefields.get(player1.getId()).remove(bearsPerm);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnsAsNewUntappedObjectWithoutCounters() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new CathedralSanctifier());
        original.tap();
        original.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new Cloudshift()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, original.getId());

        Permanent returned = findPermanent(player1, "Cathedral Sanctifier");
        assertThat(returned.getId()).isNotEqualTo(original.getId());
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(returned.isSummoningSick()).isTrue();
    }

    @Test
    void returningCreatureTriggersItsEntersAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CathedralSanctifier());
        harness.setHand(player1, List.of(new Cloudshift()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotTargetNoncreaturePermanentYouControl() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ScrollOfAvacyn());
        harness.setHand(player1, List.of(new Cloudshift()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exiledTokenDoesNotReturn() {
        harness.castFromHand(player1, new ThatcherRevolt(), "{2}{R}");
        harness.passBothPriorities();
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).toList();
        assertThat(tokens).hasSize(3);
        UUID tokenId = tokens.getFirst().getId();
        harness.setHand(player1, List.of(new Cloudshift()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, tokenId);

        assertThat(gqs.findPermanentById(gd, tokenId)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void returningCloneMayChooseACreatureToCopyAgain() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bearsId);
        UUID cloneId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new Cloudshift()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, cloneId);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bearsId);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Clone");
        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(cloneId);
    }
}
