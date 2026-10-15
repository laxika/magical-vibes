package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RedXIIIProudWarrior.class, GrizzlyBears.class, Bonesplitter.class, HolyStrength.class})
class RedXIIIProudWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Other modified creatures you control gain vigilance and trample")
    void modifiedCreaturesGainVigilanceAndTrample() {
        addCreatureReady(player1, new RedXIIIProudWarrior());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        equipment.setAttachedTo(bears.getId());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Creatures lose the granted keywords when they are no longer modified")
    void losesGrantedKeywordsWhenUnmodified() {
        addCreatureReady(player1, new RedXIIIProudWarrior());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        equipment.setAttachedTo(bears.getId());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();

        equipment.setAttachedTo(null);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cosmo Memory returns a target Aura or Equipment from your graveyard to hand")
    void returnsTargetAuraOrEquipmentFromOwnGraveyard() {
        HolyStrength aura = new HolyStrength();
        Bonesplitter equipment = new Bonesplitter();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(aura, equipment, creature));
        harness.setGraveyard(player2, List.of(new Bonesplitter()));
        harness.castFromHand(player1, new RedXIIIProudWarrior(), "{1}{R}{G}");

        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(aura.getId(), equipment.getId());

        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Holy Strength");
        harness.assertInGraveyard(player1, "Bonesplitter");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Bonesplitter");
    }

    @Test
    void unmodifiedAndOpposingModifiedCreaturesDoNotGainKeywords() {
        addCreatureReady(player1, new RedXIIIProudWarrior());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBears = addCreatureReady(player2, new GrizzlyBears());
        opposingBears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingBears, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingBears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void nonStatCounterGrantsKeywordsUntilRemoved() {
        addCreatureReady(player1, new RedXIIIProudWarrior());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.STUN, 1);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();

        bears.setCounterCount(CounterType.STUN, 0);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void ownAuraModifiesCreatureButOpposingAuraDoesNot() {
        addCreatureReady(player1, new RedXIIIProudWarrior());
        Permanent ownEnchantedBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingEnchantedBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent ownAura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        Permanent opposingAura = harness.addToBattlefieldAndReturn(player2, new HolyStrength());
        ownAura.setAttachedTo(ownEnchantedBears.getId());
        opposingAura.setAttachedTo(opposingEnchantedBears.getId());

        assertThat(gqs.hasKeyword(gd, ownEnchantedBears, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownEnchantedBears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingEnchantedBears, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingEnchantedBears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void opposingEquipmentStillModifiesOwnCreature() {
        addCreatureReady(player1, new RedXIIIProudWarrior());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new Bonesplitter());
        equipment.setAttachedTo(bears.getId());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void returnsEquipmentToHand() {
        Bonesplitter equipment = new Bonesplitter();
        harness.setGraveyard(player1, List.of(equipment));
        harness.castFromHand(player1, new RedXIIIProudWarrior(), "{1}{R}{G}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(equipment.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Bonesplitter");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void removedGraveyardTargetDoesNotReturnAnotherCard() {
        HolyStrength aura = new HolyStrength();
        Bonesplitter equipment = new Bonesplitter();
        harness.setGraveyard(player1, List.of(aura, equipment));
        harness.castFromHand(player1, new RedXIIIProudWarrior(), "{1}{R}{G}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));
        harness.setGraveyard(player1, List.of(equipment));
        harness.setExile(player1, List.of(aura));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Bonesplitter");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void entersWithoutLegalGraveyardTargets() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new HolyStrength(), new Bonesplitter()));
        harness.castFromHand(player1, new RedXIIIProudWarrior(), "{1}{R}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Red XIII, Proud Warrior");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Holy Strength");
        harness.assertInGraveyard(player2, "Bonesplitter");
    }
}
