package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.s.ScreechingBuzzard;
import com.github.laxika.magicalvibes.cards.w.WirewoodElf;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrownOfAwe.class, ElvishWarrior.class, GlorySeeker.class, ScreechingBuzzard.class,
        WirewoodElf.class})
class CrownOfAweTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has protection from black and red")
    void enchantedCreatureHasProtectionFromBlackAndRed() {
        Permanent warrior = addCreatureReady(player1, new ElvishWarrior());
        attachCrown(warrior);

        assertThat(gqs.hasProtectionFrom(gd, warrior, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, warrior, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, warrior, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("Sacrificing the Aura protects the enchanted creature and creatures sharing its type")
    void sacrificeProtectsEnchantedAndSharingCreatures() {
        Permanent warrior = addCreatureReady(player1, new ElvishWarrior());
        Permanent otherElf = addCreatureReady(player2, new WirewoodElf());
        Permanent human = addCreatureReady(player1, new GlorySeeker());
        Permanent bird = addCreatureReady(player2, new ScreechingBuzzard());
        Permanent crown = attachCrown(warrior);

        assertThat(gqs.hasProtectionFrom(gd, otherElf, CardColor.BLACK)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, otherElf, CardColor.RED)).isFalse();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(crown), null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, warrior, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, warrior, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, otherElf, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, otherElf, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, human, CardColor.BLACK)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, bird, CardColor.RED)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(crown);
    }

    @Test
    @DisplayName("Sacrifice protection wears off at end of turn")
    void sacrificeProtectionWearsOffAtEndOfTurn() {
        Permanent warrior = addCreatureReady(player1, new ElvishWarrior());
        Permanent otherElf = addCreatureReady(player2, new WirewoodElf());
        Permanent crown = attachCrown(warrior);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(crown), null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasProtectionFrom(gd, otherElf, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, otherElf, CardColor.RED)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, warrior, CardColor.BLACK)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, warrior, CardColor.RED)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, otherElf, CardColor.BLACK)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, otherElf, CardColor.RED)).isFalse();
    }

    private Permanent attachCrown(Permanent host) {
        Permanent crown = harness.addToBattlefieldAndReturn(player1, new CrownOfAwe());
        crown.setAttachedTo(host.getId());
        return crown;
    }
}
