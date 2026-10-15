package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.s.SusanForeman;
import com.github.laxika.magicalvibes.cards.t.TheMoment;
import com.github.laxika.magicalvibes.cards.t.TheWarGames;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BanishToAnotherUniverse.class, AlphaMyr.class, Forest.class, GrizzlyBears.class,
        Naturalize.class, SolRing.class, SusanForeman.class, TheMoment.class, TheWarGames.class})
class BanishToAnotherUniverseTest extends BaseCardTest {

    @Test
    @DisplayName("Affinity for historic permanents reduces the generic mana cost")
    void affinityForHistoricPermanentsReducesGenericCost() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new AlphaMyr());
        }
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new BanishToAnotherUniverse()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bearsId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("ETB exiles target nonland permanent an opponent controls")
    void etbExilesOpponentPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolve(bearsId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Exiled card returns when Banish to Another Universe is destroyed")
    void exiledCardReturnsWhenSourceDestroyed() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolve(bearsId);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID sourceId = harness.getPermanentId(player1, "Banish to Another Universe");
        harness.castAndResolveInstant(player2, 0, sourceId);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        UUID forestId = harness.getPermanentId(player2, "Forest");
        setUpCast();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a permanent the caster controls")
    void cannotTargetOwnPermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID ownBearsId = harness.getPermanentId(player1, "Grizzly Bears");
        setUpCast();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, ownBearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Legendary permanents and Sagas both reduce the cost")
    void legendaryPermanentsAndSagasReduceCost() {
        harness.addToBattlefield(player1, new SusanForeman());
        harness.addToBattlefield(player1, new TheWarGames());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new SolRing()).getId();
        harness.setHand(player1, List.of(new BanishToAnotherUniverse()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A legendary artifact counts only once for affinity")
    void legendaryArtifactCountsOnlyOnce() {
        harness.addToBattlefield(player1, new TheMoment());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new SolRing()).getId();
        harness.setHand(player1, List.of(new BanishToAnotherUniverse()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Opponent historic permanents and ordinary lands do not reduce the cost")
    void ignoresOpponentHistoricPermanentsAndNonhistoricPermanents() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new SusanForeman());
        harness.addToBattlefield(player2, new TheWarGames());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new SolRing()).getId();
        setUpCast();

        harness.castEnchantment(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Affinity cannot remove the white mana requirement")
    void affinityDoesNotReduceColoredCost() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new SolRing());
        }
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new SolRing()).getId();
        harness.setHand(player1, List.of(new BanishToAnotherUniverse()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Banish to Another Universe");
    }

    @Test
    @DisplayName("ETB can exile a noncreature artifact")
    void exilesNoncreatureArtifact() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new SolRing()).getId();

        castAndResolve(targetId);

        harness.assertNotOnBattlefield(player2, "Sol Ring");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Sol Ring"));
    }

    @Test
    @DisplayName("Removing the source before its ETB resolves prevents exile")
    void sourceLeavesBeforeTriggerResolves() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new SolRing()).getId();
        setUpCast();
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();
        UUID sourceId = harness.getPermanentId(player1, "Banish to Another Universe");
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player2, 0, sourceId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Banish to Another Universe");
        harness.assertOnBattlefield(player2, "Sol Ring");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The original ETB does nothing when the same card has left and returned")
    void originalTriggerDoesNotUseReturnedSource() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new SolRing()).getId();
        BanishToAnotherUniverse banish = new BanishToAnotherUniverse();
        harness.setHand(player1, List.of(banish));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();
        var originalSource = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, originalSource));
        harness.setHand(player1, List.of());
        var returnedSource = harness.addToBattlefieldAndReturn(player1, banish);
        assertThat(returnedSource.getId()).isNotEqualTo(originalSource.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Sol Ring");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    private void setUpCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BanishToAnotherUniverse()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void castAndResolve(UUID targetId) {
        setUpCast();
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
