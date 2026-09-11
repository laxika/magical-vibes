package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BoggartBrute;
import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.f.FaerieMiscreant;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SynchronizedSpellcraft.class, BoggartBrute.class, ColossalDreadmaw.class,
        FaerieMiscreant.class, FugitiveWizard.class, SoulWarden.class})
class SynchronizedSpellcraftTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to the target creature and no damage to its controller with no party")
    void dealsBaseDamageWithNoParty() {
        Permanent target = addTarget();

        castSpell(target);

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Deals damage to the target creature's controller equal to the party size")
    void dealsPartyDamageToTargetController() {
        Permanent target = addTarget();
        addFullParty();

        castSpell(target);

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Counts the party when the spell resolves")
    void countsPartyAtResolution() {
        Permanent target = addTarget();
        harness.setHand(player1, List.of(new SynchronizedSpellcraft()));
        addMana();
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, target.getId());
        addFullParty();
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    private Permanent addTarget() {
        return harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
    }

    private void addFullParty() {
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player1, new FaerieMiscreant());
        harness.addToBattlefield(player1, new BoggartBrute());
        harness.addToBattlefield(player1, new FugitiveWizard());
    }

    private void castSpell(Permanent target) {
        harness.setHand(player1, List.of(new SynchronizedSpellcraft()));
        addMana();
        harness.setLife(player2, 20);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
