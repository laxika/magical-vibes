package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WirewoodElf;
import com.github.laxika.magicalvibes.cards.w.WirewoodLodge;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({CrownOfFury.class, ElvishWarrior.class, GlorySeeker.class, Shock.class, WirewoodElf.class, WirewoodLodge.class})
class CrownOfFuryTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+0 and has first strike")
    void enchantedCreatureGetsBoostAndFirstStrike() {
        Permanent warrior = addCreatureReady(player1, new ElvishWarrior());
        attachCrown(warrior);

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent lodge = harness.addToBattlefieldAndReturn(player2, new WirewoodLodge());
        CrownOfFury crown = new CrownOfFury();
        harness.setHand(player1, List.of(crown));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, lodge.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Sacrificing the Aura boosts and grants first strike to sharing creatures")
    void sacrificeAffectsEnchantedAndSharingCreatures() {
        Permanent warrior = addCreatureReady(player1, new ElvishWarrior());
        Permanent otherElf = addCreatureReady(player2, new WirewoodElf());
        Permanent human = addCreatureReady(player1, new GlorySeeker());
        Permanent otherHuman = addCreatureReady(player2, new GlorySeeker());
        Permanent crown = attachCrown(warrior);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(crown), null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, otherElf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherElf)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, otherHuman)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherHuman)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherElf, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, human, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherHuman, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(crown);
    }

    @Test
    @DisplayName("Sacrifice boost and first strike wear off at end of turn")
    void sacrificeEffectsWearOffAtEndOfTurn() {
        Permanent warrior = addCreatureReady(player1, new ElvishWarrior());
        Permanent otherElf = addCreatureReady(player2, new WirewoodElf());
        Permanent crown = attachCrown(warrior);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(crown), null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, otherElf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, otherElf, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, otherElf)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherElf, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Sacrifice effect does not affect creatures entering later")
    void sacrificeDoesNotAffectCreaturesEnteringLater() {
        Permanent warrior = addCreatureReady(player1, new ElvishWarrior());
        Permanent crown = attachCrown(warrior);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(crown), null, null);
        harness.passBothPriorities();

        Permanent lateElf = addCreatureReady(player2, new WirewoodElf());

        assertThat(gqs.getEffectivePower(gd, lateElf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, lateElf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lateElf, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Aura can enchant an opponent's creature and its controller can sacrifice it")
    void canEnchantAndBoostOpponentsCreature() {
        Permanent warrior = addCreatureReady(player2, new ElvishWarrior());
        Permanent elf = addCreatureReady(player1, new WirewoodElf());
        harness.setHand(player1, List.of(new CrownOfFury()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, warrior.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.FIRST_STRIKE)).isTrue();
        Permanent crown = findPermanent(player1, "Crown of Fury");
        assertThat(crown.getAttachedTo()).isEqualTo(warrior.getId());
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(crown), null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, elf, Keyword.FIRST_STRIKE)).isTrue();
        harness.assertInGraveyard(player1, "Crown of Fury");
    }

    @Test
    @DisplayName("Sharing creatures entering before resolution receive both effects")
    void sacrificeIncludesCreaturesEnteringBeforeResolution() {
        Permanent warrior = addCreatureReady(player1, new ElvishWarrior());
        Permanent crown = attachCrown(warrior);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(crown), null, null);

        Permanent elf = harness.enterBattlefieldAndReturn(player2, new WirewoodElf());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, elf, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Sacrifice uses the enchanted creature's last known types if it dies in response")
    void sacrificeUsesLastKnownCreatureTypes() {
        Permanent host = addCreatureReady(player1, new WirewoodElf());
        Permanent warrior = addCreatureReady(player2, new ElvishWarrior());
        Permanent human = addCreatureReady(player2, new GlorySeeker());
        Permanent crown = attachCrown(host);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(crown), null, null);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, host.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Wirewood Elf");
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, human, Keyword.FIRST_STRIKE)).isFalse();
    }

    private Permanent attachCrown(Permanent host) {
        Permanent crown = harness.addToBattlefieldAndReturn(player1, new CrownOfFury());
        crown.setAttachedTo(host.getId());
        return crown;
    }
}
