package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ConcertedCare;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BofurReliableGuardian.class, ConcertedCare.class, FountainOfYouth.class, GrizzlyBears.class})
class BofurReliableGuardianTest extends BaseCardTest {

    @Test
    void adventureProtectsTargetCreatureUntilEndOfTurnAndExilesTheCard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        BofurReliableGuardian card = new BofurReliableGuardian();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.hasKeyword(Keyword.HEXPROOF)).isTrue();
        assertThat(creature.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.findExiledCard(card.getId())).isNotNull();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.hasKeyword(Keyword.HEXPROOF)).isFalse();
        assertThat(creature.hasKeyword(Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void adventureCanProtectTargetArtifactYouControl() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        BofurReliableGuardian card = new BofurReliableGuardian();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.hasKeyword(Keyword.HEXPROOF)).isTrue();
        assertThat(artifact.hasKeyword(Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void adventureCannotTargetOpponentCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        BofurReliableGuardian card = new BofurReliableGuardian();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureFaceCanBeCastFromExileAfterAdventure() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        BofurReliableGuardian card = new BofurReliableGuardian();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bofur, Reliable Guardian");
        assertThat(gd.findExiledCard(card.getId())).isNull();
    }
}
