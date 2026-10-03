package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PorcelainLegionnaire;
import com.github.laxika.magicalvibes.cards.s.ShrineOfLimitlessPower;
import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeastWithin.class, PorcelainLegionnaire.class, Forest.class,
        ShrineOfLimitlessPower.class, DarksteelRelic.class})
class BeastWithinTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creature and gives its controller a 3/3 Beast token")
    void destroysCreatureAndCreatesTokenForController() {
        harness.addToBattlefield(player2, new PorcelainLegionnaire());
        UUID targetId = harness.getPermanentId(player2, "Porcelain Legionnaire");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BeastWithin()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, targetId);

        // Target creature destroyed
        harness.assertNotOnBattlefield(player2, "Porcelain Legionnaire");
        harness.assertInGraveyard(player2, "Porcelain Legionnaire");

        // Opponent gets a 3/3 green Beast token
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Beast")
                        && p.getCard().isToken()
                        && p.getCard().hasType(CardType.CREATURE)
                        && p.getCard().getPower() == 3
                        && p.getCard().getToughness() == 3
                        && p.getCard().getColor() == CardColor.GREEN
                        && p.getCard().getSubtypes().contains(CardSubtype.BEAST));
    }

    @Test
    @DisplayName("Can target own permanent — controller gets the Beast token")
    void canDestroyOwnPermanent() {
        harness.addToBattlefield(player1, new PorcelainLegionnaire());
        UUID targetId = harness.getPermanentId(player1, "Porcelain Legionnaire");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BeastWithin()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, targetId);

        // Own creature destroyed
        harness.assertNotOnBattlefield(player1, "Porcelain Legionnaire");

        // Controller (player1) gets a 3/3 Beast token
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Beast")
                        && p.getCard().isToken()
                        && p.getCard().getPower() == 3
                        && p.getCard().getToughness() == 3);
    }

    @Test
    @DisplayName("Can destroy a land — target permanent is any permanent")
    void canDestroyLand() {
        harness.addToBattlefield(player2, new Forest());
        UUID targetId = harness.getPermanentId(player2, "Forest");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BeastWithin()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, targetId);

        // Land destroyed
        harness.assertNotOnBattlefield(player2, "Forest");

        // Opponent gets a Beast token
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Beast") && p.getCard().isToken());
    }

    @Test
    @DisplayName("Can destroy an artifact")
    void canDestroyArtifact() {
        harness.addToBattlefield(player2, new ShrineOfLimitlessPower());
        UUID targetId = harness.getPermanentId(player2, "Shrine of Limitless Power");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BeastWithin()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, targetId);

        // Artifact destroyed
        harness.assertNotOnBattlefield(player2, "Shrine of Limitless Power");

        // Opponent gets a Beast token
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Beast") && p.getCard().isToken());
    }

    @Test
    @DisplayName("Fizzles when target is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new PorcelainLegionnaire());
        UUID targetId = harness.getPermanentId(player2, "Porcelain Legionnaire");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BeastWithin()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(targetId));

        harness.passBothPriorities();

        // Spell fizzles — no Beast token created
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertNotOnBattlefield(player2, "Beast");
    }

    @Test
    @DisplayName("Goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new PorcelainLegionnaire());
        UUID targetId = harness.getPermanentId(player2, "Porcelain Legionnaire");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BeastWithin()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Beast Within");
    }

    @Test
    @DisplayName("An indestructible target survives and its controller still gets exactly one Beast")
    void indestructibleTargetStillCreatesToken() {
        var target = harness.addToBattlefieldAndReturn(player2, new DarksteelRelic());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BeastWithin()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Darksteel Relic");
        harness.assertNotInGraveyard(player2, "Darksteel Relic");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().isToken() && p.getCard().getName().equals("Beast"))
                .hasSize(1);
        harness.assertNotOnBattlefield(player1, "Beast");
    }

    @Test
    @DisplayName("Regeneration saves the target without preventing the Beast token")
    void regeneratedTargetStillCreatesToken() {
        var target = harness.addToBattlefieldAndReturn(player2, new PorcelainLegionnaire());
        target.setRegenerationShield(1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BeastWithin()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Porcelain Legionnaire");
        harness.assertNotInGraveyard(player2, "Porcelain Legionnaire");
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getRegenerationShield()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().isToken() && p.getCard().getName().equals("Beast"))
                .hasSize(1);
        harness.assertNotOnBattlefield(player1, "Beast");
    }

    @Test
    @DisplayName("The controller at resolution receives the token, while the owner receives the destroyed card")
    void usesControllerAtResolution() {
        var target = harness.addToBattlefieldAndReturn(player2, new PorcelainLegionnaire());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BeastWithin()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        gd.stolenCreatures.put(target.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Porcelain Legionnaire");
        harness.assertInGraveyard(player2, "Porcelain Legionnaire");
        harness.assertNotInGraveyard(player1, "Porcelain Legionnaire");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken() && p.getCard().getName().equals("Beast"))
                .hasSize(1);
        harness.assertNotOnBattlefield(player2, "Beast");
    }
}
