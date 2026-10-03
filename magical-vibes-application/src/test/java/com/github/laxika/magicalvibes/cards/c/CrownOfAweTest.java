package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.s.ScreechingBuzzard;
import com.github.laxika.magicalvibes.cards.w.WirewoodElf;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrownOfAwe.class, ChainOfVapor.class, ElvishWarrior.class, GlorySeeker.class, ScreechingBuzzard.class,
        WirewoodElf.class})
class CrownOfAweTest extends BaseCardTest {

    @Test
    @DisplayName("Casting the Aura on an opponent's creature grants protection only to that creature")
    void castingAuraOnOpposingCreatureGrantsProtection() {
        Permanent warrior = addCreatureReady(player2, new ElvishWarrior());
        Permanent otherElf = addCreatureReady(player1, new WirewoodElf());
        harness.setHand(player1, List.of(new CrownOfAwe()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, warrior.getId());
        harness.passBothPriorities();

        Permanent crown = findPermanent(player1, "Crown of Awe");
        assertThat(crown.getAttachedTo()).isEqualTo(warrior.getId());
        assertThat(gqs.hasProtectionFrom(gd, warrior, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, warrior, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, otherElf, CardColor.BLACK)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, otherElf, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("The sacrifice ability uses the enchanted creature's last known types after it leaves")
    void sacrificeProtectsSharingCreaturesAfterHostLeaves() {
        Permanent warrior = addCreatureReady(player1, new ElvishWarrior());
        Permanent otherElf = addCreatureReady(player2, new WirewoodElf());
        Permanent human = addCreatureReady(player1, new GlorySeeker());
        Permanent crown = attachCrown(warrior);
        harness.setHand(player2, List.of(new ChainOfVapor()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(crown), null, null);
        harness.assertInGraveyard(player1, "Crown of Awe");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, warrior.getId());
        harness.assertInHand(player1, "Elvish Warrior");
        harness.passBothPriorities();

        assertThat(gqs.hasProtectionFrom(gd, otherElf, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, otherElf, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, human, CardColor.BLACK)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, human, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering after the sacrifice ability resolves do not gain protection")
    void protectionDoesNotApplyToCreaturesEnteringLater() {
        Permanent warrior = addCreatureReady(player1, new ElvishWarrior());
        Permanent crown = attachCrown(warrior);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(crown), null, null);
        harness.passBothPriorities();
        Permanent lateElf = harness.enterBattlefieldAndReturn(player2, new WirewoodElf());

        assertThat(gqs.hasProtectionFrom(gd, warrior, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, warrior, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, lateElf, CardColor.BLACK)).isFalse();
        assertThat(gqs.hasProtectionFrom(gd, lateElf, CardColor.RED)).isFalse();
    }

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
