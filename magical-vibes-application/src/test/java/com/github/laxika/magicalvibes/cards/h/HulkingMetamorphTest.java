package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HulkingMetamorphTest extends BaseCardTest {

    @Test
    void prototypeCopyUsesControlledPermanentAndKeepsThreeThree() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        HulkingMetamorph metamorph = new HulkingMetamorph();
        harness.setHand(player1, List.of(metamorph));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of());
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

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of());
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

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null, List.of());
        harness.passBothPriorities();

        Permanent copy = findMetamorph(metamorph);
        assertThat(copy).isNotNull();
        assertThat(copy.getCard().getPower()).isEqualTo(3);
        assertThat(copy.getCard().getToughness()).isEqualTo(3);
    }

    private Permanent findMetamorph(HulkingMetamorph metamorph) {
        GameData gameData = harness.getGameData();
        return gameData.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(metamorph.getId()))
                .findFirst()
                .orElse(null);
    }
}
