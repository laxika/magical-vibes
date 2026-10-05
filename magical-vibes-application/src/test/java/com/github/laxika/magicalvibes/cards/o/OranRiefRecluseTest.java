package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GiantScorpion;
import com.github.laxika.magicalvibes.cards.w.WelkinTern;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OranRiefRecluse.class, WelkinTern.class, GiantScorpion.class})
class OranRiefRecluseTest extends BaseCardTest {

    @Test
    void entersWithoutKickerAndDoesNotDestroyFlyingCreature() {
        harness.addToBattlefield(player2, new WelkinTern());
        harness.setHand(player1, List.of(new OranRiefRecluse()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Oran-Rief Recluse");
        harness.assertOnBattlefield(player2, "Welkin Tern");
    }

    @Test
    void destroysTargetFlyingCreatureWhenKicked() {
        harness.addToBattlefield(player2, new WelkinTern());
        Permanent target = findPermanent(player2, "Welkin Tern");
        harness.setHand(player1, List.of(new OranRiefRecluse()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castKickedCreature(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Oran-Rief Recluse");
        harness.assertInGraveyard(player2, "Welkin Tern");
    }

    @Test
    void cannotTargetCreatureWithoutFlyingWhenKicked() {
        harness.addToBattlefield(player2, new WelkinTern());
        harness.addToBattlefield(player2, new GiantScorpion());
        Permanent target = findPermanent(player2, "Giant Scorpion");
        harness.setHand(player1, List.of(new OranRiefRecluse()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
        harness.handlePermanentChosen(player1, findPermanent(player2, "Welkin Tern").getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Giant Scorpion");
        harness.assertInGraveyard(player2, "Welkin Tern");
    }

    @Test
    void canBeKickedWithoutAnyFlyingCreature() {
        harness.setHand(player1, List.of(new OranRiefRecluse()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Oran-Rief Recluse");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canDestroyItsControllersFlyingCreature() {
        harness.addToBattlefield(player1, new WelkinTern());
        harness.setHand(player1, List.of(new OranRiefRecluse()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, findPermanent(player1, "Welkin Tern").getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Oran-Rief Recluse");
        harness.assertInGraveyard(player1, "Welkin Tern");
    }
}
