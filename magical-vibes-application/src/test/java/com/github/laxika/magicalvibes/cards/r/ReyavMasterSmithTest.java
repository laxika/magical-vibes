package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReyavMasterSmith.class, GrizzlyBears.class, HolyStrength.class, Bonesplitter.class})
class ReyavMasterSmithTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted and equipped attackers gain double strike")
    void enchantedAndEquippedAttackersGainDoubleStrike() {
        addCreatureReady(player1, new ReyavMasterSmith());
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipped = addCreatureReady(player1, new GrizzlyBears());
        Permanent plain = addCreatureReady(player1, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        aura.setAttachedTo(enchanted.getId());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        equipment.setAttachedTo(equipped.getId());

        declareAttackers(player1, List.of(1, 2, 3));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, enchanted, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, equipped, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, plain, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The granted double strike expires at end of turn")
    void doubleStrikeExpiresAtEndOfTurn() {
        addCreatureReady(player1, new ReyavMasterSmith());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        equipment.setAttachedTo(attacker.getId());

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DOUBLE_STRIKE)).isFalse();
    }
}
