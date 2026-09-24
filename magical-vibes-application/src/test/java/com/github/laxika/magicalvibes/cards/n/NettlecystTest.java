package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PhyrexianArena;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Nettlecyst.class, FountainOfYouth.class, PhyrexianArena.class, GrizzlyBears.class})
class NettlecystTest extends BaseCardTest {

    @Test
    @DisplayName("Living weapon creates a Germ and attaches Nettlecyst to it")
    void livingWeaponCreatesAndEquipsGerm() {
        castNettlecyst();

        Permanent germ = findPermanent(player1, "Phyrexian Germ");
        Permanent nettlecyst = findPermanent(player1, "Nettlecyst");
        assertThat(germ.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(1);
        assertThat(nettlecyst.getAttachedTo()).isEqualTo(germ.getId());
    }

    @Test
    @DisplayName("Equipped creature gets +1/+1 for each artifact and enchantment controlled")
    void countsArtifactsAndEnchantments() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.addToBattlefield(player1, new PhyrexianArena());
        castNettlecyst();

        Permanent germ = findPermanent(player1, "Phyrexian Germ");
        assertThat(gqs.getEffectivePower(gd, germ)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, germ)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equip can move Nettlecyst to another creature")
    void equipsAnotherCreature() {
        castNettlecyst();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Permanent nettlecyst = findPermanent(player1, "Nettlecyst");
        int nettlecystIndex = gd.playerBattlefields.get(player1.getId()).indexOf(nettlecyst);
        harness.activateAbility(player1, nettlecystIndex, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(nettlecyst.getAttachedTo()).isEqualTo(target.getId());
    }

    private void castNettlecyst() {
        harness.setHand(player1, List.of(new Nettlecyst()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
