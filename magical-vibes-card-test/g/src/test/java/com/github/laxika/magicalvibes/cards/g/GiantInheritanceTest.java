package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.Cursebreak;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GiantInheritance.class, GrizzlyBears.class, Cursebreak.class})
class GiantInheritanceTest extends BaseCardTest {

    @Test
    void boostsEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GiantInheritance());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);
    }

    @Test
    void attackingEnchantedCreatureCreatesMonsterRoleOnChosenAttacker() {
        Permanent enchantedCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GiantInheritance());
        aura.setAttachedTo(enchantedCreature.getId());

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(enchantedCreature),
                gd.playerBattlefields.get(player1.getId()).indexOf(otherAttacker)));
        resolveAllTriggers();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(otherAttacker.getId());
        harness.handlePermanentChosen(player1, otherAttacker.getId());
        resolveAllTriggers();

        Permanent role = findPermanent(player1, "Monster");
        assertThat(role.getAttachedTo()).isEqualTo(otherAttacker.getId());
        assertThat(gqs.getEffectivePower(gd, otherAttacker)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, otherAttacker, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void attackTriggerCanBeDeclinedAndCreatesUnattachedRole() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GiantInheritance());
        aura.setAttachedTo(creature.getId());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        Permanent role = findPermanent(player1, "Monster");
        assertThat(role.getAttachedTo()).isNull();
    }

    @Test
    void returnsToOwnersHandWhenPutIntoGraveyardFromBattlefield() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GiantInheritance());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new Cursebreak()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, aura.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInHand(player1, "Giant Inheritance");
        harness.assertNotOnBattlefield(player1, "Giant Inheritance");
    }
}
