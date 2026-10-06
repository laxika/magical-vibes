package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DawnhartRejuvenator;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RevengeOfTheDrowned.class, DawnhartRejuvenator.class, Island.class})
class RevengeOfTheDrownedTest extends BaseCardTest {

    @Test
    @DisplayName("Puts the target creature on the bottom and creates a decayed Zombie")
    void ownerChoosesBottomAndSpellCreatesZombie() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DawnhartRejuvenator());
        Card libraryCard = new DawnhartRejuvenator();
        harness.setLibrary(player2, List.of(libraryCard));

        castRevenge(target.getId());

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.TargetLibraryDestinationChoice.class)
                .playerId()).isEqualTo(player2.getId());

        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard, target.getCard());
        assertDecayedZombieCreated();
        harness.assertInGraveyard(player1, "Revenge of the Drowned");
    }

    @Test
    @DisplayName("Puts the target creature on top and creates a decayed Zombie")
    void ownerChoosesTopAndSpellCreatesZombie() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DawnhartRejuvenator());
        Card libraryCard = new DawnhartRejuvenator();
        harness.setLibrary(player2, List.of(libraryCard));

        castRevenge(target.getId());

        harness.handleListChoice(player2, "Top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard(), libraryCard);
        assertDecayedZombieCreated();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Card land = new Island();
        Permanent target = harness.addToBattlefieldAndReturn(player2, land);
        harness.setHand(player1, List.of(new RevengeOfTheDrowned()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void ownerChoosesDestinationForCreatureControlledByOpponent() {
        Card creature = new DawnhartRejuvenator();
        creature.setOwnerId(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, creature);
        Card libraryCard = new Island();
        harness.setLibrary(player1, List.of(libraryCard));

        castRevenge(target.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.TargetLibraryDestinationChoice.class)
                .playerId()).isEqualTo(player1.getId());
        harness.handleListChoice(player1, "Bottom");

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard, creature);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertDecayedZombieCreated();
    }

    @Test
    void illegalTargetPreventsZombieCreation() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DawnhartRejuvenator());
        harness.setHand(player1, List.of(new RevengeOfTheDrowned()));
        addMana();
        harness.castInstant(player1, 0, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, target));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Revenge of the Drowned");
    }

    @Test
    void attackingZombieIsSacrificedAtEndOfCombat() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DawnhartRejuvenator());
        castRevenge(target.getId());
        harness.handleListChoice(player2, "Top");
        Permanent zombie = findPermanent(player1, "Zombie");
        zombie.setSummoningSick(false);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(zombie);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(attackerIndex));
            resolveAllTriggers();
            assertThat(gd.playerBattlefields.get(player1.getId())).contains(zombie);
        });
        harness.passUntil(player1, TurnStep.END_OF_COMBAT);

        harness.assertLife(player2, 18);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(zombie);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(zombie);
    }

    private void castRevenge(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new RevengeOfTheDrowned()));
        addMana();
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void assertDecayedZombieCreated() {
        Permanent zombie = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(zombie.getCard().getPower()).isEqualTo(2);
        assertThat(zombie.getCard().getToughness()).isEqualTo(2);
        assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(zombie.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(zombie.getCard().getKeywords()).contains(Keyword.DECAYED);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(bls.canBlock(gd, zombie)).isFalse();
    }
}
