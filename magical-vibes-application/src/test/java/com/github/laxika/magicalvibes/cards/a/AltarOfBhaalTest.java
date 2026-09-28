package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BoneOffering;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AltarOfBhaal.class, BoneOffering.class, GrizzlyBears.class})
class AltarOfBhaalTest extends BaseCardTest {

    @Test
    @DisplayName("Bone Offering creates a tapped 4/1 black Skeleton with menace")
    void adventureCreatesTappedMenaceSkeleton() {
        AltarOfBhaal altar = new AltarOfBhaal();
        harness.setHand(player1, List.of(altar));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        Permanent skeleton = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(skeleton.getCard().getName()).isEqualTo("Skeleton");
        assertThat(skeleton.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(skeleton.getCard().getSubtypes()).containsExactly(CardSubtype.SKELETON);
        assertThat(gqs.getEffectivePower(gd, skeleton)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, skeleton)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, skeleton, Keyword.MENACE)).isTrue();
        assertThat(skeleton.isTapped()).isTrue();
        assertThat(gd.findExiledCard(altar.getId())).isNotNull();
    }

    @Test
    @DisplayName("Altar of Bhaal exiles a creature and returns a targeted creature from the graveyard")
    void exilesCreatureAndReturnsTargetedCreature() {
        Permanent creatureToExile = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        AltarOfBhaal altar = new AltarOfBhaal();
        Permanent altarPermanent = harness.addToBattlefieldAndReturn(player1, altar);
        altarPermanent.setSummoningSick(false);
        GrizzlyBears creatureToReturn = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creatureToReturn));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 1, 0, null, creatureToReturn.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(creatureToExile.getCard().getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creatureToReturn);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .contains(creatureToReturn);
        assertThat(altarPermanent.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Altar of Bhaal cannot target a noncreature card")
    void cannotTargetNoncreatureCard() {
        Permanent altar = harness.addToBattlefieldAndReturn(player1, new AltarOfBhaal());
        altar.setSummoningSick(false);
        BoneOffering noncreature = new BoneOffering();
        harness.setGraveyard(player1, List.of(noncreature));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, noncreature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }
}
