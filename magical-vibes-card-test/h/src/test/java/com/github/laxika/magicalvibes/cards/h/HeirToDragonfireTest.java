package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HeirToDragonfire.class})
class HeirToDragonfireTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability gives +1/+0 until end of turn")
    void activatedAbilityBoostsUntilEndOfTurn() {
        Permanent heir = harness.addToBattlefieldAndReturn(player1, new HeirToDragonfire());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(heir.getEffectivePower()).isEqualTo(3);
        assertThat(heir.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(heir.getEffectivePower()).isEqualTo(2);
        assertThat(heir.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Hand ability keeps the card in hand and perpetually upgrades it")
    void handAbilityPerpetuallyUpgradesCard() {
        harness.setHand(player1, List.of(new HeirToDragonfire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Heir to Dragonfire");

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent heir = findPermanent(player1, "Heir to Dragonfire");
        assertThat(gqs.getEffectivePower(gd, heir)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, heir)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, heir, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, heir, CardSubtype.DRAGON)).isTrue();
    }

    @Test
    @DisplayName("Becoming a Dragon replaces Human and Warlock in hand and on the battlefield")
    void becomingDragonReplacesPreviousCreatureTypes() {
        HeirToDragonfire card = new HeirToDragonfire();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gqs.cardHasSubtype(card, CardSubtype.DRAGON, gd, player1.getId())).isTrue();
        assertThat(gqs.cardHasSubtype(card, CardSubtype.HUMAN, gd, player1.getId())).isFalse();
        assertThat(gqs.cardHasSubtype(card, CardSubtype.WARLOCK, gd, player1.getId())).isFalse();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent heir = findPermanent(player1, "Heir to Dragonfire");
        assertThat(gqs.hasEffectiveSubtype(gd, heir, CardSubtype.DRAGON)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, heir, CardSubtype.HUMAN)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, heir, CardSubtype.WARLOCK)).isFalse();
    }

    @Test
    @DisplayName("Repeated hand activations stack and perpetual boosts survive cleanup")
    void repeatedHandActivationsStackAndSurviveCleanup() {
        harness.setHand(player1, List.of(new HeirToDragonfire()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateHandAbility(player1, 0, null);
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent heir = findPermanent(player1, "Heir to Dragonfire");
        assertThat(gqs.getEffectivePower(gd, heir)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, heir)).isEqualTo(8);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, heir)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, heir)).isEqualTo(8);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, heir)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, heir)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, heir, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, heir, CardSubtype.DRAGON)).isTrue();
    }

    @Test
    @DisplayName("The hand ability upgrades only the revealed copy")
    void handAbilityDoesNotUpgradeAnotherCopy() {
        HeirToDragonfire upgraded = new HeirToDragonfire();
        HeirToDragonfire other = new HeirToDragonfire();
        harness.setHand(player1, List.of(upgraded, other));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 1);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent otherPermanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(other.getId()))
                .findFirst().orElseThrow();
        Permanent upgradedPermanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(upgraded.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, otherPermanent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherPermanent)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, otherPermanent, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, otherPermanent, CardSubtype.DRAGON)).isFalse();
        assertThat(gqs.getEffectivePower(gd, upgradedPermanent)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, upgradedPermanent)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, upgradedPermanent, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, upgradedPermanent, CardSubtype.DRAGON)).isTrue();
    }
}
