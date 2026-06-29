package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToControlledLandCountEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MolimoMaroSorcererTest extends BaseCardTest {


    @Test
    @DisplayName("Molimo, Maro-Sorcerer has correct card properties")
    void hasCorrectProperties() {
        MolimoMaroSorcerer card = new MolimoMaroSorcerer();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(PowerToughnessEqualToControlledLandCountEffect.class);
    }

    @Test
    @DisplayName("Casting Molimo puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new MolimoMaroSorcerer()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Molimo, Maro-Sorcerer");
    }

    @Test
    @DisplayName("Molimo dies to state-based actions with no lands")
    void diesWithNoLands() {
        harness.setHand(player1, List.of(new MolimoMaroSorcerer()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Molimo, Maro-Sorcerer"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Molimo, Maro-Sorcerer"));
    }

    @Test
    @DisplayName("Molimo survives when you control a land")
    void survivesWithLand() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new MolimoMaroSorcerer()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Molimo, Maro-Sorcerer"));
    }

    @Test
    @DisplayName("Molimo power and toughness equal lands you control")
    void ptEqualsControlledLands() {
        Permanent molimo = addMolimoReady(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectivePower(gd, molimo)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, molimo)).isEqualTo(3);
    }

    @Test
    @DisplayName("Molimo counts only your lands, not opponent lands")
    void countsOnlyControllersLands() {
        Permanent molimo = addMolimoReady(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Plains());

        assertThat(gqs.getEffectivePower(gd, molimo)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, molimo)).isEqualTo(1);
    }

    @Test
    @DisplayName("Molimo power and toughness update when lands change")
    void ptUpdatesWhenLandsChange() {
        Permanent molimo = addMolimoReady(player1);
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, molimo)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, molimo)).isEqualTo(1);

        harness.addToBattlefield(player1, new Plains());
        assertThat(gqs.getEffectivePower(gd, molimo)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, molimo)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().hasType(CardType.LAND));
        assertThat(gqs.getEffectivePower(gd, molimo)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, molimo)).isEqualTo(0);
    }

    @Test
    @DisplayName("Molimo characteristic-defining P/T stacks with other static bonuses")
    void ptStacksWithOtherStaticBonuses() {
        Permanent molimo = addMolimoReady(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new GloriousAnthem());

        assertThat(gqs.getEffectivePower(gd, molimo)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, molimo)).isEqualTo(3);
    }

    @Test
    @DisplayName("Molimo has trample on the battlefield")
    void hasTrampleOnBattlefield() {
        Permanent molimo = addMolimoReady(player1);
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.hasKeyword(gd, molimo, Keyword.TRAMPLE)).isTrue();
    }

    private Permanent addMolimoReady(Player player) {
        MolimoMaroSorcerer card = new MolimoMaroSorcerer();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
