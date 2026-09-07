package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BringToLife;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnimatingFaerie.class, BringToLife.class, IronMyr.class, MindStone.class})
class AnimatingFaerieTest extends BaseCardTest {

    @Test
    void bringToLifeAnimatesControlledNoncreatureArtifactWithFourCounters() {
        Permanent mindStone = harness.addToBattlefieldAndReturn(player1, new MindStone());
        AnimatingFaerie card = new AnimatingFaerie();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, mindStone.getId());
        harness.passBothPriorities();

        assertThat(mindStone.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, mindStone)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mindStone)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mindStone)).isEqualTo(4);
        assertThat(mindStone.getCard().hasType(CardType.ARTIFACT)).isTrue();
    }

    @Test
    void bringToLifeCannotTargetArtifactCreatureOrOpponentsArtifact() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player1, new IronMyr());
        AnimatingFaerie firstCard = new AnimatingFaerie();
        harness.setHand(player1, List.of(firstCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, artifactCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        AnimatingFaerie secondCard = new AnimatingFaerie();
        harness.setHand(player1, List.of(secondCard));

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, opponentArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
