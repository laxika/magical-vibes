package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromSubtypesEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EliteInquisitorTest extends BaseCardTest {

    private static Card createCreatureWithSubtype(String name, int power, int toughness,
                                                  CardColor color, CardSubtype subtype, Keyword... keywords) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(List.of(subtype));
        if (keywords.length > 0) {
            card.setKeywords(Set.of(keywords));
        }
        return card;
    }

    private static Card createCreature(String name, int power, int toughness, CardColor color, Keyword... keywords) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        if (keywords.length > 0) {
            card.setKeywords(Set.of(keywords));
        }
        return card;
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Elite Inquisitor has protection from Vampires, Werewolves, and Zombies")
    void hasProtectionFromVampiresWerewolvesAndZombies() {
        EliteInquisitor card = new EliteInquisitor();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(ProtectionFromSubtypesEffect.class);

        ProtectionFromSubtypesEffect protection = (ProtectionFromSubtypesEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(protection.subtypes()).containsExactlyInAnyOrder(
                CardSubtype.VAMPIRE, CardSubtype.WEREWOLF, CardSubtype.ZOMBIE);
    }

    // ===== Protection - blocking =====

    @Test
    @DisplayName("Vampire creature cannot block Elite Inquisitor")
    void vampireCannotBlock() {
        Permanent attacker = new Permanent(new EliteInquisitor());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(createCreatureWithSubtype("Vampire Interloper", 2, 1, CardColor.BLACK, CardSubtype.VAMPIRE));
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
    @DisplayName("Werewolf creature cannot block Elite Inquisitor")
    void werewolfCannotBlock() {
        Permanent attacker = new Permanent(new EliteInquisitor());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(createCreatureWithSubtype("Reckless Waif", 3, 2, CardColor.RED, CardSubtype.WEREWOLF));
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
    @DisplayName("Zombie creature cannot block Elite Inquisitor")
    void zombieCannotBlock() {
        Permanent attacker = new Permanent(new EliteInquisitor());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(createCreatureWithSubtype("Walking Corpse", 2, 2, CardColor.BLACK, CardSubtype.ZOMBIE));
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
    @DisplayName("Non-protected creature can block Elite Inquisitor")
    void regularCreatureCanBlock() {
        Permanent attacker = new Permanent(new EliteInquisitor());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(createCreature("Grizzly Bears", 2, 2, CardColor.GREEN));
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
    @DisplayName("Elite Inquisitor takes no combat damage from Vampire creature")
    void takesNoDamageFromVampire() {
        Permanent attacker = new Permanent(createCreatureWithSubtype("Sengir Vampire", 4, 4, CardColor.BLACK, CardSubtype.VAMPIRE));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new EliteInquisitor());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Elite Inquisitor has first strike: deals 2 damage first (Vampire survives at 4/2)
        // Vampire's 4 damage to Elite Inquisitor is prevented (protection from Vampires)
        // Both creatures survive
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sengir Vampire"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Elite Inquisitor"));
    }

    @Test
    @DisplayName("Elite Inquisitor takes normal combat damage from non-protected creature")
    void takesNormalDamageFromRegularCreature() {
        Permanent attacker = new Permanent(createCreature("Hill Giant", 3, 3, CardColor.RED));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(new EliteInquisitor());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Elite Inquisitor deals 2 first strike damage (Hill Giant survives at 3/1)
        // Hill Giant deals 3 regular damage (kills 2/2 Elite Inquisitor)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Hill Giant"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Elite Inquisitor"));
    }
}
