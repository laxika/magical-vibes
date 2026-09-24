package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.k.KamiOfFalseHope;
import com.github.laxika.magicalvibes.cards.t.TakenosCavalry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StirTheGrave.class, KamiOfFalseHope.class, TakenosCavalry.class})
class StirTheGraveTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature card with mana value equal to X from the graveyard")
    void returnsCreatureWithManaValueEqualToX() {
        KamiOfFalseHope creature = new KamiOfFalseHope();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new StirTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 1, creature.getId());

        harness.assertOnBattlefield(player1, "Kami of False Hope");
        harness.assertNotInGraveyard(player1, "Kami of False Hope");
    }

    @Test
    @DisplayName("Returns a creature card with mana value less than X from the graveyard")
    void returnsCreatureWithManaValueLessThanX() {
        KamiOfFalseHope creature = new KamiOfFalseHope();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new StirTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 2, creature.getId());

        harness.assertOnBattlefield(player1, "Kami of False Hope");
    }

    @Test
    @DisplayName("Rejects a creature card with mana value greater than X")
    void rejectsManaValueAboveX() {
        TakenosCavalry creature = new TakenosCavalry();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new StirTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a noncreature card in the graveyard")
    void rejectsNoncreatureCard() {
        StirTheGrave graveyardCard = new StirTheGrave();
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(new StirTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, graveyardCard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a creature card in an opponent's graveyard")
    void rejectsOpponentGraveyardCard() {
        KamiOfFalseHope creature = new KamiOfFalseHope();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new StirTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the target leaves the graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyard() {
        KamiOfFalseHope creature = new KamiOfFalseHope();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new StirTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 1, creature.getId());
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kami of False Hope");
    }
}
