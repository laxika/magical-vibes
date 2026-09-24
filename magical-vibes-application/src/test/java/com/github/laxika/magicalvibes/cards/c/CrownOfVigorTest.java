package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.s.ScreechingBuzzard;
import com.github.laxika.magicalvibes.cards.w.WirewoodElf;
import com.github.laxika.magicalvibes.cards.w.WirewoodLodge;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrownOfVigor.class, ElvishWarrior.class, GlorySeeker.class, ScreechingBuzzard.class,
        WirewoodElf.class, WirewoodLodge.class})
class CrownOfVigorTest extends BaseCardTest {

    @Test
    @DisplayName("Can enchant a creature")
    void canEnchantCreature() {
        Permanent warrior = addCreatureReady(player2, new ElvishWarrior());
        CrownOfVigor crown = new CrownOfVigor();
        harness.setHand(player1, List.of(crown));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castEnchantment(player1, 0, warrior.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() == crown)
                .singleElement()
                .extracting(Permanent::getAttachedTo)
                .isEqualTo(warrior.getId());
        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent lodge = harness.addToBattlefieldAndReturn(player2, new WirewoodLodge());
        harness.setHand(player1, List.of(new CrownOfVigor()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, lodge.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanted creature gets +1/+1")
    void enchantedCreatureGetsBoost() {
        Permanent warrior = addCreatureReady(player1, new ElvishWarrior());
        attachCrown(warrior);

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(4);
    }

    @Test
    @DisplayName("Sacrificing the Aura boosts the enchanted creature and creatures sharing its type")
    void sacrificeBoostsEnchantedAndSharingCreatures() {
        Permanent warrior = addCreatureReady(player1, new ElvishWarrior());
        Permanent otherElf = addCreatureReady(player2, new WirewoodElf());
        Permanent human = addCreatureReady(player1, new GlorySeeker());
        Permanent bird = addCreatureReady(player2, new ScreechingBuzzard());
        Permanent crown = attachCrown(warrior);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(crown), null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, otherElf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherElf)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bird)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bird)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(crown);
    }

    @Test
    @DisplayName("Sacrifice boost wears off at end of turn")
    void sacrificeBoostWearsOffAtEndOfTurn() {
        Permanent warrior = addCreatureReady(player1, new ElvishWarrior());
        Permanent otherElf = addCreatureReady(player2, new WirewoodElf());
        Permanent crown = attachCrown(warrior);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(crown), null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, otherElf)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, otherElf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, otherElf)).isEqualTo(2);
    }

    private Permanent attachCrown(Permanent host) {
        Permanent crown = harness.addToBattlefieldAndReturn(player1, new CrownOfVigor());
        crown.setAttachedTo(host.getId());
        return crown;
    }
}
