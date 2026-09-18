package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.Arachnoid;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({EnsouledScimitar.class, Arachnoid.class})
class EnsouledScimitarTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+5")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new Arachnoid());
        Permanent scimitar = addScimitarReady(player1);
        scimitar.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(11);
    }

    @Test
    @DisplayName("Animation makes the Scimitar a 1/5 Spirit artifact creature with flying")
    void animationMakesCreature() {
        Permanent scimitar = addScimitarReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, scimitar)).isTrue();
        assertThat(gqs.isArtifact(scimitar)).isTrue();
        assertThat(GameQueryService.permanentHasSubtype(scimitar, CardSubtype.EQUIPMENT)).isTrue();
        assertThat(gqs.getEffectivePower(gd, scimitar)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, scimitar)).isEqualTo(5);
        assertThat(GameQueryService.permanentHasSubtype(scimitar, CardSubtype.SPIRIT)).isTrue();
        assertThat(gqs.hasKeyword(gd, scimitar, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Animation ends at the end of the turn")
    void animationEndsAtEndOfTurn() {
        Permanent scimitar = addScimitarReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, scimitar)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, scimitar)).isFalse();
        assertThat(GameQueryService.permanentHasSubtype(scimitar, CardSubtype.SPIRIT)).isFalse();
        assertThat(gqs.hasKeyword(gd, scimitar, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Animating the Scimitar unattaches it from its equipped creature")
    void animationUnattachesFromCreature() {
        Permanent scimitar = addScimitarReady(player1);
        Permanent creature = addCreatureReady(player1, new Arachnoid());
        scimitar.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(scimitar.getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
    }

    @Test
    @DisplayName("An animated Scimitar cannot equip a creature")
    void animatedScimitarCannotEquipCreature() {
        Permanent scimitar = addScimitarReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent creature = addCreatureReady(player1, new Arachnoid());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(scimitar.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip attaches the Scimitar to a creature")
    void equipAttaches() {
        Permanent scimitar = addScimitarReady(player1);
        Permanent creature = addCreatureReady(player1, new Arachnoid());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(scimitar.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addScimitarReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new EnsouledScimitar());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
