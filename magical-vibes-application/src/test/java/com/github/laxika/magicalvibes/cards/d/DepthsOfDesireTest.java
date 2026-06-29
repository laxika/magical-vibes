package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DepthsOfDesireTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has bounce effect and treasure creation effect on SPELL slot")
    void hasCorrectSpellEffects() {
        DepthsOfDesire card = new DepthsOfDesire();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL))
                .hasSize(2)
                .anySatisfy(e -> assertThat(e).isInstanceOf(ReturnTargetPermanentToHandEffect.class))
                .anySatisfy(e -> assertThat(e).isInstanceOf(CreateTokenEffect.class));
    }

    // ===== Bounce + Treasure =====

    @Test
    @DisplayName("Bounces target creature and creates a Treasure token")
    void bouncesCreatureAndCreatesTreasure() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new DepthsOfDesire()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Creature bounced
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Treasure created
        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(treasure).isNotNull();
        assertThat(treasure.getCard().isToken()).isTrue();
        assertThat(treasure.getCard().getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(treasure.getCard().getSubtypes()).contains(CardSubtype.TREASURE);
    }

    @Test
    @DisplayName("Treasure token has sacrifice-for-mana activated ability")
    void treasureTokenHasManaAbility() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new DepthsOfDesire()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(treasure.getCard().getActivatedAbilities()).hasSize(1);
        assertThat(treasure.getCard().getActivatedAbilities().getFirst().isRequiresTap()).isTrue();
    }

    // ===== Targeting restrictions =====

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // valid target so spell is playable
        harness.addToBattlefield(player2, new Island());
        UUID targetId = harness.getPermanentId(player2, "Island");
        harness.setHand(player1, List.of(new DepthsOfDesire()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Can bounce own creature =====

    @Test
    @DisplayName("Can bounce own creature")
    void canBounceOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new DepthsOfDesire()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Still creates treasure even when bouncing own creature
        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(treasure).isNotNull();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target creature is removed before resolution — no Treasure created")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new DepthsOfDesire()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Depths of Desire"));
    }

    // ===== Helpers =====

}
