package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GreatSableStagTest extends BaseCardTest {

    private static Card createCreature(String name, int power, int toughness, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private static Card createTargetedInstant(String name, CardColor color, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(1));
        return card;
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Great Sable Stag has cant-be-countered and protection from blue and black")
    void hasCorrectProperties() {
        GreatSableStag card = new GreatSableStag();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasAtLeastOneElementOfType(CantBeCounteredEffect.class);
        assertThat(card.getEffects(EffectSlot.STATIC)).hasAtLeastOneElementOfType(ProtectionFromColorsEffect.class);

        ProtectionFromColorsEffect protection = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof ProtectionFromColorsEffect)
                .map(e -> (ProtectionFromColorsEffect) e)
                .findFirst().orElseThrow();
        assertThat(protection.colors()).containsExactlyInAnyOrder(CardColor.BLUE, CardColor.BLACK);
    }

    // ===== Can't be countered =====

    @Test
    @DisplayName("Great Sable Stag cannot be countered by Cancel")
    void cannotBeCounteredByCancel() {
        GreatSableStag stag = new GreatSableStag();
        harness.setHand(player1, List.of(stag));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, stag.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Great Sable Stag"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Great Sable Stag"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Cancel"));
    }

    // ===== Protection - targeting =====

    @Test
    @DisplayName("Cannot be targeted by blue instant")
    void cannotBeTargetedByBlueInstant() {
        Permanent stag = addStagReady(player1);

        // Add valid target so spell is playable
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.setHand(player2, List.of(createTargetedInstant("Blue Zap", CardColor.BLUE, "{U}")));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, stag.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from blue");
    }

    @Test
    @DisplayName("Cannot be targeted by black instant")
    void cannotBeTargetedByBlackInstant() {
        Permanent stag = addStagReady(player1);

        // Add valid target so spell is playable
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.setHand(player2, List.of(createTargetedInstant("Black Zap", CardColor.BLACK, "{B}")));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, stag.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("Can be targeted by red instant")
    void canBeTargetedByRedInstant() {
        Permanent stag = addStagReady(player1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        gs.playCard(gd, player1, 0, 0, stag.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shock");
    }

    @Test
    @DisplayName("Can be targeted by green instant")
    void canBeTargetedByGreenInstant() {
        Permanent stag = addStagReady(player1);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        gs.playCard(gd, player1, 0, 0, stag.getId(), null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Giant Growth");
    }

    // ===== Protection - blocking =====

    @Test
    @DisplayName("Blue creature cannot block Great Sable Stag")
    void blueCreatureCannotBlock() {
        Permanent attacker = new Permanent(new GreatSableStag());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(createCreature("Blue Flyer", 2, 2, CardColor.BLUE));
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Black creature cannot block Great Sable Stag")
    void blackCreatureCannotBlock() {
        Permanent attacker = new Permanent(new GreatSableStag());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(createCreature("Black Shade", 2, 2, CardColor.BLACK));
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Red creature can block Great Sable Stag")
    void redCreatureCanBlock() {
        Permanent attacker = new Permanent(new GreatSableStag());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(createCreature("Red Goblin", 2, 2, CardColor.RED));
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    // ===== Protection - combat damage =====

    @Test
    @DisplayName("Great Sable Stag takes no combat damage from blue creature")
    void takesNoDamageFromBlueCreature() {
        Permanent attacker = new Permanent(createCreature("Blue Giant", 4, 4, CardColor.BLUE));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new GreatSableStag());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Stag deals 3 to Blue Giant (4/4 survives), Blue Giant's 4 damage is prevented by protection
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Great Sable Stag"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Blue Giant"));
    }

    @Test
    @DisplayName("Great Sable Stag takes normal combat damage from red creature")
    void takesNormalDamageFromRedCreature() {
        Permanent attacker = new Permanent(createCreature("Red Giant", 4, 4, CardColor.RED));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new GreatSableStag());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Red Giant deals 4 to Stag (4 >= 3 toughness, dies), Stag deals 3 to Red Giant (3 < 4, survives)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Great Sable Stag"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Great Sable Stag"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Red Giant"));
    }

    // ===== Helper methods =====

    private Permanent addStagReady(Player player) {
        GreatSableStag card = new GreatSableStag();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
