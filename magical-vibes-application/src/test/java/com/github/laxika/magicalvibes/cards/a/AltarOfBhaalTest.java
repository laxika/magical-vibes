package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BoneOffering;
import com.github.laxika.magicalvibes.cards.l.LurkingRoper;
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

@CardUsed({AltarOfBhaal.class, BoneOffering.class, LurkingRoper.class})
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
        Permanent creatureToExile = harness.addToBattlefieldAndReturn(player1, new LurkingRoper());
        AltarOfBhaal altar = new AltarOfBhaal();
        Permanent altarPermanent = addCreatureReady(player1, altar);
        LurkingRoper creatureToReturn = new LurkingRoper();
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
        addCreatureReady(player1, new AltarOfBhaal());
        harness.addToBattlefield(player1, new LurkingRoper());
        AltarOfBhaal noncreature = new AltarOfBhaal();
        harness.setGraveyard(player1, List.of(noncreature));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, noncreature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canCastArtifactAfterAdventureAndExileItsTokenToReturnCreature() {
        AltarOfBhaal altar = new AltarOfBhaal();
        LurkingRoper target = new LurkingRoper();
        harness.setHand(player1, List.of(altar));
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        Permanent skeleton = findPermanent(player1, "Skeleton");

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, altar.getId());
        harness.passBothPriorities();
        assertThat(gd.findExiledCard(altar.getId())).isNull();
        assertThat(countPermanents(player1, "Skeleton")).isEqualTo(1);
        Permanent artifact = findPermanent(player1, "Altar of Bhaal");
        assertThat(artifact.isTapped()).isFalse();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 1, 0, null, target.getId(), Zone.GRAVEYARD);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(skeleton);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target);
        assertThat(artifact.isTapped()).isTrue();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).contains(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target);
    }

    @Test
    void cannotTargetOpponentsGraveyard() {
        LurkingRoper target = prepareActivation();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(target));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Altar of Bhaal").isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Lurking Roper");
    }

    @Test
    void cannotActivateOutsideMainPhase() {
        LurkingRoper target = prepareActivation();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateDuringOpponentsMainPhase() {
        LurkingRoper target = prepareActivation();
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithNonemptyStack() {
        LurkingRoper target = prepareActivation();
        harness.setHand(player1, List.of(new AltarOfBhaal()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateTappedAltar() {
        LurkingRoper target = prepareActivation();
        findPermanent(player1, "Altar of Bhaal").tap();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Lurking Roper");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target);
    }

    @Test
    void cannotExileOpponentsCreatureToPayCost() {
        LurkingRoper target = prepareActivation();
        gd.playerBattlefields.get(player1.getId()).remove(1);
        harness.addToBattlefield(player2, new LurkingRoper());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Lurking Roper");
    }

    @Test
    void removedTargetDoesNotReturnAndCostsRemainPaid() {
        LurkingRoper target = prepareActivation();
        Permanent costCreature = findPermanent(player1, "Lurking Roper");
        harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD);
        assertThat(gd.findExiledCard(costCreature.getCard().getId())).isNotNull();
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(costCreature.getCard(), target));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lurking Roper");
        assertThat(findPermanent(player1, "Altar of Bhaal").isTapped()).isTrue();
        assertThat(gd.findExiledCard(target.getId())).isNotNull();
    }

    @Test
    void choosesWhichCreatureToExileWhenSeveralAreAvailable() {
        LurkingRoper target = prepareActivation();
        Permanent unchosen = findPermanent(player1, "Lurking Roper");
        Permanent chosen = harness.addToBattlefieldAndReturn(player1, new LurkingRoper());

        harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.handlePermanentChosen(player1, chosen.getId());

        assertThat(gd.findExiledCard(chosen.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(unchosen).doesNotContain(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).contains(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target);
    }

    @Test
    void abilityStillReturnsCreatureAfterAltarLeavesBattlefield() {
        LurkingRoper target = prepareActivation();
        Permanent altar = findPermanent(player1, "Altar of Bhaal");
        harness.activateAbility(player1, 0, 0, null, target.getId(), Zone.GRAVEYARD);
        gd.playerBattlefields.get(player1.getId()).remove(altar);
        harness.setGraveyard(player1, List.of(target, altar.getCard()));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Altar of Bhaal");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).contains(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(altar.getCard()).doesNotContain(target);
    }

    @Test
    void insufficientManaDoesNotExileCreatureOrTapAltar() {
        Permanent altar = harness.addToBattlefieldAndReturn(player1, new AltarOfBhaal());
        Permanent costCreature = harness.addToBattlefieldAndReturn(player1, new LurkingRoper());
        LurkingRoper target = new LurkingRoper();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        assertThat(altar.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(costCreature);
        assertThat(gd.findExiledCard(costCreature.getCard().getId())).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target);
    }

    private LurkingRoper prepareActivation() {
        harness.addToBattlefield(player1, new AltarOfBhaal());
        harness.addToBattlefield(player1, new LurkingRoper());
        LurkingRoper target = new LurkingRoper();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return target;
    }
}
