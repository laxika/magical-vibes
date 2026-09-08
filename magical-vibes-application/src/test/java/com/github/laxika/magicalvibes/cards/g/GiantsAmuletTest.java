package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GiantsAmuletTest extends BaseCardTest {

    @Test
    @DisplayName("Paying the ETB cost creates and equips a Giant Wizard")
    void payingEtbCostCreatesAndEquipsGiantWizard() {
        Permanent amulet = castAmuletWithMana(3, 2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent giantWizard = findPermanents(player1, "Giant Wizard").getFirst();
        assertThat(amulet.getAttachedTo()).isEqualTo(giantWizard.getId());
        assertThat(gqs.getEffectivePower(gd, giantWizard)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, giantWizard)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, giantWizard, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Declining the ETB cost creates no Giant Wizard")
    void decliningEtbCostCreatesNoGiantWizard() {
        Permanent amulet = castAmuletWithMana(3, 2);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Giant Wizard")).isEmpty();
        assertThat(amulet.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Hexproof is present only while the equipped creature is untapped")
    void hexproofRequiresEquippedCreatureToBeUntapped() {
        Permanent giantWizard = addCreatureReady(player1, new GrizzlyBears());
        Permanent amulet = addAmuletReady(player1);
        amulet.setAttachedTo(giantWizard.getId());

        assertThat(gqs.getEffectiveToughness(gd, giantWizard)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, giantWizard, Keyword.HEXPROOF)).isTrue();

        giantWizard.tap();

        assertThat(gqs.getEffectiveToughness(gd, giantWizard)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, giantWizard, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Equip {2} attaches Giant's Amulet to a creature you control")
    void equipAttachesAmulet() {
        Permanent amulet = addAmuletReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(amulet.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
    }

    private Permanent castAmuletWithMana(int colorless, int blue) {
        harness.setHand(player1, List.of(new GiantsAmulet()));
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
        harness.addMana(player1, ManaColor.BLUE, blue);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof GiantsAmulet)
                .findFirst()
                .orElseThrow();
    }

    private Permanent addAmuletReady(com.github.laxika.magicalvibes.model.Player player) {
        Permanent permanent = new Permanent(new GiantsAmulet());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
