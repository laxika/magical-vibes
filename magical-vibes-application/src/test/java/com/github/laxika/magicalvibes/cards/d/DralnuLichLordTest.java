package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DralnuLichLord.class, GrizzlyBears.class, Shock.class})
class DralnuLichLordTest extends BaseCardTest {

    @Test
    @DisplayName("Damage to Dralnu is replaced by sacrificing permanents")
    void damageIsReplacedBySacrificingPermanents() {
        harness.addToBattlefield(player1, new DralnuLichLord());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        Permanent dralnu = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.castInstant(player2, 0, dralnu.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Dralnu, Lich Lord");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Combat damage to Dralnu is replaced by sacrificing permanents")
    void combatDamageIsReplacedBySacrificingPermanents() {
        Permanent dralnu = addReadyDralnu(player2);
        Permanent attacker = addReadyCreature(player1);
        attacker.setAttacking(true);
        dralnu.setBlocking(true);
        dralnu.addBlockingTarget(0);

        resolveCombat(player1);

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
        harness.assertInGraveyard(player2, "Dralnu, Lich Lord");
    }

    @Test
    @DisplayName("The tap ability grants flashback to a targeted instant or sorcery")
    void tapAbilityGrantsFlashback() {
        addReadyDralnu(player1);
        Shock shock = new Shock();
        Permanent target = addReadyCreature(player2);
        harness.setGraveyard(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, shock.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.castFlashback(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    private Permanent addReadyDralnu(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new DralnuLichLord());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
