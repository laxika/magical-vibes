package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.n.Nefashu;
import com.github.laxika.magicalvibes.cards.s.Stabilizer;
import com.github.laxika.magicalvibes.cards.t.TreetopScout;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonShadow.class, Nefashu.class, Stabilizer.class, TreetopScout.class})
class DragonShadowTest extends BaseCardTest {

    @Test
    void resolvingAuraBoostsEnchantedCreatureAndGrantsFear() {
        Permanent scout = addCreatureReady(player1, new TreetopScout());
        harness.setHand(player1, List.of(new DragonShadow()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, scout.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Dragon Shadow");
        assertThat(aura.getAttachedTo()).isEqualTo(scout.getId());
        assertThat(gqs.getEffectivePower(gd, scout)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, scout)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, scout, Keyword.FEAR)).isTrue();
    }

    @Test
    void creatureWithManaValueSixEnteringUnderAnyPlayersControlTriggersReturn() {
        DragonShadow shadow = new DragonShadow();
        harness.setGraveyard(player1, List.of(shadow));
        Permanent creature = harness.enterBattlefieldAndReturn(player2, new Nefashu());

        resolveMayAbility(true);

        Permanent returnedShadow = findPermanent(player1, "Dragon Shadow");
        assertThat(returnedShadow.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FEAR)).isTrue();
        harness.assertNotInGraveyard(player1, "Dragon Shadow");
    }

    @Test
    void smallerCreatureDoesNotTriggerReturn() {
        harness.setGraveyard(player1, List.of(new DragonShadow()));
        harness.enterBattlefieldAndReturn(player1, new TreetopScout());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Dragon Shadow");
    }

    @Test
    void decliningReturnKeepsAuraInGraveyard() {
        harness.setGraveyard(player1, List.of(new DragonShadow()));
        harness.enterBattlefieldAndReturn(player1, new Nefashu());

        resolveMayAbility(false);

        harness.assertInGraveyard(player1, "Dragon Shadow");
        harness.assertNotOnBattlefield(player1, "Dragon Shadow");
    }

    @Test
    void acceptedReturnEntersUnattachedWhenEnteringCreatureLeavesBeforeResolution() {
        harness.setGraveyard(player1, List.of(new DragonShadow()));
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new Nefashu());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gameLogContains("Dragon Shadow returns to the battlefield unattached.")).isTrue();
        harness.assertInGraveyard(player1, "Dragon Shadow");
        harness.assertNotOnBattlefield(player1, "Dragon Shadow");
    }

    @Test
    void cannotEnchantNonCreaturePermanent() {
        harness.addToBattlefield(player1, new Stabilizer());
        harness.setHand(player1, List.of(new DragonShadow()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent artifact = findPermanent(player1, "Stabilizer");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void resolveMayAbility(boolean accepted) {
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, accepted);
        resolveAllTriggers();
    }
}
