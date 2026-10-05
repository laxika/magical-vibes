package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.g.GloryheathLynx;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MendicantCoreGuidelight.class, AlphaMyr.class, Ornithopter.class, GloryheathLynx.class})
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

        assertThat(findPermanents(player1, "Ornithopter").stream()
                .filter(permanent -> permanent.getCard().isToken()))
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

    @Test
    void opponentsArtifactsDoNotIncreasePower() {
        Permanent mendicant = addCreatureReady(player1, new MendicantCoreGuidelight());
        addCreatureReady(player2, new AlphaMyr());

        assertThat(gqs.getEffectivePower(gd, mendicant)).isEqualTo(1);
    }

    @Test
    void decliningPaymentDoesNotCopyArtifactSpell() {
        addCreatureReady(player1, new MendicantCoreGuidelight());
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.setHand(player1, List.of(new Ornithopter()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Ornithopter"))
                .hasSize(1)
                .allSatisfy(permanent -> assertThat(permanent.getCard().isToken()).isFalse());
    }

    @Test
    void opponentsArtifactSpellDoesNotTriggerCopy() {
        addCreatureReady(player1, new MendicantCoreGuidelight());
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Ornithopter()));

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(countPermanents(player2, "Ornithopter")).isEqualTo(1);
        assertThat(countPermanents(player1, "Ornithopter")).isZero();
    }

    @Test
    void enteringBattlefieldStartsEngines() {
        harness.setHand(player1, List.of(new MendicantCoreGuidelight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Mendicant Core, Guidelight");
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void nonartifactSpellDoesNotTriggerAtMaxSpeed() {
        Permanent mendicant = addCreatureReady(player1, new MendicantCoreGuidelight());
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.setHand(player1, List.of(new GloryheathLynx()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Gloryheath Lynx")).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, mendicant)).isEqualTo(1);
    }

    @Test
    void paidCopyAndOriginalBothResolveAndIncreasePower() {
        Permanent mendicant = addCreatureReady(player1, new MendicantCoreGuidelight());
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.setHand(player1, List.of(new AlphaMyr()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Alpha Myr")).hasSize(2);
        assertThat(findPermanents(player1, "Alpha Myr").stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, mendicant)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }
}
