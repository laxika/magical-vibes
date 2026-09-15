package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
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

@CardUsed({CrownOfAscension.class, ElvishWarrior.class, GlorySeeker.class, WirewoodElf.class, WirewoodLodge.class})
class CrownOfAscensionTest extends BaseCardTest {

    @Test
    @DisplayName("Can enchant a creature")
    void canEnchantCreature() {
        Permanent warrior = addCreatureReady(player2, new ElvishWarrior());
        CrownOfAscension auraCard = new CrownOfAscension();
        harness.setHand(player1, List.of(auraCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, warrior.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() == auraCard)
                .singleElement()
                .extracting(Permanent::getAttachedTo)
                .isEqualTo(warrior.getId());
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent lodge = harness.addToBattlefieldAndReturn(player2, new WirewoodLodge());
        harness.setHand(player1, List.of(new CrownOfAscension()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, lodge.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanted creature has flying")
    void enchantedCreatureHasFlying() {
        Permanent warrior = addCreatureReady(player1, new ElvishWarrior());
        attachCrown(warrior);

        assertThat(gqs.hasKeyword(gd, warrior, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Sacrificing the Aura gives flying to the enchanted creature and creatures sharing its type")
    void sacrificeGivesFlyingToEnchantedAndSharingCreatures() {
        Permanent warrior = addCreatureReady(player1, new ElvishWarrior());
        Permanent otherElf = addCreatureReady(player2, new WirewoodElf());
        Permanent human = addCreatureReady(player1, new GlorySeeker());
        Permanent otherHuman = addCreatureReady(player2, new GlorySeeker());
        Permanent crown = attachCrown(warrior);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(crown), null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, warrior, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherElf, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, human, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherHuman, Keyword.FLYING)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(crown);
    }

    @Test
    @DisplayName("Only creatures on the battlefield when the ability resolves gain flying")
    void sacrificeAbilityDoesNotAffectCreaturesEnteringLater() {
        Permanent warrior = addCreatureReady(player1, new ElvishWarrior());
        Permanent crown = attachCrown(warrior);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(crown), null, null);
        harness.passBothPriorities();

        Permanent lateElf = addCreatureReady(player2, new WirewoodElf());

        assertThat(gqs.hasKeyword(gd, lateElf, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Sacrifice flying wears off at end of turn")
    void sacrificeFlyingWearsOffAtEndOfTurn() {
        Permanent warrior = addCreatureReady(player1, new ElvishWarrior());
        Permanent otherElf = addCreatureReady(player2, new WirewoodElf());
        Permanent crown = attachCrown(warrior);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(crown), null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, otherElf, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, warrior, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherElf, Keyword.FLYING)).isFalse();
    }

    private Permanent attachCrown(Permanent host) {
        Permanent crown = harness.addToBattlefieldAndReturn(player1, new CrownOfAscension());
        crown.setAttachedTo(host.getId());
        return crown;
    }
}
