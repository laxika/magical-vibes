package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BreakTies.class, FountainOfYouth.class, GloriousAnthem.class, GrizzlyBears.class})
class BreakTiesTest extends BaseCardTest {

    @Test
    void destroysTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        cast(0, artifact.getId());

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    void destroysTargetEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        cast(1, enchantment.getId());

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    void exilesTargetCardFromAnyGraveyard() {
        Card card = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(card));

        harness.setHand(player1, List.of(new BreakTies()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castModalInstantWithModes(player1, 0, 1, new int[]{2}, card.getId(), List.of());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void reinforcePutsCounterOnTargetCreatureAndDiscardsThisCard() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BreakTies()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Break Ties");
    }

    @Test
    void eachModeRejectsAnIllegalTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        prepareSpell();
        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of(enchantment.getId())))
                .isInstanceOf(IllegalStateException.class);

        prepareSpell();
        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);

        Card card = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(card));
        prepareSpell();
        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, new int[]{2}, creature.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int modeIndex, java.util.UUID targetId) {
        prepareSpell();
        if (modeIndex == 2) {
            harness.castModalInstantWithModes(player1, 0, 1, new int[]{modeIndex}, targetId, List.of());
        } else {
            harness.castModalInstant(player1, 0, modeIndex, List.of(targetId));
        }
        harness.passBothPriorities();
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new BreakTies()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
