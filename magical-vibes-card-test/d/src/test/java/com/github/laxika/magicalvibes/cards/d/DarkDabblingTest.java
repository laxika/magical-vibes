package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GaeasRevenge;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarkDabbling.class, Forest.class, GrizzlyBears.class, RuneclawBear.class,
        Shock.class, LavaAxe.class, GaeasRevenge.class})
class DarkDabblingTest extends BaseCardTest {

    @Test
    @DisplayName("Regenerates the target creature and draws a card")
    void regeneratesTargetAndDraws() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));

        cast(player1, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(findPermanent(player1, "Grizzly Bears").getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Can regenerate a creature an opponent controls")
    void canTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));

        cast(player1, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(findPermanent(player2, "Grizzly Bears").getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Without spell mastery only the target is regenerated")
    void withoutSpellMasteryOnlyTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new Forest()));

        cast(player1, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(findPermanent(player1, "Grizzly Bears").getRegenerationShield()).isEqualTo(1);
        assertThat(findPermanent(player1, "Runeclaw Bear").getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Spell mastery also regenerates each other creature you control, but never twice the target")
    void spellMasteryRegeneratesOtherOwnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Shock(), new LavaAxe()));
        harness.setLibrary(player1, List.of(new Forest()));

        cast(player1, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(findPermanent(player1, "Grizzly Bears").getRegenerationShield()).isEqualTo(1);
        assertThat(findPermanent(player1, "Runeclaw Bear").getRegenerationShield()).isEqualTo(1);
        assertThat(findPermanent(player2, "Grizzly Bears").getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Spell mastery regenerates all your creatures when the target is an opponent's creature")
    void spellMasteryWithOpponentTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new RuneclawBear());
        harness.setGraveyard(player1, List.of(new Shock(), new LavaAxe()));
        harness.setLibrary(player1, List.of(new Forest()));

        cast(player1, harness.getPermanentId(player2, "Runeclaw Bear"));

        assertThat(findPermanent(player1, "Grizzly Bears").getRegenerationShield()).isEqualTo(1);
        assertThat(findPermanent(player2, "Runeclaw Bear").getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Two instants satisfy spell mastery")
    void twoInstantsSatisfySpellMastery() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setGraveyard(player1, List.of(new Shock(), new Shock()));
        harness.setLibrary(player1, List.of(new Forest()));

        cast(player1, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(findPermanent(player1, "Runeclaw Bear").getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Two sorceries satisfy spell mastery")
    void twoSorceriesSatisfySpellMastery() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setGraveyard(player1, List.of(new LavaAxe(), new LavaAxe()));
        harness.setLibrary(player1, List.of(new Forest()));

        cast(player1, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(findPermanent(player1, "Runeclaw Bear").getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Nonspells and the opponent's graveyard do not satisfy spell mastery")
    void onlyOwnInstantsAndSorceriesCount() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setGraveyard(player1, List.of(new Shock(), new Forest(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new Shock(), new LavaAxe()));
        harness.setLibrary(player1, List.of(new Forest()));

        cast(player1, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(findPermanent(player1, "Runeclaw Bear").getRegenerationShield()).isZero();
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Spell mastery is checked when the spell resolves")
    void spellMasteryChecksGraveyardAtResolution() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setGraveyard(player1, List.of(new LavaAxe()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new DarkDabbling()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));

        harness.setGraveyard(player1, List.of(new LavaAxe(), new Shock()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").getRegenerationShield()).isEqualTo(1);
        assertThat(findPermanent(player1, "Runeclaw Bear").getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("An illegal target prevents drawing and all spell mastery regeneration")
    void illegalTargetPreventsAllEffects() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.setGraveyard(player1, List.of(new Shock(), new LavaAxe()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new DarkDabbling()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Dark Dabbling");
        assertThat(findPermanent(player1, "Runeclaw Bear").getRegenerationShield()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Regeneration replaces lethal damage without tapping the creature early")
    void shieldProtectsAgainstLethalDamage() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        cast(player1, targetId);

        assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isFalse();
        harness.assertInHand(player1, "Forest");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, targetId);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Grizzly Bears").getMarkedDamage()).isZero();
        assertThat(findPermanent(player1, "Grizzly Bears").getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Spell mastery regenerates creatures that cannot be targeted by Dark Dabbling")
    void spellMasteryDoesNotTargetOtherCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GaeasRevenge());
        harness.addToBattlefield(player1, new Forest());
        harness.setGraveyard(player1, List.of(new Shock(), new LavaAxe()));
        harness.setLibrary(player1, List.of(new Forest()));

        cast(player1, harness.getPermanentId(player1, "Grizzly Bears"));

        assertThat(findPermanent(player1, "Gaea's Revenge").getRegenerationShield()).isEqualTo(1);
        assertThat(findPermanent(player1, "Forest").getRegenerationShield()).isZero();
    }

    private void cast(Player player, UUID targetId) {
        harness.setHand(player, List.of(new DarkDabbling()));
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player, 0, targetId);
    }
}
