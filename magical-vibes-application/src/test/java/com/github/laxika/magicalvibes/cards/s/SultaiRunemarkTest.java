package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SultaiRunemark.class, EliteVanguard.class, GrizzlyBears.class, WindDrake.class})
class SultaiRunemarkTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +2/+2")
    void enchantedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new EliteVanguard());

        castAuraOn(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Enchanted creature has deathtouch while the Aura's controller controls a green permanent")
    void gainsDeathtouchWithGreenPermanent() {
        Permanent creature = addCreatureReady(player1, new EliteVanguard());
        addCreatureReady(player1, new GrizzlyBears());

        castAuraOn(creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature has deathtouch while the Aura's controller controls a blue permanent")
    void gainsDeathtouchWithBluePermanent() {
        Permanent creature = addCreatureReady(player1, new EliteVanguard());
        addCreatureReady(player1, new WindDrake());

        castAuraOn(creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Deathtouch is lost when the qualifying permanent leaves")
    void losesDeathtouchWhenQualifyingPermanentLeaves() {
        Permanent creature = addCreatureReady(player1, new EliteVanguard());
        Permanent greenCreature = addCreatureReady(player1, new GrizzlyBears());

        castAuraOn(creature);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(greenCreature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("An opponent's green permanent does not qualify")
    void opponentGreenPermanentDoesNotQualify() {
        Permanent creature = addCreatureReady(player1, new EliteVanguard());
        addCreatureReady(player2, new GrizzlyBears());

        castAuraOn(creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("The enchanted green creature itself qualifies for deathtouch")
    void enchantedCreatureCanBeTheQualifyingPermanent() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        castAuraOn(creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Deathtouch appears when a qualifying permanent enters after the Aura resolves")
    void gainsDeathtouchWhenQualifyingPermanentEnters() {
        Permanent creature = addCreatureReady(player1, new EliteVanguard());
        castAuraOn(creature);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();

        addCreatureReady(player1, new WindDrake());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("An opponent's enchanted creature gains deathtouch from the Aura controller's green permanent")
    void opponentCreatureUsesAuraControllersPermanents() {
        Permanent creature = addCreatureReady(player2, new EliteVanguard());
        addCreatureReady(player1, new GrizzlyBears());

        castAuraOn(creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's enchanted green creature does not satisfy the Aura controller's condition")
    void opponentEnchantedGreenCreatureDoesNotQualify() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        castAuraOn(creature);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    private void castAuraOn(Permanent target) {
        harness.setHand(player1, List.of(new SultaiRunemark()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
