package com.github.laxika.magicalvibes.cards.i;

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

@CardUsed({IllvoiLightJammer.class, IntrepidTenderfoot.class})
class IllvoiLightJammerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters attached to a creature you control and grants it hexproof")
    void entersAttachedAndGrantsHexproof() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot());
        castIllvoiLightJammer(creature);

        Permanent jammer = findPermanent(player1, "Illvoi Light Jammer");
        assertThat(jammer.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Granted hexproof wears off at end of turn while the equipped bonus remains")
    void hexproofWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot());
        castIllvoiLightJammer(creature);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Equip attaches Illvoi Light Jammer to another creature you control")
    void equipAttachesToAnotherCreature() {
        Permanent jammer = harness.addToBattlefieldAndReturn(player1, new IllvoiLightJammer());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot());
        jammer.setAttachedTo(firstCreature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(jammer.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, secondCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("ETB cannot target an opponent's creature")
    void etbCannotTargetOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new IntrepidTenderfoot());
        harness.setHand(player1, List.of(new IllvoiLightJammer()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBeCastDuringOpponentsTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.ensurePriority(player1);

        castIllvoiLightJammer(creature);

        assertThat(findPermanent(player1, "Illvoi Light Jammer").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void canEnterWithoutAnyCreaturesToAttachTo() {
        prepareJammer();
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Illvoi Light Jammer").getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void grantsHexproofEvenIfEquipmentLeavesBeforeTriggerResolves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot());
        prepareJammer();
        harness.castArtifact(player1, 0, creature.getId());
        harness.passBothPriorities();
        Permanent jammer = findPermanent(player1, "Illvoi Light Jammer");
        gd.playerBattlefields.get(player1.getId()).remove(jammer);
        gd.playerGraveyards.get(player1.getId()).add(jammer.getCard());

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void triggerDoesNothingIfTargetLeavesBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot());
        prepareJammer();
        harness.castArtifact(player1, 0, creature.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());

        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Illvoi Light Jammer").getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void prepareJammer() {
        harness.setHand(player1, List.of(new IllvoiLightJammer()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private void castIllvoiLightJammer(Permanent target) {
        prepareJammer();
        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

}
