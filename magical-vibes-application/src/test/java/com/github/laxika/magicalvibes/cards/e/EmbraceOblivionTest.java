package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmbraceOblivion.class, GrizzlyBears.class, MindStone.class})
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
        Permanent target = harness.addToBattlefieldAndReturn(player2, spacecraft());

        castAndResolve(target, sacrificed);

        harness.assertInGraveyard(player1, "Mind Stone");
        harness.assertInGraveyard(player2, "Test Spacecraft");
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

    private void castAndResolve(Permanent target, Permanent sacrificed) {
        harness.setHand(player1, List.of(new EmbraceOblivion()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrificed.getId());
        harness.passBothPriorities();
    }

    private Card spacecraft() {
        Card card = new Card();
        card.setName("Test Spacecraft");
        card.setType(CardType.ARTIFACT);
        card.setSubtypes(List.of(CardSubtype.SPACECRAFT));
        return card;
    }
}
