package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BloodstainedMire;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DwarvenBlastminer.class, BloodstainedMire.class, Forest.class, GlorySeeker.class})
class DwarvenBlastminerTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target nonbasic land")
    void destroysNonbasicLand() {
        addCreatureReady(player1, new DwarvenBlastminer());
        harness.addToBattlefield(player2, new BloodstainedMire());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Bloodstained Mire");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Bloodstained Mire");
        harness.assertInGraveyard(player2, "Bloodstained Mire");
    }

    @Test
    @DisplayName("Can destroy a nonbasic land its controller controls")
    void destroysOwnNonbasicLand() {
        addCreatureReady(player1, new DwarvenBlastminer());
        harness.addToBattlefield(player1, new BloodstainedMire());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player1, "Bloodstained Mire");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bloodstained Mire");
    }

    @Test
    @DisplayName("Pays its mana cost and taps itself when activated")
    void paysManaAndTapsSource() {
        Permanent blastminer = addCreatureReady(player1, new DwarvenBlastminer());
        harness.addToBattlefield(player2, new BloodstainedMire());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Bloodstained Mire");

        harness.activateAbility(player1, 0, null, targetId);

        assertThat(blastminer.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot target a basic land")
    void cannotTargetBasicLand() {
        addCreatureReady(player1, new DwarvenBlastminer());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetCreature() {
        addCreatureReady(player1, new DwarvenBlastminer());
        harness.addToBattlefield(player2, new GlorySeeker());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Glory Seeker");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the target is no longer a nonbasic land at resolution")
    void fizzlesIfTargetBecomesFaceDown() {
        addCreatureReady(player1, new DwarvenBlastminer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BloodstainedMire());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        target.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.passBothPriorities();

        assertThat(target.isFaceDown()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new DwarvenBlastminer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent blastminer = findPermanent(player1, "Dwarven Blastminer");
        assertThat(blastminer.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, blastminer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blastminer)).isEqualTo(2);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(blastminer));
        harness.passBothPriorities();

        assertThat(blastminer.isFaceDown()).isFalse();
        assertThat(gqs.getEffectivePower(gd, blastminer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, blastminer)).isEqualTo(1);
    }
}
