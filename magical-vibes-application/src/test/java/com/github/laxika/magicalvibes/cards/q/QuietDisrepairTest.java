package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuietDisrepair.class, AccordersShield.class, GloriousAnthem.class, GrizzlyBears.class})
class QuietDisrepairTest extends BaseCardTest {

    private static final String DESTROY_MODE = "Destroy enchanted permanent.";
    private static final String GAIN_LIFE_MODE = "You gain 2 life.";

    @Test
    void cannotEnchantCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new QuietDisrepair()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or enchantment");
    }

    @Test
    void destroyModeDestroysEnchantedArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new AccordersShield());
        Permanent aura = castOn(artifact);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, DESTROY_MODE);

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(artifact.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
    }

    @Test
    void gainLifeModeLeavesEnchantedEnchantmentOnTheBattlefield() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        Permanent aura = castOn(enchantment);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, GAIN_LIFE_MODE);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(enchantment);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
    }

    private Permanent castOn(Permanent target) {
        harness.setHand(player1, List.of(new QuietDisrepair()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof QuietDisrepair)
                .findFirst()
                .orElseThrow();
    }
}
