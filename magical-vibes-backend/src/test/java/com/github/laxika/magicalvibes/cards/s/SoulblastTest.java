package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAllCreaturesYouControlCost;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoulblastTest {

    private GameTestHarness harness;
    private GameData gd;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        gd = harness.getGameData();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    @Test
    @DisplayName("Soulblast has correct card properties")
    void hasCorrectProperties() {
        Soulblast card = new Soulblast();

        assertThat(card.getName()).isEqualTo("Soulblast");
        assertThat(card.getType()).isEqualTo(CardType.INSTANT);
        assertThat(card.getManaCost()).isEqualTo("{3}{R}{R}{R}");
        assertThat(card.getColor()).isEqualTo(CardColor.RED);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(SacrificeAllCreaturesYouControlCost.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(DealXDamageToAnyTargetEffect.class);
    }

    @Test
    @DisplayName("Casting Soulblast sacrifices all your creatures and stores total power in X")
    void castingSacrificesAllYourCreaturesAndStoresTotalPower() {
        harness.setHand(player1, List.of(new Soulblast()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Soulblast");
        assertThat(entry.getTargetPermanentId()).isEqualTo(player2.getId());
        assertThat(entry.getXValue()).isEqualTo(3);
        assertThat(entry.getEffectsToResolve()).hasSize(1);
        assertThat(entry.getEffectsToResolve().getFirst()).isInstanceOf(DealXDamageToAnyTargetEffect.class);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears") || p.getCard().getName().equals("Raging Goblin"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Raging Goblin"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Soulblast deals damage equal to sacrificed total power")
    void dealsDamageEqualToSacrificedTotalPower() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Soulblast()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new RagingGoblin());

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Soulblast can be cast with no creatures and deals 0 damage")
    void canCastWithNoCreatures() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Soulblast()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Soulblast ignores opponent creature power when calculating damage")
    void ignoresOpponentCreaturePower() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Soulblast()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("If mana payment fails, Soulblast does not sacrifice creatures")
    void manaPaymentFailureDoesNotSacrificeCreatures() {
        harness.setHand(player1, List.of(new Soulblast()));
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }
}
