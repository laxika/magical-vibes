package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SkyhunterProwler;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PreWarFormalwear.class, GrizzlyBears.class, SerraAngel.class, SkyhunterProwler.class})
class PreWarFormalwearTest extends BaseCardTest {

    @Test
    void returnsAndAttachesEligibleCreatureFromGraveyard() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.castFromHand(player1, new PreWarFormalwear(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        Permanent formalwear = findPermanent(player1, "Pre-War Formalwear");
        assertThat(formalwear.getAttachedTo()).isEqualTo(returned.getId());
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void doesNotReturnCreatureWithManaValueGreaterThanThree() {
        SerraAngel angel = new SerraAngel();
        harness.setGraveyard(player1, List.of(angel));
        harness.castFromHand(player1, new PreWarFormalwear(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Serra Angel");
    }

    @Test
    void equipAttachesFormalwearToCreature() {
        Permanent formalwear = harness.addToBattlefieldAndReturn(player1, new PreWarFormalwear());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(formalwear.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void returnsCreatureWithManaValueExactlyThree() {
        SkyhunterProwler prowler = new SkyhunterProwler();
        harness.setGraveyard(player1, List.of(prowler));
        harness.castFromHand(player1, new PreWarFormalwear(), "{2}{W}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(prowler.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Skyhunter Prowler");
        assertThat(findPermanent(player1, "Pre-War Formalwear").getAttachedTo()).isEqualTo(returned.getId());
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(5);
        harness.assertNotInGraveyard(player1, "Skyhunter Prowler");
    }

    @Test
    void doesNotTargetOpponentsGraveyardOrNoncreatureCards() {
        harness.setGraveyard(player1, List.of(new PreWarFormalwear()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.castFromHand(player1, new PreWarFormalwear(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Pre-War Formalwear");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanent(player1, "Pre-War Formalwear").getAttachedTo()).isNull();
    }

    @Test
    void returnsCreatureEvenIfEquipmentLeavesBeforeTriggerResolves() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.castFromHand(player1, new PreWarFormalwear(), "{2}{W}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        Permanent formalwear = findPermanent(player1, "Pre-War Formalwear");
        gd.playerBattlefields.get(player1.getId()).remove(formalwear);
        gd.playerGraveyards.get(player1.getId()).add(formalwear.getCard());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.VIGILANCE)).isFalse();
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Pre-War Formalwear");
    }

    @Test
    void doesNotReturnTargetThatLeavesGraveyardBeforeResolution() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.castFromHand(player1, new PreWarFormalwear(), "{2}{W}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(bears.getId()));
        assertThat(findPermanent(player1, "Pre-War Formalwear").getAttachedTo()).isNull();
    }

    @Test
    void reequippingMovesBonusesToNewCreature() {
        Permanent formalwear = harness.addToBattlefieldAndReturn(player1, new PreWarFormalwear());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, first.getId());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(formalwear.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, second, Keyword.VIGILANCE)).isTrue();
    }
}
