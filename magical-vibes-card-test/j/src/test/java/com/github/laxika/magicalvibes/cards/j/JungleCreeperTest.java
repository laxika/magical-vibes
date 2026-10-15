package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.r.RummagingGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JungleCreeper.class, RummagingGoblin.class})
class JungleCreeperTest extends BaseCardTest {

    @Test
    void canActivateGraveyardAbilityWithEnoughMana() {
        JungleCreeper creeper = new JungleCreeper();
        harness.setGraveyard(player1, List.of(creeper));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    void resolvingGraveyardAbilityReturnsThisCardToHand() {
        JungleCreeper creeper = new JungleCreeper();
        harness.setGraveyard(player1, List.of(creeper));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Jungle Creeper");
        harness.assertNotInGraveyard(player1, "Jungle Creeper");
    }

    @Test
    void graveyardAbilityPaysManaCost() {
        JungleCreeper creeper = new JungleCreeper();
        harness.setGraveyard(player1, List.of(creeper));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    void cannotActivateGraveyardAbilityWithoutEnoughMana() {
        JungleCreeper creeper = new JungleCreeper();
        harness.setGraveyard(player1, List.of(creeper));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    void returnsOnlyTheActivatedCopyFromItsOwnersGraveyard() {
        JungleCreeper creeper = new JungleCreeper();
        JungleCreeper otherCopy = new JungleCreeper();
        JungleCreeper opponentsCopy = new JungleCreeper();
        harness.setGraveyard(player1, List.of(otherCopy, creeper));
        harness.setGraveyard(player2, List.of(opponentsCopy));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(creeper).doesNotContain(otherCopy);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherCopy);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsCopy);
    }

    @Test
    void canActivateTwiceButReturnsTheCardOnlyOnce() {
        JungleCreeper creeper = new JungleCreeper();
        harness.setGraveyard(player1, List.of(creeper));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateGraveyardAbility(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creeper);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsOnlyOnce(creeper);
        harness.assertNotInGraveyard(player1, "Jungle Creeper");
    }
    @Test
    void olderActivationDoesNotReturnTheCardAfterItIsDiscardedAgain() {
        JungleCreeper creeper = new JungleCreeper();
        JungleCreeper drawCard = new JungleCreeper();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(creeper));
        harness.setLibrary(player1, List.of(drawCard));
        harness.addToBattlefieldAndReturn(player1, new RummagingGoblin()).setSummoningSick(false);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateGraveyardAbility(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creeper);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creeper);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creeper);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawCard);
    }
}
