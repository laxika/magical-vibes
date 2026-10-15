package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SylvokBattleChair.class, GrizzlyBears.class})
class SylvokBattleChairTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Sylvok Battle-Chair creates and equips a 2/2 Rebel token")
    void enteringCreatesAndEquipsRebel() {
        harness.setHand(player1, List.of(new SylvokBattleChair()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent chair = findPermanent(player1, "Sylvok Battle-Chair");
        Permanent rebel = findPermanent(player1, "Rebel");

        assertThat(rebel.getCard().getPower()).isEqualTo(2);
        assertThat(rebel.getCard().getToughness()).isEqualTo(2);
        assertThat(rebel.getCard().getSubtypes()).contains(CardSubtype.REBEL);
        assertThat(chair.getAttachedTo()).isEqualTo(rebel.getId());
    }

    @Test
    @DisplayName("Sylvok Battle-Chair gives the equipped creature +4/+4 and trample")
    void equippedCreatureGetsBoostAndTrample() {
        Permanent chair = addCreatureReady(player1, new SylvokBattleChair());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        chair.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Equipping Sylvok Battle-Chair moves it to another creature")
    void equipMovesChairToAnotherCreature() {
        Permanent chair = addCreatureReady(player1, new SylvokBattleChair());
        Permanent firstCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player1, new GrizzlyBears());
        chair.setAttachedTo(firstCreature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(chair.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("For Mirrodin! still creates a Rebel when the Equipment leaves before resolution")
    void createsRebelAfterChairLeavesBattlefield() {
        harness.setHand(player1, List.of(new SylvokBattleChair()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent chair = findPermanent(player1, "Sylvok Battle-Chair");
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, chair));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sylvok Battle-Chair");
        Permanent rebel = findPermanent(player1, "Rebel");
        assertThat(gqs.getEffectivePower(gd, rebel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rebel)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, rebel, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        Permanent chair = addCreatureReady(player1, new SylvokBattleChair());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(chair.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void cannotEquipDuringCombat() {
        Permanent chair = addCreatureReady(player1, new SylvokBattleChair());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(chair.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
