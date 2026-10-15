package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HauntedPlateMail.class, RuneclawBear.class})
class HauntedPlateMailTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +4/+4")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent mail = addMailReady(player1);
        mail.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
    }

    @Test
    @DisplayName("Equip attaches to target creature")
    void equipAttaches() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent mail = addMailReady(player1);
        int mailIdx = gd.playerBattlefields.get(player1.getId()).indexOf(mail);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, mailIdx, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(mail.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Animation ability makes it a 4/4 Spirit creature and removes Equipment")
    void animationMakesCreatureAndRemovesEquipment() {
        Permanent mail = addMailReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, mail)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mail)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mail)).isEqualTo(4);
        assertThat(mail.getTransientSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(mail.getTransientRemovedSubtypes()).contains(CardSubtype.EQUIPMENT);
        assertThat(GameQueryService.permanentHasSubtype(mail, CardSubtype.EQUIPMENT)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate animation while controlling a creature")
    void cannotAnimateWhileControllingCreature() {
        addMailReady(player1);
        addCreatureReady(player1, new RuneclawBear());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Activate only if you control no creatures");
    }

    @Test
    @DisplayName("Equip while animated has no effect")
    void equipWhileAnimatedHasNoEffect() {
        Permanent mail = addMailReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, mail)).isTrue();

        Permanent ownBear = addCreatureReady(player1, new RuneclawBear());
        int mailIdx = gd.playerBattlefields.get(player1.getId()).indexOf(mail);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, mailIdx, 1, null, ownBear.getId());
        harness.passBothPriorities();

        assertThat(mail.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Animation and Equipment loss wear off at end of turn")
    void animationWearsOffAtEndOfTurn() {
        Permanent mail = addMailReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, mail)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(mail, CardSubtype.EQUIPMENT)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mail.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, mail)).isFalse();
        assertThat(mail.getTransientRemovedSubtypes()).isEmpty();
        assertThat(GameQueryService.permanentHasSubtype(mail, CardSubtype.EQUIPMENT)).isTrue();
    }

    private Permanent addMailReady(Player player) {
        return addCreatureReady(player, new HauntedPlateMail());
    }

    @Test
    @DisplayName("Two Mails can animate in response to each other")
    void bothMailsAnimateWhenActivatedBeforeEitherResolves() {
        Permanent first = addMailReady(player1);
        Permanent second = addMailReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 1, 0, null, null);
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, first)).isTrue();
        assertThat(gqs.isCreature(gd, second)).isTrue();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        assertThat(gqs.hasEffectiveSubtype(gd, first, CardSubtype.EQUIPMENT)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, second, CardSubtype.EQUIPMENT)).isFalse();
    }

    @Test
    @DisplayName("A creature arriving after activation does not prevent animation")
    void creatureArrivingBeforeResolutionDoesNotPreventAnimation() {
        Permanent mail = addMailReady(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        addCreatureReady(player1, new RuneclawBear());

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, mail)).isTrue();
        assertThat(gqs.isArtifact(gd, mail)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, mail, CardSubtype.SPIRIT)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mail)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mail)).isEqualTo(4);
    }

    @Test
    @DisplayName("Opponent's creatures do not prevent animation on their turn")
    void canAnimateOnOpponentsTurnWithOpponentsCreature() {
        Permanent mail = addMailReady(player1);
        addCreatureReady(player2, new RuneclawBear());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.ensurePriority(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, mail)).isTrue();
        assertThat(gqs.getEffectivePower(gd, mail)).isEqualTo(4);
    }

    @Test
    @DisplayName("An animated Mail itself prevents another animation activation")
    void animatedMailCountsAsControlledCreature() {
        addMailReady(player1);
        addMailReady(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Activate only if you control no creatures");
    }

    @Test
    @DisplayName("Animation does not let a Mail that entered this turn attack")
    void newlyEnteredMailCannotAttackAfterAnimation() {
        Permanent mail = harness.addToBattlefieldAndReturn(player1, new HauntedPlateMail());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, mail)).isTrue();
        assertThat(als.canAttack(gd, mail, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        Permanent mail = addMailReady(player1);
        Permanent creature = addCreatureReady(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mail.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void cannotEquipDuringCombat() {
        Permanent mail = addMailReady(player1);
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(mail.getAttachedTo()).isNull();
    }
}
