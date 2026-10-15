package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.k.KnightOfCliffhaven;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({GuardDuty.class, KnightOfCliffhaven.class, Mountain.class})
class GuardDutyTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Guard Duty attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent bears = addCreatureReady(player1, new KnightOfCliffhaven());
        harness.setHand(player1, List.of(new GuardDuty()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof GuardDuty
                        && permanent.isAttached()
                        && permanent.getAttachedTo().equals(bears.getId()));
    }

    @Test
    @DisplayName("Enchanted creature has defender")
    void enchantedCreatureHasDefender() {
        Permanent bears = addCreatureReady(player1, new KnightOfCliffhaven());
        attachGuardDuty(bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("Guard Duty does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent enchanted = addCreatureReady(player1, new KnightOfCliffhaven());
        Permanent other = addCreatureReady(player1, new KnightOfCliffhaven());
        attachGuardDuty(enchanted);

        assertThat(gqs.hasKeyword(gd, other, Keyword.DEFENDER)).isFalse();
    }

    @Test
    @DisplayName("Creature loses defender when Guard Duty leaves the battlefield")
    void creatureLosesDefenderWhenAuraLeaves() {
        Permanent bears = addCreatureReady(player1, new KnightOfCliffhaven());
        Permanent aura = attachGuardDuty(bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEFENDER)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEFENDER)).isFalse();
    }

    @Test
    @DisplayName("Guard Duty cannot enchant a land")
    void cannotEnchantALand() {
        harness.addToBattlefield(player2, new KnightOfCliffhaven());
        harness.addToBattlefield(player1, new Mountain());
        harness.setHand(player1, List.of(new GuardDuty()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        Permanent mountain = findPermanent(player1, "Mountain");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Guard Duty can enchant an opponent's creature")
    void enchantsOpponentsCreature() {
        Permanent creature = addCreatureReady(player2, new KnightOfCliffhaven());
        harness.setHand(player1, List.of(new GuardDuty()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Guard Duty").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEFENDER)).isTrue();
        assertThat(als.canAttack(gd, creature, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("An enchanted creature cannot attack, but other creatures still can")
    void defenderPreventsAttackingOnlyForEnchantedCreature() {
        Permanent enchanted = addCreatureReady(player1, new KnightOfCliffhaven());
        Permanent other = addCreatureReady(player1, new KnightOfCliffhaven());
        harness.setHand(player1, List.of(new GuardDuty()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        assertThat(als.canAttack(gd, enchanted, player1.getId())).isFalse();
        assertThat(als.canAttack(gd, other, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("Defender granted by Guard Duty does not prevent blocking")
    void enchantedCreatureCanStillBlock() {
        Permanent creature = addCreatureReady(player2, new KnightOfCliffhaven());
        harness.setHand(player1, List.of(new GuardDuty()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEFENDER)).isTrue();
        assertThat(bls.canBlock(gd, creature)).isTrue();
    }

    @Test
    @DisplayName("Guard Duty goes to the graveyard if its target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent creature = addCreatureReady(player2, new KnightOfCliffhaven());
        harness.setHand(player1, List.of(new GuardDuty()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, creature.getId());

        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerGraveyards.get(player2.getId()).add(creature.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof GuardDuty);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .singleElement().isInstanceOf(GuardDuty.class);
    }

    private Permanent attachGuardDuty(Permanent creature) {
        Permanent aura = new Permanent(new GuardDuty());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }
}
