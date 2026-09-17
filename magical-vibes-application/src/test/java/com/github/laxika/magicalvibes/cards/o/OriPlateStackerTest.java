package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OriPlateStacker.class, Ornithopter.class, RuleOfLaw.class, GrizzlyBears.class})
class OriPlateStackerTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys opponents' artifacts and enchantments and gains life for each")
    void destroysOpponentsArtifactsAndEnchantments() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent ownEnchantment = harness.addToBattlefieldAndReturn(player1, new RuleOfLaw());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent opponentEnchantment = harness.addToBattlefieldAndReturn(player2, new RuleOfLaw());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new OriPlateStacker()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(ownArtifact, ownEnchantment)
                .anyMatch(permanent -> permanent.getCard() instanceof OriPlateStacker);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(opponentArtifact, opponentEnchantment)
                .contains(opponentCreature);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Does not destroy your artifacts or enchantments and gains no life when none are destroyed")
    void leavesYourPermanentsAloneWhenNoOpponentTargetsExist() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent ownEnchantment = harness.addToBattlefieldAndReturn(player1, new RuleOfLaw());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new OriPlateStacker()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(ownArtifact, ownEnchantment)
                .anyMatch(permanent -> permanent.getCard() instanceof OriPlateStacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
