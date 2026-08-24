package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuthlessLawbringer.class, GrizzlyBears.class, Forest.class})
class RuthlessLawbringerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may sacrifice another creature to destroy a target nonland permanent")
    void etbSacrificeDestroysTargetNonlandPermanent() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        RuthlessLawbringer lawbringer = new RuthlessLawbringer();
        harness.setHand(player1, List.of(lawbringer));
        addLawbringerMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice sacrificeChoice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(sacrificeChoice.validIds()).containsExactly(sacrifice.getId());
        harness.handlePermanentChosen(player1, sacrifice.getId());

        PendingInteraction.PermanentChoice targetChoice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(targetChoice.validIds()).contains(target.getId()).doesNotContain(land.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sacrifice.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(lawbringer.getId()));
    }

    @Test
    @DisplayName("Declining the ETB sacrifice leaves permanents unchanged")
    void decliningEtbSacrificeDoesNothing() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RuthlessLawbringer()));
        addLawbringerMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrifice);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    private void addLawbringerMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
