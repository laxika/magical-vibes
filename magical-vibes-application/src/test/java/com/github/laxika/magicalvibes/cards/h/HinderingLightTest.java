package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BranchingBolt;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KathariScreecher;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.o.ObeliskOfBant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HinderingLight.class, Shock.class, GrizzlyBears.class})
class HinderingLightTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell targeting you and draws a card")
    void countersSpellTargetingYouAndDraws() {
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.setHand(player1, List.of(new HinderingLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        // Player2 casts Shock targeting player1 (the Hindering Light caster)
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        // Player1 casts Hindering Light targeting Shock
        harness.castInstant(player1, 0, shock.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Shock countered
        harness.assertInGraveyard(player2, "Shock");
        // Player1 life untouched
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        // Player1 drew a card (hand emptied by casting, then drew 1)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Counters a spell targeting a permanent you control")
    void countersSpellTargetingYourPermanent() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.setHand(player1, List.of(new HinderingLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player2);

        harness.castInstant(player1, 0, shock.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a spell targeting the opponent player")
    void cannotTargetSpellTargetingOpponent() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock, new HinderingLight()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        // Player1 casts Shock targeting player2 (the opponent)
        harness.castInstant(player1, 0, player2.getId());

        // Hindering Light cannot target a spell aimed at the opponent
        assertThatThrownBy(() -> harness.castInstant(player1, 0, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a spell targeting an opponent's permanent")
    void cannotTargetSpellTargetingOpponentsPermanent() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player2, bears);

        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock, new HinderingLight()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, shock.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can counter your own spell targeting you")
    void countersOwnSpellTargetingYou() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock, new HinderingLight()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.castAndResolveInstant(player1, 0, shock.getId());

        harness.assertInGraveyard(player1, "Shock");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not draw if the targeted spell has already been countered")
    void doesNotDrawWhenTargetLeavesStack() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock, new HinderingLight(), new HinderingLight()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player1.getId());
        harness.castInstant(player1, 0, shock.getId());
        harness.castAndResolveInstant(player1, 0, shock.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Shock");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot target a spell with no targets")
    void cannotTargetUntargetedSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.castFromHand(player1, bears, "{1}{G}");
        harness.setHand(player1, List.of(new HinderingLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed({Naturalize.class, ObeliskOfBant.class})
    @DisplayName("Counters a spell targeting a noncreature permanent you control")
    void countersSpellTargetingYourArtifact() {
        harness.addToBattlefield(player1, new ObeliskOfBant());
        Naturalize naturalize = new Naturalize();
        harness.setHand(player2, List.of(naturalize));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new HinderingLight()));
        harness.setLibrary(player1, List.of(new HinderingLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Obelisk of Bant"));
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, naturalize.getId());

        harness.assertInGraveyard(player2, "Naturalize");
        harness.assertOnBattlefield(player1, "Obelisk of Bant");
        harness.assertInHand(player1, "Hindering Light");
    }

    @Test
    @CardUsed({BranchingBolt.class, KathariScreecher.class})
    @DisplayName("Counters the entire spell when only one of its targets is yours")
    void countersSpellWithTargetsControlledByBothPlayers() {
        harness.addToBattlefield(player1, new KathariScreecher());
        harness.addToBattlefield(player2, new GrizzlyBears());
        BranchingBolt bolt = new BranchingBolt();
        harness.setHand(player2, List.of(bolt));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new HinderingLight()));
        harness.setLibrary(player1, List.of(new HinderingLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);

        harness.castModalInstant(player2, 0, 2, List.of(
                harness.getPermanentId(player1, "Kathari Screecher"),
                harness.getPermanentId(player2, "Grizzly Bears")));
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, bolt.getId());

        harness.assertInGraveyard(player2, "Branching Bolt");
        harness.assertOnBattlefield(player1, "Kathari Screecher");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Hindering Light");
    }

    @Test
    @CardUsed({Naturalize.class, ObeliskOfBant.class})
    @DisplayName("Does not draw when the targeted spell no longer targets your permanent")
    void doesNotDrawWhenTargetedPermanentLeavesBattlefield() {
        harness.addToBattlefield(player1, new ObeliskOfBant());
        var artifactId = harness.getPermanentId(player1, "Obelisk of Bant");
        Naturalize naturalize = new Naturalize();
        harness.setHand(player2, List.of(naturalize));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new HinderingLight(), new Naturalize()));
        harness.setLibrary(player1, List.of(new HinderingLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, artifactId);
        harness.passPriority(player2);
        harness.castInstant(player1, 0, naturalize.getId());
        harness.castAndResolveInstant(player1, 0, artifactId);
        harness.assertInGraveyard(player1, "Obelisk of Bant");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Hindering Light");
        assertThat(gd.stack).hasSize(1);
    }
}
