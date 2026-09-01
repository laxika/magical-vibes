package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BubbleUp;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AquaticAlchemistBubbleUp.class, BubbleUp.class, GrizzlyBears.class, Shock.class})
class AquaticAlchemistBubbleUpTest extends BaseCardTest {

    @Test
    void adventurePutsTargetInstantOrSorceryOnTopOfLibraryAndExilesCard() {
        AquaticAlchemistBubbleUp card = new AquaticAlchemistBubbleUp();
        Card target = new Shock();
        Card oldTop = new GrizzlyBears();
        harness.setHand(player1, List.of(card));
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(oldTop));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(target, oldTop);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void adventureCannotTargetCreatureCard() {
        AquaticAlchemistBubbleUp card = new AquaticAlchemistBubbleUp();
        Card target = new GrizzlyBears();
        harness.setHand(player1, List.of(card));
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void boostsOnlyTheFirstInstantOrSorcerySpellEachTurn() {
        Permanent alchemist = harness.addToBattlefieldAndReturn(player1, new AquaticAlchemistBubbleUp());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).filteredOn(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .hasSize(1);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(alchemist.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(alchemist.getPowerModifier()).isZero();
    }

    @Test
    void creatureSpellDoesNotConsumeTheFirstInstantOrSorceryTrigger() {
        Permanent alchemist = harness.addToBattlefieldAndReturn(player1, new AquaticAlchemistBubbleUp());
        harness.setHand(player1, List.of(new GrizzlyBears(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).filteredOn(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .hasSize(1);

        harness.passBothPriorities();

        assertThat(alchemist.getPowerModifier()).isEqualTo(2);
    }
}
