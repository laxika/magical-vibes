package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.cards.e.EnergyRefractor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UrzasRebuff.class, ArgothianSprite.class, EnergyRefractor.class})
class UrzasRebuffTest extends BaseCardTest {

    @Test
    @DisplayName("Counter mode counters the target spell")
    void counterModeCountersSpell() {
        ArgothianSprite bears = new ArgothianSprite();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new UrzasRebuff()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castModalInstantWithModes(player2, 0, 1, new int[]{0}, bears.getId(), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Argothian Sprite");
        harness.assertNotOnBattlefield(player1, "Argothian Sprite");
    }

    @Test
    @DisplayName("Tap mode taps up to two target creatures")
    void tapModeTapsUpToTwoCreatures() {
        Permanent first = addCreatureReady(player2, new ArgothianSprite());
        Permanent second = addCreatureReady(player2, new ArgothianSprite());
        Permanent third = addCreatureReady(player2, new ArgothianSprite());
        harness.setHand(player1, List.of(new UrzasRebuff()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castModalInstant(player1, 0, 1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tap mode may choose no creatures")
    void tapModeMayChooseNoCreatures() {
        harness.setHand(player1, List.of(new UrzasRebuff()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castModalInstant(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Tap mode rejects a noncreature target")
    void tapModeRejectsNoncreatureTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new EnergyRefractor());
        harness.setHand(player1, List.of(new UrzasRebuff()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tapModeCanTargetOneCreatureYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        harness.setHand(player1, List.of(new UrzasRebuff()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castModalInstant(player1, 0, 1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Urza's Rebuff");
    }

    @Test
    void tapModeRejectsThreeTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        harness.setHand(player1, List.of(new UrzasRebuff()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tapModeRejectsTheSameCreatureTwice() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        harness.setHand(player1, List.of(new UrzasRebuff()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 1,
                List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tapModeStillTapsRemainingTargetWhenOneLeaves() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        harness.setHand(player1, List.of(new UrzasRebuff()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castModalInstant(player1, 0, 1, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(first);
        gd.playerGraveyards.get(player2.getId()).add(first.getCard());
        harness.passBothPriorities();

        assertThat(second.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Urza's Rebuff");
    }
}
