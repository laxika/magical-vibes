package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DireDowndraft.class, DaggerfangDuo.class, Island.class})
class DireDowndraftTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {2}{U} when targeting a tapped creature")
    void reducedCostWhenTargetingTappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DaggerfangDuo());
        target.tap();

        castDireDowndraft(target, 2);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Costs {2}{U} when targeting an attacking creature")
    void reducedCostWhenTargetingAttackingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DaggerfangDuo());
        target.setAttacking(true);
        target.setAttackTarget(player1.getId());

        castDireDowndraft(target, 2);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Requires the full cost when targeting an untapped nonattacking creature")
    void reducedCostDoesNotApplyToUntappedNonattackingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DaggerfangDuo());
        harness.setHand(player1, List.of(new DireDowndraft()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The target creature's owner can put it on the bottom of their library")
    void targetOwnerChoosesBottom() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DaggerfangDuo());
        Card topCard = new Island();
        Card nextCard = new Island();
        harness.setLibrary(player2, List.of(topCard, nextCard));

        harness.setHand(player1, List.of(new DireDowndraft()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.TargetLibraryDestinationChoice.class)
                .playerId()).isEqualTo(player2.getId());

        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, nextCard, target.getCard());
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new DireDowndraft()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Being both tapped and attacking reduces the cost only once")
    void overlappingConditionsReduceCostOnlyOnce() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DaggerfangDuo());
        target.tap();
        target.setAttacking(true);
        target.setAttackTarget(player1.getId());

        castDireDowndraft(target, 3);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The owner, rather than the controller, chooses the library destination")
    void ownerChoosesTopForCreatureControlledByOpponent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DaggerfangDuo());
        gd.stolenCreatures.put(target.getId(), player2.getId());
        Card topCard = new Island();
        Card nextCard = new Island();
        harness.setLibrary(player2, List.of(topCard, nextCard));
        harness.setLibrary(player1, List.of());

        castDireDowndraft(target, 3);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.TargetLibraryDestinationChoice.class)
                .playerId()).isEqualTo(player2.getId());
        assertThatThrownBy(() -> harness.handleListChoice(player1, "Top"))
                .isInstanceOf(IllegalStateException.class);
        harness.handleListChoice(player2, "Top");

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard(), topCard, nextCard);
    }

    @Test
    @DisplayName("A target that leaves before resolution is not put into the library")
    void targetLeavingBattlefieldMakesSpellFizzle() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DaggerfangDuo());
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard));
        castDireDowndraft(target, 3);
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target.getCard());
        harness.assertInGraveyard(player1, "Dire Downdraft");
    }

    @Test
    @DisplayName("Untapping the target after casting does not change the paid cost")
    void discountIsDeterminedWhenCasting() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DaggerfangDuo());
        target.tap();
        harness.setLibrary(player2, List.of());
        castDireDowndraft(target, 2);
        target.untap();

        harness.passBothPriorities();
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard());
    }

    private void castDireDowndraft(Permanent target, int genericMana) {
        harness.setHand(player1, List.of(new DireDowndraft()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, genericMana);
        harness.castInstant(player1, 0, target.getId());
    }
}
