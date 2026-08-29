package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MendicantCoreGuidelightTest extends BaseCardTest {

    @Test
    void powerEqualsArtifactsYouControl() {
        Permanent mendicant = addCreatureReady(player1, new MendicantCoreGuidelight());
        addCreatureReady(player1, new AlphaMyr());

        assertThat(gqs.getEffectivePower(gd, mendicant)).isEqualTo(2);

        addCreatureReady(player1, new AlphaMyr());

        assertThat(gqs.getEffectivePower(gd, mendicant)).isEqualTo(3);
    }

    @Test
    void maxSpeedCopiesArtifactSpellAsToken() {
        addCreatureReady(player1, new MendicantCoreGuidelight());
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.setHand(player1, List.of(new Ornithopter()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Ornithopter")))
                .hasSize(1);
    }

    @Test
    void artifactSpellDoesNotTriggerBelowMaxSpeed() {
        addCreatureReady(player1, new MendicantCoreGuidelight());
        gd.playerSpeeds.put(player1.getId(), 3);
        harness.setHand(player1, List.of(new Ornithopter()));

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNull();
        assertThat(gd.stack).hasSize(1);
    }
}
