package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToControlledCreatureCountEffect;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScionOfTheWildTest {

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
    @DisplayName("Scion of the Wild has correct card properties")
    void hasCorrectProperties() {
        ScionOfTheWild card = new ScionOfTheWild();

        assertThat(card.getName()).isEqualTo("Scion of the Wild");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{1}{G}{G}");
        assertThat(card.getColor()).isEqualTo(CardColor.GREEN);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.AVATAR);
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(PowerToughnessEqualToControlledCreatureCountEffect.class);
    }

    @Test
    @DisplayName("Casting Scion of the Wild puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new ScionOfTheWild()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Scion of the Wild");
    }

    @Test
    @DisplayName("Scion of the Wild is 1/1 when it is your only creature")
    void isOneOneWhenOnlyCreature() {
        Permanent scion = addScionReady(player1);

        assertThat(gqs.getEffectivePower(gd, scion)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, scion)).isEqualTo(1);
    }

    @Test
    @DisplayName("Scion of the Wild power and toughness equal creatures you control")
    void ptEqualsControlledCreatures() {
        Permanent scion = addScionReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, scion)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, scion)).isEqualTo(3);
    }

    @Test
    @DisplayName("Scion of the Wild counts only your creatures, not opponent creatures")
    void countsOnlyControllersCreatures() {
        Permanent scion = addScionReady(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, scion)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, scion)).isEqualTo(1);
    }

    @Test
    @DisplayName("Scion of the Wild power and toughness update as creatures enter and leave")
    void ptUpdatesAsCreaturesChange() {
        Permanent scion = addScionReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, scion)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, scion)).isEqualTo(2);

        harness.addToBattlefield(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, scion)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, scion)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gqs.getEffectivePower(gd, scion)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, scion)).isEqualTo(1);
    }

    @Test
    @DisplayName("Scion of the Wild characteristic-defining P/T stacks with static bonuses")
    void ptStacksWithStaticBonuses() {
        Permanent scion = addScionReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GloriousAnthem());

        assertThat(gqs.getEffectivePower(gd, scion)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, scion)).isEqualTo(3);
    }

    private Permanent addScionReady(Player player) {
        ScionOfTheWild card = new ScionOfTheWild();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
