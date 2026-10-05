package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.m.MuragandaPetroglyphs;
import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.cards.v.VeilstoneAmulet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuietDisrepair.class, VeilstoneAmulet.class, MuragandaPetroglyphs.class, NessianCourser.class})
class QuietDisrepairTest extends BaseCardTest {

    private static final String DESTROY_MODE = "Destroy enchanted permanent.";
    private static final String GAIN_LIFE_MODE = "You gain 2 life.";

    @Test
    void cannotEnchantCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new NessianCourser());
        harness.setHand(player1, List.of(new QuietDisrepair()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or enchantment");
    }

    @Test
    void destroyModeDestroysEnchantedArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new VeilstoneAmulet());
        Permanent aura = castOn(artifact);

        advanceToUpkeep(player1);
        harness.handleListChoice(player1, DESTROY_MODE);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(artifact.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
    }

    @Test
    void gainLifeModeLeavesEnchantedEnchantmentOnTheBattlefield() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new MuragandaPetroglyphs());
        Permanent aura = castOn(enchantment);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.handleListChoice(player1, GAIN_LIFE_MODE);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchantment);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new MuragandaPetroglyphs());
        Permanent aura = castOn(enchantment);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchantment);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
    }

    @Test
    void choosesModeBeforePlayersCanRespondAndGainsLifeOnlyOnResolution() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new VeilstoneAmulet());
        castOn(artifact);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, GAIN_LIFE_MODE);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
    }

    @Test
    void destroyModeCanDestroyAnEnchantmentYouControl() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new MuragandaPetroglyphs());
        Permanent aura = castOn(enchantment);

        advanceToUpkeep(player1);
        harness.handleListChoice(player1, DESTROY_MODE);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(enchantment, aura);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(enchantment, aura);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(enchantment.getCard(), aura.getCard());
    }

    private Permanent castOn(Permanent target) {
        harness.setHand(player1, List.of(new QuietDisrepair()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        return findPermanent(player1, "Quiet Disrepair");
    }
}
