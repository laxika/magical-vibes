package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.model.Permanent;




@CardUsed({ForceOfNegation.class, GrizzlyBears.class, Opt.class})
class ForceOfNegationTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a noncreature spell and exiles it when cast normally")
    void countersNoncreatureSpellAndExilesItNormally() {
        Opt opt = new Opt();
        harness.setHand(player1, List.of(opt));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.setHand(player2, List.of(new ForceOfNegation()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, opt.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(opt.getId()));
        harness.assertNotInGraveyard(player1, "Opt");
        harness.assertInGraveyard(player2, "Force of Negation");
    }

    @Test
    @DisplayName("May exile a blue card to cast it during an opponent's turn")
    void castsByExilingBlueCardDuringOpponentsTurn() {
        Opt target = new Opt();
        Opt blueCard = new Opt();
        harness.setHand(player2, List.of(target));
        harness.addMana(player2, ManaColor.BLUE, 1);

        ForceOfNegation force = new ForceOfNegation();
        harness.setHand(player1, List.of(force, blueCard));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0);
        harness.castInstantWithAlternateExileFromHand(player1, 0, target.getId(), 1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(blueCard.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getId()));
        harness.assertInGraveyard(player1, "Force of Negation");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot use the alternate cost during its controller's turn")
    void alternateCostRequiresOpponentTurn() {
        Opt target = new Opt();
        harness.setHand(player2, List.of(target));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.setHand(player1, List.of(new ForceOfNegation(), new Opt()));
        harness.passPriority(player1);
        harness.castInstant(player2, 0);

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(
                player1, 0, target.getId(), 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("condition is not met");
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new ForceOfNegation()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("noncreature spell");
    }
}

@CardUsed({ForceOfNegation.class, Counterspell.class, GrizzlyBears.class, MightOfOaks.class})
class Mh1ForceOfNegationTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a noncreature spell and exiles it when cast normally")
    void countersNoncreatureSpellAndExilesItNormally() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        MightOfOaks might = new MightOfOaks();
        harness.setHand(player2, List.of(might));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.setHand(player1, List.of(new ForceOfNegation()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        harness.castInstant(player1, 0, might.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Force of Negation");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .containsExactly("Might of Oaks");
    }

    @Test
    @DisplayName("Can exile a blue card to cast on an opponent's turn")
    void castsForAlternateCostOnOpponentsTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        MightOfOaks might = new MightOfOaks();
        harness.setHand(player2, List.of(might));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.setHand(player1, List.of(new ForceOfNegation(), new Counterspell()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, target.getId());
        harness.passPriority(player2);
        harness.castInstantWithAlternateExileFromHand(player1, 0, might.getId(), 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Force of Negation");
        assertThat(gd.exiledCards).extracting(entry -> entry.card().getName())
                .containsExactlyInAnyOrder("Might of Oaks", "Counterspell");
    }

    @Test
    @DisplayName("Cannot use its alternate cost during your own turn")
    void alternateCostUnavailableOnOwnTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        MightOfOaks might = new MightOfOaks();
        harness.setHand(player2, List.of(might));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new ForceOfNegation(), new Counterspell()));

        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(
                player1, 0, might.getId(), 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new ForceOfNegation()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player2, 0);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a noncreature spell");
    }
}
