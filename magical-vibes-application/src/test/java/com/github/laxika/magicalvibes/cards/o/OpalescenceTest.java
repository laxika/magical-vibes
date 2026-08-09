package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Insight;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpalescenceTest extends BaseCardTest {

    @Test
    @DisplayName("Other non-Aura enchantments on both battlefields become creatures with mana-value P/T")
    void animatesOtherNonAuraEnchantmentsOnBothBattlefields() {
        harness.addToBattlefield(player1, new Opalescence());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player2, new Insight());

        Permanent anthem = findPermanent(player1, "Glorious Anthem");
        Permanent insight = findPermanent(player2, "Insight");

        assertThat(gqs.isCreature(gd, anthem)).isTrue();
        assertThat(gqs.getEffectivePower(gd, anthem)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, anthem)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, insight)).isTrue();
        assertThat(gqs.getEffectivePower(gd, insight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, insight)).isEqualTo(3);
    }

    @Test
    @DisplayName("Opalescence does not animate itself")
    void doesNotAnimateItself() {
        harness.addToBattlefield(player1, new Opalescence());

        assertThat(gqs.isCreature(gd, findPermanent(player1, "Opalescence"))).isFalse();
    }

    @Test
    @DisplayName("Auras are not animated")
    void doesNotAnimateAuras() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Pacifism());
        harness.addToBattlefield(player1, new Opalescence());
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent pacifism = findPermanent(player1, "Pacifism");
        pacifism.setAttachedTo(bears.getId());

        assertThat(gqs.isCreature(gd, pacifism)).isFalse();
    }

    @Test
    @DisplayName("Animated enchantments retain their enchantment type and abilities")
    void retainsOtherTypesAndAbilities() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player1, new Opalescence());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent anthem = findPermanent(player1, "Glorious Anthem");
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.isEnchantment(gd, anthem)).isTrue();
        assertThat(gqs.isCreature(gd, anthem)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }
}
