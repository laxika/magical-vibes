package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GhostQuarter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DwarvenBlastminer.class, Forest.class, GhostQuarter.class, GrizzlyBears.class})
class DwarvenBlastminerTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target nonbasic land")
    void destroysNonbasicLand() {
        addCreatureReady(player1, new DwarvenBlastminer());
        harness.addToBattlefield(player2, new GhostQuarter());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Ghost Quarter");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ghost Quarter");
        harness.assertInGraveyard(player2, "Ghost Quarter");
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
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
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

        harness.addMana(player1, ManaColor.RED, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(blastminer));
        harness.passBothPriorities();

        assertThat(blastminer.isFaceDown()).isFalse();
    }

    private Permanent addCreatureReady(Player player, DwarvenBlastminer blastminer) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, blastminer);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
