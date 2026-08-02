package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VigilanceTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Vigilance attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new Vigilance()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Vigilance")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("Enchanted creature has vigilance")
    void enchantedCreatureHasVigilance() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent auraPerm = new Permanent(new Vigilance());
        auraPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses vigilance when Vigilance is removed")
    void vigilanceStopsWhenRemoved() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent auraPerm = new Permanent(new Vigilance());
        auraPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(auraPerm);

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Vigilance")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Vigilance()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Vigilance does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        Permanent otherBears = new Permanent(new GrizzlyBears());
        otherBears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(otherBears);

        Permanent auraPerm = new Permanent(new Vigilance());
        auraPerm.setAttachedTo(bearsPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.VIGILANCE)).isFalse();
    }
}
