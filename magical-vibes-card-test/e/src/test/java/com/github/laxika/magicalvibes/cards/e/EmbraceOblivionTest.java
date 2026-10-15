package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.s.SpecimenFreighter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmbraceOblivion.class, GrizzlyBears.class, MindStone.class, SpecimenFreighter.class})
class EmbraceOblivionTest extends BaseCardTest {

    @Test
    void sacrificesCreatureAndDestroysTargetCreature() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolve(target, sacrificed);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void sacrificesArtifactAndDestroysTargetSpacecraft() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new MindStone());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpecimenFreighter());

        castAndResolve(target, sacrificed);

        harness.assertInGraveyard(player1, "Mind Stone");
        harness.assertInGraveyard(player2, "Specimen Freighter");
    }

    @Test
    void cannotTargetPermanentThatIsNeitherCreatureNorSpacecraft() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MindStone());
        harness.setHand(player1, List.of(new EmbraceOblivion()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, target.getId(), sacrificed.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or Spacecraft");
    }

    @Test
    void sacrificeIsPaidBeforeTargetIsDestroyed() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new MindStone());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EmbraceOblivion()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrificed.getId());

        harness.assertInGraveyard(player1, "Mind Stone");
        harness.assertNotOnBattlefield(player1, "Mind Stone");
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Embrace Oblivion");
    }

    @Test
    void canSacrificeTheTargetCreatureAsTheAdditionalCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castAndResolve(target, target);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Embrace Oblivion");
    }

    @Test
    void cannotCastWithoutPayingTheSacrificeCost() {
        harness.addToBattlefield(player1, new MindStone());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EmbraceOblivion()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must sacrifice");

        harness.assertInHand(player1, "Embrace Oblivion");
        harness.assertOnBattlefield(player1, "Mind Stone");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void cannotSacrificeAnOpponentsPermanent() {
        harness.addToBattlefield(player1, new MindStone());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new MindStone());
        harness.setHand(player1, List.of(new EmbraceOblivion()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(
                player1, 0, target.getId(), sacrifice.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Can only sacrifice permanents you control");

        harness.assertInHand(player1, "Embrace Oblivion");
        harness.assertOnBattlefield(player2, "Mind Stone");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void canSacrificeANoncreatureSpacecraftToDestroyOwnCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new SpecimenFreighter());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castAndResolve(target, sacrifice);

        harness.assertInGraveyard(player1, "Specimen Freighter");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Embrace Oblivion");
    }
    private void castAndResolve(Permanent target, Permanent sacrificed) {
        harness.setHand(player1, List.of(new EmbraceOblivion()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrificed.getId());
        harness.passBothPriorities();
    }

}
