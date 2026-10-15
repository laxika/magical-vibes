package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NantukoVigilante.class, FountainOfYouth.class, GloriousAnthem.class, GrizzlyBears.class,
        Juggernaut.class, Boomerang.class})
class NantukoVigilanteTest extends BaseCardTest {

    @Test
    void turningFaceUpCanDestroyAnArtifactOrEnchantment() {
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent opponentEnchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent vigilante = castFaceDown();

        turnFaceUp(vigilante);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(opponentArtifact.getId(), opponentEnchantment.getId(), ownArtifact.getId());
        harness.handlePermanentChosen(player1, ownArtifact.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Fountain of Youth");
        harness.assertOnBattlefield(player2, "Glorious Anthem");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
    }

    @Test
    void turningFaceUpHasNoTargetWhenNoArtifactsOrEnchantmentsExist() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent vigilante = castFaceDown();

        turnFaceUp(vigilante);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(vigilante.isFaceDown()).isFalse();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void turningFaceUpCanDestroyAnArtifactCreature() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player2, new Juggernaut());
        Permanent vigilante = castFaceDown();

        turnFaceUp(vigilante);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(artifactCreature.getId());
        harness.handlePermanentChosen(player1, artifactCreature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Juggernaut");
    }

    @Test
    void turningFaceUpDestroysAnEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        Permanent vigilante = castFaceDown();

        turnFaceUp(vigilante);
        harness.handlePermanentChosen(player1, enchantment.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
        harness.assertOnBattlefield(player1, "Nantuko Vigilante");
    }

    @Test
    void castingFaceUpDoesNotTriggerDestruction() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.castFromHand(player1, new NantukoVigilante(), "{3}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Nantuko Vigilante");
        harness.assertOnBattlefield(player2, "Fountain of Youth");
    }

    @Test
    void destructionResolvesAfterVigilanteLeavesTheBattlefield() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent vigilante = castFaceDown();
        turnFaceUp(vigilante);
        harness.handlePermanentChosen(player1, artifact.getId());

        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, vigilante.getId());
        harness.assertInHand(player1, "Nantuko Vigilante");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    void destructionDoesNotRetargetWhenItsTargetLeavesTheBattlefield() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new GloriousAnthem());
        Permanent vigilante = castFaceDown();
        turnFaceUp(vigilante);
        harness.handlePermanentChosen(player1, artifact.getId());

        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player2, "Fountain of Youth");
        harness.assertNotInGraveyard(player2, "Fountain of Youth");
        harness.assertOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    void castingFaceDownDoesNotTriggerDestruction() {
        harness.addToBattlefield(player2, new FountainOfYouth());

        Permanent vigilante = castFaceDown();

        assertThat(vigilante.isFaceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Fountain of Youth");
    }

    @Test
    void turningFaceUpRequiresGreenMana() {
        Permanent vigilante = castFaceDown();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.turnFaceUp(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(vigilante)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(vigilante.isFaceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new NantukoVigilante()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanent(player1, "Nantuko Vigilante");
    }

    private void turnFaceUp(Permanent vigilante) {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vigilante));
    }
}
