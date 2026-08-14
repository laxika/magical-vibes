package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EnsouledScimitarTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+5")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scimitar = addScimitarReady(player1);
        scimitar.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);
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
    @DisplayName("Equip attaches the Scimitar to a creature")
    void equipAttaches() {
        Permanent scimitar = addScimitarReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(scimitar.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addScimitarReady(Player player) {
        Permanent permanent = new Permanent(new EnsouledScimitar());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
