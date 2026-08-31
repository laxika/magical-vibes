package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NovaCleric.class, AngelicChorus.class, RuleOfLaw.class, GrizzlyBears.class})
class NovaClericTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Nova Cleric destroys all enchantments")
    void sacrificingNovaClericDestroysAllEnchantments() {
        Permanent novaCleric = addReadyNovaCleric();
        harness.addToBattlefield(player1, new RuleOfLaw());
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.addToBattlefield(player1, new GrizzlyBears());
        addMana();

        harness.activateAbility(player1, battlefieldIndex(player1, novaCleric), null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Nova Cleric");
        harness.assertNotOnBattlefield(player1, "Rule of Law");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private Permanent addReadyNovaCleric() {
        Permanent novaCleric = harness.addToBattlefieldAndReturn(player1, new NovaCleric());
        novaCleric.setSummoningSick(false);
        return novaCleric;
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private int battlefieldIndex(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
