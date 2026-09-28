package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({PreWarFormalwear.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class PreWarFormalwearTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns and attaches a target creature card with mana value 3 or less")
    void etbReturnsAndAttachesTargetCreature() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        castFormalwear();

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        Permanent formalwear = findPermanent(player1, "Pre-War Formalwear");
        Permanent returned = findPermanentByCardId(bears.getId());
        assertThat(formalwear.getAttachedTo()).isEqualTo(returned.getId());
        assertThat(returned.getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.VIGILANCE)).isTrue();
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB only offers creature cards with mana value 3 or less")
    void etbFiltersInvalidGraveyardCards() {
        GrizzlyBears bears = new GrizzlyBears();
        HillGiant giant = new HillGiant();
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(giant, shock, bears));
        castFormalwear();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(bears.getId());
    }

    @Test
    @DisplayName("Equip {3} moves Pre-War Formalwear and its boost to another creature")
    void equipMovesToAnotherCreature() {
        Permanent firstCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent formalwear = harness.addToBattlefieldAndReturn(player1, new PreWarFormalwear());
        formalwear.setAttachedTo(firstCreature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int equipmentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(formalwear);
        harness.activateAbility(player1, equipmentIndex, null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(formalwear.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.VIGILANCE)).isTrue();
    }

    private void castFormalwear() {
        harness.setHand(player1, List.of(new PreWarFormalwear()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent findPermanentByCardId(java.util.UUID cardId) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElseThrow();
    }
}
