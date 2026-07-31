package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HauntedPlateMailTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +4/+4")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent mail = addMailReady(player1);
        mail.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
    }

    @Test
    @DisplayName("Equip attaches to target creature")
    void equipAttaches() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
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
        addCreatureReady(player1, new GrizzlyBears());

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

        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
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
        Permanent perm = new Permanent(new HauntedPlateMail());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
