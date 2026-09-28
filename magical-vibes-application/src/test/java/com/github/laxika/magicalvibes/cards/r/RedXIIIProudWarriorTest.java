package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.model.Card;
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
        harness.setHand(player1, List.of(new RedXIIIProudWarrior()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
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
}
