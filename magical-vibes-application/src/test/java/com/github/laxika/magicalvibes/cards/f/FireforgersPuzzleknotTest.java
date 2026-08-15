package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FireforgersPuzzleknotTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield deals 1 damage to a target creature")
    void enteringBattlefieldDealsDamageToCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FireforgersPuzzleknot()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Permanent target = findPermanent(player2, "Grizzly Bears");
        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Entering the battlefield deals 1 damage to a target player")
    void enteringBattlefieldDealsDamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new FireforgersPuzzleknot()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Sacrificing it deals 1 damage to a target creature")
    void sacrificeAbilityDealsDamageToCreature() {
        harness.addToBattlefield(player1, new FireforgersPuzzleknot());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent target = findPermanent(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Fireforger's Puzzleknot");
    }

    @Test
    @DisplayName("Sacrificing it deals 1 damage to a target player")
    void sacrificeAbilityDealsDamageToPlayer() {
        harness.addToBattlefield(player1, new FireforgersPuzzleknot());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertInGraveyard(player1, "Fireforger's Puzzleknot");
    }
}
