package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.Distress;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Sift;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WasteNotTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent discarding a creature card creates a 2/2 black Zombie token")
    void creatureDiscardCreatesZombie() {
        harness.addToBattlefield(player1, new WasteNot());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        var zombies = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Zombie"))
                .toList();
        assertThat(zombies).hasSize(1);
        assertThat(zombies.getFirst().getCard().getPower()).isEqualTo(2);
        assertThat(zombies.getFirst().getCard().getToughness()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Opponent discarding a noncreature, nonland card draws its controller a card")
    void noncreatureNonlandDiscardDrawsCard() {
        harness.addToBattlefield(player1, new WasteNot());
        harness.setHand(player2, new ArrayList<>(List.of(new Sift())));
        harness.setLibrary(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .noneMatch(p -> p.getCard().getName().equals("Zombie"))).isTrue();
    }

    @Test
    @DisplayName("Opponent discarding a land card adds {B}{B} to its controller's mana pool")
    void landDiscardAddsBlackMana() {
        harness.addToBattlefield(player1, new WasteNot());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setLibrary(player2, new ArrayList<>(List.of(new Swamp(), new Swamp(), new Swamp())));
        harness.setHand(player2, List.of(new Sift()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .noneMatch(p -> p.getCard().getName().equals("Zombie"))).isTrue();
    }

    @Test
    @DisplayName("Waste Not does not trigger when its own controller discards")
    void doesNotTriggerOnControllerDiscard() {
        harness.addToBattlefield(player1, new WasteNot());
        harness.setLibrary(player1, new ArrayList<>(List.of(new Swamp(), new Swamp(), new Swamp())));
        harness.setHand(player1, List.of(new Sift()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .noneMatch(p -> p.getCard().getName().equals("Zombie"))).isTrue();
    }
}
