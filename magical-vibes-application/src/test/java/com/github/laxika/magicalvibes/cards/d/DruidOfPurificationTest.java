package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Propaganda;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DruidOfPurification.class, ZuranOrb.class, Propaganda.class, GrizzlyBears.class})
class DruidOfPurificationTest extends BaseCardTest {

    @Test
    void eachPlayerChoosesOpponentArtifactOrEnchantmentStartingWithController() {
        Permanent player1Orb = harness.addToBattlefieldAndReturn(player1, new ZuranOrb());
        Permanent player1Propaganda = harness.addToBattlefieldAndReturn(player1, new Propaganda());
        Permanent player1Bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player2Orb = harness.addToBattlefieldAndReturn(player2, new ZuranOrb());
        Permanent player2Propaganda = harness.addToBattlefieldAndReturn(player2, new Propaganda());
        Permanent player2Bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new DruidOfPurification()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.playerId()).isEqualTo(player1.getId());
        assertThat(firstChoice.validIds()).containsExactly(player2Orb.getId(), player2Propaganda.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(player2Propaganda.getId()));

        PendingInteraction.MultiPermanentChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.playerId()).isEqualTo(player2.getId());
        assertThat(secondChoice.validIds()).containsExactly(player1Orb.getId(), player1Propaganda.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(player1Orb.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(player1Propaganda, player1Bear)
                .doesNotContain(player1Orb);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(player2Orb, player2Bear)
                .doesNotContain(player2Propaganda);
    }
}
