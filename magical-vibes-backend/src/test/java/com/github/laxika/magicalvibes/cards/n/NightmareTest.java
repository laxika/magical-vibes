package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToControlledSubtypeCountEffect;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NightmareTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;
    private GameQueryService gqs;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        gqs = harness.getGameQueryService();
        harness.skipMulligan();
        harness.clearMessages();
    }

    @Test
    @DisplayName("Nightmare has correct card properties")
    void hasCorrectProperties() {
        Nightmare card = new Nightmare();

        assertThat(card.getName()).isEqualTo("Nightmare");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{5}{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.getKeywords()).containsExactly(Keyword.FLYING);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.NIGHTMARE, CardSubtype.HORSE);
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(PowerToughnessEqualToControlledSubtypeCountEffect.class);
        PowerToughnessEqualToControlledSubtypeCountEffect effect =
                (PowerToughnessEqualToControlledSubtypeCountEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.subtype()).isEqualTo(CardSubtype.SWAMP);
    }

    @Test
    @DisplayName("Casting Nightmare puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new Nightmare()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Nightmare");
    }

    @Test
    @DisplayName("Nightmare dies to state-based actions with no Swamps")
    void diesWithNoSwamps() {
        harness.setHand(player1, List.of(new Nightmare()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Nightmare"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Nightmare"));
    }

    @Test
    @DisplayName("Nightmare survives when you control a Swamp")
    void survivesWithSwamp() {
        harness.addToBattlefield(player1, new Swamp());
        harness.setHand(player1, List.of(new Nightmare()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Nightmare"));
    }

    @Test
    @DisplayName("Nightmare power and toughness equal number of Swamps you control")
    void ptEqualsControlledSwamps() {
        Permanent nightmare = addNightmareReady(player1);
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectivePower(gd, nightmare)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nightmare)).isEqualTo(2);
    }

    @Test
    @DisplayName("Nightmare counts only your Swamps, not opponent Swamps")
    void countsOnlyControllersSwamps() {
        Permanent nightmare = addNightmareReady(player1);
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());

        assertThat(gqs.getEffectivePower(gd, nightmare)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nightmare)).isEqualTo(1);
    }

    @Test
    @DisplayName("Nightmare power and toughness update when Swamps change")
    void ptUpdatesWhenSwampsChange() {
        Permanent nightmare = addNightmareReady(player1);
        harness.addToBattlefield(player1, new Swamp());

        assertThat(gqs.getEffectivePower(gd, nightmare)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nightmare)).isEqualTo(1);

        harness.addToBattlefield(player1, new Swamp());
        assertThat(gqs.getEffectivePower(gd, nightmare)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nightmare)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Swamp"));
        assertThat(gqs.getEffectivePower(gd, nightmare)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, nightmare)).isEqualTo(0);
    }

    @Test
    @DisplayName("Nightmare characteristic-defining P/T stacks with other static bonuses")
    void ptStacksWithOtherStaticBonuses() {
        Permanent nightmare = addNightmareReady(player1);
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new GloriousAnthem());

        assertThat(gqs.getEffectivePower(gd, nightmare)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, nightmare)).isEqualTo(3);
    }

    @Test
    @DisplayName("Nightmare has flying on the battlefield")
    void hasFlyingOnBattlefield() {
        Permanent nightmare = addNightmareReady(player1);
        harness.addToBattlefield(player1, new Swamp());

        assertThat(gqs.hasKeyword(gd, nightmare, Keyword.FLYING)).isTrue();
    }

    private Permanent addNightmareReady(Player player) {
        Nightmare card = new Nightmare();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
