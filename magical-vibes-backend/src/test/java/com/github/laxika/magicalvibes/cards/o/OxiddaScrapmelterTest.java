package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OxiddaScrapmelterTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Oxidda Scrapmelter has correct card properties")
    void hasCorrectProperties() {
        OxiddaScrapmelter card = new OxiddaScrapmelter();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(DestroyTargetPermanentEffect.class);
        DestroyTargetPermanentEffect effect = (DestroyTargetPermanentEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.cannotBeRegenerated()).isFalse();
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Oxidda Scrapmelter puts it on the stack with target")
    void castingPutsItOnStackWithTarget() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new OxiddaScrapmelter()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Leonin Scimitar");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Oxidda Scrapmelter");
        assertThat(entry.getTargetId()).isEqualTo(targetId);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Resolving Oxidda Scrapmelter enters battlefield and triggers ETB destroy")
    void resolvingEntersBattlefieldAndTriggersEtb() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new OxiddaScrapmelter()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Leonin Scimitar");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        // Resolve creature spell → enters battlefield, ETB triggers
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Oxidda Scrapmelter"));

        // ETB triggered ability should be on stack
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Oxidda Scrapmelter");
        assertThat(trigger.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("ETB resolves and destroys target artifact")
    void etbDestroysTargetArtifact() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new OxiddaScrapmelter()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Leonin Scimitar");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB triggered ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Leonin Scimitar"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Leonin Scimitar"));
    }

    @Test
    @DisplayName("ETB destroys artifact creature")
    void etbDestroysArtifactCreature() {
        harness.addToBattlefield(player2, new BottleGnomes());
        harness.setHand(player1, List.of(new OxiddaScrapmelter()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Bottle Gnomes");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        // Resolve creature spell
        harness.passBothPriorities();
        // Resolve ETB triggered ability
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Bottle Gnomes"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Bottle Gnomes"));
    }

    // ===== Target restrictions =====

    @Test
    @DisplayName("Cannot target a non-artifact creature")
    void cannotTargetNonArtifactCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OxiddaScrapmelter()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact");
    }

    // ===== Indestructible =====

    @Test
    @DisplayName("Indestructible artifact survives ETB")
    void indestructibleArtifactSurvives() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new OxiddaScrapmelter()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Leonin Scimitar");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        // Resolve creature spell → ETB on stack
        harness.passBothPriorities();

        // Grant indestructible to the target before ETB resolves
        Permanent target = harness.getGameData().playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Leonin Scimitar"))
                .findFirst().orElseThrow();
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        // Resolve ETB — should not destroy indestructible artifact
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Leonin Scimitar"));
        assertThat(gd.gameLog).anyMatch(log -> log.contains("indestructible"));
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("ETB fizzles if target artifact is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new OxiddaScrapmelter()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Leonin Scimitar");
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, targetId, null);

        // Resolve creature spell → ETB on stack
        harness.passBothPriorities();

        // Remove target before ETB resolves
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        // Resolve ETB → fizzles
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== No target scenarios =====

    @Test
    @DisplayName("Can cast without a target when no artifacts on battlefield")
    void canCastWithoutTargetWhenNoArtifacts() {
        harness.setHand(player1, List.of(new OxiddaScrapmelter()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Oxidda Scrapmelter");
    }

    @Test
    @DisplayName("ETB does not trigger when cast without a target")
    void etbDoesNotTriggerWithoutTarget() {
        harness.setHand(player1, List.of(new OxiddaScrapmelter()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);

        // Resolve creature spell
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Oxidda Scrapmelter"));
        assertThat(gd.stack).isEmpty();
    }
}
