package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfBlossoms;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OminousLockbox.class, GrizzlyBears.class, WallOfBlossoms.class})
class OminousLockboxTest extends BaseCardTest {

    private Permanent castLockbox(int chosenNumber) {
        OminousLockbox lockbox = new OminousLockbox();
        harness.setHand(player1, List.of(lockbox));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, Integer.toString(chosenNumber));
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }

    @Test
    void matchingOpponentSpellSacrificesLockboxAndCreatesCopy() {
        Permanent lockbox = castLockbox(2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anySatisfy(card -> assertThat(card.getId()).isEqualTo(lockbox.getCard().getId()));
        assertThat(gd.stack).anyMatch(StackEntry::isCopy);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
    }

    @Test
    void powerOrToughnessMatchWithoutManaValueDoesNotTrigger() {
        castLockbox(4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new WallOfBlossoms()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);

        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == com.github.laxika.magicalvibes.model.StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    void sacrificeAbilityDrawsACard() {
        Card drawn = new GrizzlyBears();
        Permanent lockbox = castLockbox(20);
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anySatisfy(card -> assertThat(card.getId()).isEqualTo(lockbox.getCard().getId()));
    }
}
