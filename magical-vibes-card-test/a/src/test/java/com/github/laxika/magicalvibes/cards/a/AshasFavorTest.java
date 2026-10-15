package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AshasFavor.class, GrizzlyBears.class, FountainOfYouth.class})
class AshasFavorTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Asha's Favor attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bearsPerm.setSummoningSick(false);

        harness.setHand(player1, List.of(new AshasFavor()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Asha's Favor")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("Enchanted creature has flying, first strike, and vigilance")
    void enchantedCreatureHasKeywords() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bearsPerm.setSummoningSick(false);

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new AshasFavor());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses all keywords when Asha's Favor is removed")
    void keywordsStopWhenRemoved() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bearsPerm.setSummoningSick(false);

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new AshasFavor());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(auraPerm);

        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, bearsPerm, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Asha's Favor does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bearsPerm.setSummoningSick(false);

        Permanent otherBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        otherBears.setSummoningSick(false);

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new AshasFavor());
        auraPerm.setAttachedTo(bearsPerm.getId());

        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherBears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Asha's Favor can enchant an opponent's creature and grants it all three keywords")
    void canEnchantOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AshasFavor()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Asha's Favor");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Asha's Favor goes to the graveyard if its target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AshasFavor()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof AshasFavor);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof AshasFavor);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Asha's Favor")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new AshasFavor()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
