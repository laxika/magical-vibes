package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HulkingMetamorph.class, GrizzlyBears.class, AirElemental.class, JayemdaeTome.class})
class HulkingMetamorphTest extends BaseCardTest {

    @Test
    void prototypeCopyUsesControlledPermanentAndKeepsThreeThree() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        HulkingMetamorph metamorph = new HulkingMetamorph();
        harness.setHand(player1, List.of(metamorph));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(ownCreature.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(opponentCreature.getId());

        harness.handlePermanentChosen(player1, ownCreature.getId());

        Permanent copy = findMetamorph(metamorph);
        assertThat(copy).isNotNull();
        assertThat(copy.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(copy.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(copy.getCard().getPower()).isEqualTo(3);
        assertThat(copy.getCard().getToughness()).isEqualTo(3);
    }

    @Test
    void normalCopyUsesSevenSeven() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        HulkingMetamorph metamorph = new HulkingMetamorph();
        harness.setHand(player1, List.of(metamorph));
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, ownCreature.getId());

        Permanent copy = findMetamorph(metamorph);
        assertThat(copy).isNotNull();
        assertThat(copy.getCard().getPower()).isEqualTo(7);
        assertThat(copy.getCard().getToughness()).isEqualTo(7);
    }

    @Test
    void copyingControlledArtifactAddsCreatureType() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new JayemdaeTome());
        HulkingMetamorph metamorph = new HulkingMetamorph();
        harness.setHand(player1, List.of(metamorph));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, ownArtifact.getId());

        Permanent copy = findMetamorph(metamorph);
        assertThat(copy).isNotNull();
        assertThat(copy.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(copy.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(copy.getCard().getPower()).isEqualTo(3);
        assertThat(copy.getCard().getToughness()).isEqualTo(3);
        assertThat(copy.getCard().getActivatedAbilities()).isNotEmpty();
    }

    @Test
    void noControlledArtifactOrCreatureLeavesPrototypeThreeThree() {
        HulkingMetamorph metamorph = new HulkingMetamorph();
        harness.setHand(player1, List.of(metamorph));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent copy = findMetamorph(metamorph);
        assertThat(copy).isNotNull();
        assertThat(copy.getCard().getPower()).isEqualTo(3);
        assertThat(copy.getCard().getToughness()).isEqualTo(3);
    }

    @Test
    void canDeclineCopyDespiteHavingAnEligibleCreature() {
        harness.addToBattlefield(player1, new AirElemental());
        HulkingMetamorph metamorph = new HulkingMetamorph();
        harness.setHand(player1, List.of(metamorph));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent permanent = findMetamorph(metamorph);
        assertThat(permanent).isNotNull();
        assertThat(permanent.getCard().getPower()).isEqualTo(3);
        assertThat(permanent.getCard().getToughness()).isEqualTo(3);
        assertThat(permanent.getCard().getKeywords()).doesNotContain(Keyword.FLYING);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    void copiesFlyingButNotCountersOrTappedStatus() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        original.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        original.tap();
        HulkingMetamorph metamorph = new HulkingMetamorph();
        harness.setHand(player1, List.of(metamorph));
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, original.getId());

        Permanent copy = findMetamorph(metamorph);
        assertThat(copy).isNotNull();
        assertThat(copy.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(copy.getCard().getPower()).isEqualTo(7);
        assertThat(copy.getCard().getToughness()).isEqualTo(7);
        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(copy.isTapped()).isFalse();
        assertThat(original.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(original.isTapped()).isTrue();
    }

    private Permanent findMetamorph(HulkingMetamorph metamorph) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(metamorph.getId()))
                .findFirst()
                .orElse(null);
    }
}
