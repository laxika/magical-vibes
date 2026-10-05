package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OriPlateStacker.class, Ornithopter.class, RuleOfLaw.class, GrizzlyBears.class, DarksteelIngot.class})
class OriPlateStackerTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys opponents' artifacts and enchantments and gains life for each")
    void destroysOpponentsArtifactsAndEnchantments() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent ownEnchantment = harness.addToBattlefieldAndReturn(player1, new RuleOfLaw());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent opponentEnchantment = harness.addToBattlefieldAndReturn(player2, new RuleOfLaw());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new OriPlateStacker(), "{5}{W}{W}");
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

        harness.castFromHand(player1, new OriPlateStacker(), "{5}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(ownArtifact, ownEnchantment)
                .anyMatch(permanent -> permanent.getCard() instanceof OriPlateStacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Indestructible artifacts survive and do not contribute to life gained")
    void gainsLifeOnlyForPermanentsActuallyDestroyed() {
        Permanent indestructibleArtifact = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        Permanent destructibleArtifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        harness.castFromHand(player1, new OriPlateStacker(), "{5}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(indestructibleArtifact)
                .doesNotContain(destructibleArtifact);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(destructibleArtifact.getCard());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Entering without being cast destroys the other player's permanents and benefits Ori's controller")
    void enteringWithoutCastingUsesOrisController() {
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        harness.enterBattlefieldAndReturn(player2, new OriPlateStacker());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(opposingArtifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ownArtifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(opposingArtifact.getCard());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(21);
    }
}
