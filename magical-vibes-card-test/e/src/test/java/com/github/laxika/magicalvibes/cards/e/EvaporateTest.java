package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FolkOfAnHavva;
import com.github.laxika.magicalvibes.cards.g.GiantOyster;
import com.github.laxika.magicalvibes.cards.m.MesaFalcon;
import com.github.laxika.magicalvibes.cards.r.RevekaWizardSavant;
import com.github.laxika.magicalvibes.cards.r.Roterothopter;
import com.github.laxika.magicalvibes.cards.s.SerraAviary;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Evaporate.class, FolkOfAnHavva.class, GiantOyster.class, MesaFalcon.class,
        RevekaWizardSavant.class, Roterothopter.class, SerraAviary.class})
class EvaporateTest extends BaseCardTest {

    private void castEvaporate() {
        harness.setHand(player1, List.of(new Evaporate()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
    }

    @Test
    @DisplayName("Evaporate destroys white and blue creatures on both sides")
    void damagesWhiteAndBlueCreatures() {
        harness.addToBattlefield(player1, new MesaFalcon());
        harness.addToBattlefield(player2, new RevekaWizardSavant());

        castEvaporate();

        harness.assertNotOnBattlefield(player1, "Mesa Falcon");
        harness.assertNotOnBattlefield(player2, "Reveka, Wizard Savant");
    }

    @Test
    @DisplayName("Evaporate leaves creatures that are neither white nor blue alone")
    void sparesOtherColorsAndColorless() {
        Permanent greenCreature = harness.addToBattlefieldAndReturn(player1, new FolkOfAnHavva());
        Permanent colorlessCreature = harness.addToBattlefieldAndReturn(player2, new Roterothopter());

        castEvaporate();

        harness.assertOnBattlefield(player1, "Folk of An-Havva");
        harness.assertOnBattlefield(player2, "Roterothopter");
        assertThat(greenCreature.getMarkedDamage()).isZero();
        assertThat(colorlessCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Evaporate deals exactly one damage to each matching creature")
    void dealsExactlyOneDamageToMatchingCreature() {
        Permanent oyster = harness.addToBattlefieldAndReturn(player1, new GiantOyster());

        castEvaporate();

        harness.assertOnBattlefield(player1, "Giant Oyster");
        assertThat(oyster.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Evaporate deals no damage to players")
    void doesNotDamagePlayers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        castEvaporate();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Evaporate damages a creature that is both white and blue only once")
    void damagesWhiteAndBlueCreatureOnlyOnce() {
        Permanent oyster = harness.addToBattlefieldAndReturn(player2, new GiantOyster());
        oyster.getGrantedColors().add(CardColor.WHITE);

        castEvaporate();

        harness.assertOnBattlefield(player2, "Giant Oyster");
        assertThat(oyster.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Evaporate does not damage a white noncreature permanent")
    void sparesWhiteNoncreaturePermanent() {
        Permanent aviary = harness.addToBattlefieldAndReturn(player2, new SerraAviary());

        castEvaporate();

        harness.assertOnBattlefield(player2, "Serra Aviary");
        assertThat(aviary.getMarkedDamage()).isZero();
    }
}
