package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoltenCollapse.class, FountainOfYouth.class, GrizzlyBears.class})
class MoltenCollapseTest extends BaseCardTest {

    @Test
    void destroysTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MoltenCollapse()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0}, List.of(creature.getId()), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void destroysTargetSmallNoncreatureNonlandPermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new MoltenCollapse()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{1}, List.of(artifact.getId()), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    void cannotChooseBothWithoutHavingDescended() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new MoltenCollapse()));
        addMana();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 1, 2,
                new int[]{0, 1}, List.of(creature.getId(), artifact.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void havingDescendedAllowsBothModes() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        gd.playersWhoDescendedThisTurn.add(player1.getId());
        harness.setHand(player1, List.of(new MoltenCollapse()));
        addMana();

        harness.castModalSorceryWithModes(player1, 0, 1, 2,
                new int[]{0, 1}, List.of(creature.getId(), artifact.getId()), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
