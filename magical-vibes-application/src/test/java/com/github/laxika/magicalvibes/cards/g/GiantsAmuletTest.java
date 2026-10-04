package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AxgardCavalry;
import com.github.laxika.magicalvibes.cards.d.DepartTheRealm;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiantsAmulet.class, AxgardCavalry.class, DepartTheRealm.class})
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
        Permanent giantWizard = addCreatureReady(player1, new AxgardCavalry());
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
        Permanent creature = addCreatureReady(player1, new AxgardCavalry());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(amulet.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Re-equipping moves all bonuses to the new creature")
    void reequippingMovesBonuses() {
        Permanent amulet = addAmuletReady(player1);
        Permanent first = addCreatureReady(player1, new AxgardCavalry());
        Permanent second = addCreatureReady(player1, new AxgardCavalry());
        amulet.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(amulet.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, second, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("A second Amulet is not attached by the first Amulet's trigger")
    void etbAttachesOnlyItsSource() {
        Permanent otherAmulet = addAmuletReady(player1);
        Permanent amulet = castAmuletWithMana(3, 2);

        harness.handleMayAbilityChosen(player1, true);

        Permanent token = findPermanent(player1, "Giant Wizard");
        assertThat(amulet.getAttachedTo()).isEqualTo(token.getId());
        assertThat(otherAmulet.getAttachedTo()).isNull();
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(5);
    }

    @Test
    @DisplayName("Removing the Amulet does not stop its trigger from creating a token")
    void tokenIsCreatedWhenAmuletLeavesBeforeTriggerResolves() {
        harness.setHand(player1, List.of(new GiantsAmulet()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        Permanent amulet = findPermanent(player1, "Giant's Amulet");
        harness.setHand(player2, List.of(new DepartTheRealm()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player2, 0, amulet.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent token = findPermanent(player1, "Giant Wizard");
        assertThat(findPermanents(player1, "Giant's Amulet")).isEmpty();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, token, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Untapping the equipped creature restores hexproof even if the Equipment is tapped")
    void untappingRestoresHexproof() {
        Permanent creature = addCreatureReady(player1, new AxgardCavalry());
        Permanent amulet = addAmuletReady(player1);
        amulet.setAttachedTo(creature.getId());
        amulet.tap();
        creature.tap();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();

        creature.untap();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent cannot target the untapped equipped creature but can target it tapped")
    void hexproofRestrictsOpponentTargeting() {
        Permanent creature = addCreatureReady(player1, new AxgardCavalry());
        Permanent amulet = addAmuletReady(player1);
        amulet.setAttachedTo(creature.getId());
        harness.setHand(player2, List.of(new DepartTheRealm()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        creature.tap();
        harness.castAndResolveInstant(player2, 0, creature.getId());

        assertThat(findPermanents(player1, "Axgard Cavalry")).isEmpty();
        assertThat(amulet.getAttachedTo()).isNull();
    }

    private Permanent castAmuletWithMana(int colorless, int blue) {
        harness.setHand(player1, List.of(new GiantsAmulet()));
        harness.addMana(player1, ManaColor.COLORLESS, colorless);
        harness.addMana(player1, ManaColor.BLUE, blue);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Giant's Amulet");
    }

    private Permanent addAmuletReady(com.github.laxika.magicalvibes.model.Player player) {
        return addCreatureReady(player, new GiantsAmulet());
    }
}
