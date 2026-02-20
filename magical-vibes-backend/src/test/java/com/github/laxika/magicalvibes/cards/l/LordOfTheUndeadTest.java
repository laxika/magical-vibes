package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.Gravedigger;
import com.github.laxika.magicalvibes.cards.g.GravebornMuse;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesBySubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardOfSubtypeFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LordOfTheUndeadTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameQueryService gqs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gqs = harness.getGameQueryService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Lord of the Undead has correct card properties")
    void hasCorrectProperties() {
        LordOfTheUndead card = new LordOfTheUndead();

        assertThat(card.getName()).isEqualTo("Lord of the Undead");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{1}{B}{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(2);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.ZOMBIE);
    }

    @Test
    @DisplayName("Lord of the Undead has static boost effect for Zombies")
    void hasStaticBoostEffect() {
        LordOfTheUndead card = new LordOfTheUndead();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(BoostCreaturesBySubtypeEffect.class);

        BoostCreaturesBySubtypeEffect effect = (BoostCreaturesBySubtypeEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.affectedSubtypes()).containsExactly(CardSubtype.ZOMBIE);
        assertThat(effect.powerBoost()).isEqualTo(1);
        assertThat(effect.toughnessBoost()).isEqualTo(1);
        assertThat(effect.grantedKeywords()).isEmpty();
    }

    @Test
    @DisplayName("Lord of the Undead has activated ability to return Zombie from graveyard")
    void hasActivatedAbility() {
        LordOfTheUndead card = new LordOfTheUndead();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isEqualTo("{1}{B}");
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(ReturnCardOfSubtypeFromGraveyardToHandEffect.class);

        ReturnCardOfSubtypeFromGraveyardToHandEffect effect =
                (ReturnCardOfSubtypeFromGraveyardToHandEffect) ability.getEffects().getFirst();
        assertThat(effect.subtype()).isEqualTo(CardSubtype.ZOMBIE);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new LordOfTheUndead()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Lord of the Undead");
    }

    @Test
    @DisplayName("Resolving puts it on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new LordOfTheUndead()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Lord of the Undead"));
    }

    @Test
    @DisplayName("Enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.setHand(player1, List.of(new LordOfTheUndead()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Lord of the Undead"))
                .findFirst().orElseThrow();
        assertThat(perm.isSummoningSick()).isTrue();
    }

    // ===== Static effect: buffs other Zombies =====

    @Test
    @DisplayName("Other Zombie creatures get +1/+1")
    void buffsOtherZombies() {
        // Gravedigger is a Zombie (2/2)
        harness.addToBattlefield(player1, new Gravedigger());
        harness.addToBattlefield(player1, new LordOfTheUndead());

        Permanent gravedigger = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Gravedigger"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, gravedigger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, gravedigger)).isEqualTo(3);
    }

    @Test
    @DisplayName("Lord of the Undead does not buff itself")
    void doesNotBuffItself() {
        harness.addToBattlefield(player1, new LordOfTheUndead());

        Permanent lord = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Lord of the Undead"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, lord)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lord)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff non-Zombie creatures")
    void doesNotBuffNonZombies() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LordOfTheUndead());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Buffs opponent's Zombie creatures too")
    void buffsOpponentZombies() {
        harness.addToBattlefield(player1, new LordOfTheUndead());
        harness.addToBattlefield(player2, new Gravedigger());

        Permanent opponentZombie = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Gravedigger"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, opponentZombie)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentZombie)).isEqualTo(3);
    }

    // ===== Multiple sources =====

    @Test
    @DisplayName("Two Lords of the Undead buff each other")
    void twoLordsBuffEachOther() {
        harness.addToBattlefield(player1, new LordOfTheUndead());
        harness.addToBattlefield(player1, new LordOfTheUndead());

        List<Permanent> lords = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Lord of the Undead"))
                .toList();

        assertThat(lords).hasSize(2);
        for (Permanent lord : lords) {
            assertThat(gqs.getEffectivePower(gd, lord)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, lord)).isEqualTo(3);
        }
    }

    @Test
    @DisplayName("Two Lords give +2/+2 to other Zombies")
    void twoLordsStackBonuses() {
        harness.addToBattlefield(player1, new LordOfTheUndead());
        harness.addToBattlefield(player1, new LordOfTheUndead());
        harness.addToBattlefield(player1, new Gravedigger());

        Permanent gravedigger = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Gravedigger"))
                .findFirst().orElseThrow();

        // 2/2 base + 2/2 from two lords = 4/4
        assertThat(gqs.getEffectivePower(gd, gravedigger)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gravedigger)).isEqualTo(4);
    }

    // ===== Bonus gone when source leaves =====

    @Test
    @DisplayName("Bonus is removed when Lord of the Undead leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new LordOfTheUndead());
        harness.addToBattlefield(player1, new Gravedigger());

        Permanent gravedigger = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Gravedigger"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, gravedigger)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Lord of the Undead"));

        assertThat(gqs.getEffectivePower(gd, gravedigger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gravedigger)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bonus applies when Lord resolves onto battlefield")
    void bonusAppliesOnResolve() {
        harness.addToBattlefield(player1, new Gravedigger());
        harness.setHand(player1, List.of(new LordOfTheUndead()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        Permanent gravedigger = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Gravedigger"))
                .findFirst().orElseThrow();

        assertThat(gqs.getEffectivePower(gd, gravedigger)).isEqualTo(2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, gravedigger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, gravedigger)).isEqualTo(3);
    }

    @Test
    @DisplayName("Static bonus survives end-of-turn modifier reset")
    void staticBonusSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new LordOfTheUndead());
        harness.addToBattlefield(player1, new Gravedigger());

        Permanent gravedigger = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Gravedigger"))
                .findFirst().orElseThrow();

        gravedigger.setPowerModifier(gravedigger.getPowerModifier() + 5);
        assertThat(gqs.getEffectivePower(gd, gravedigger)).isEqualTo(8); // 2 base + 5 spell + 1 static

        gravedigger.resetModifiers();

        assertThat(gqs.getEffectivePower(gd, gravedigger)).isEqualTo(3); // 2 base + 1 static
        assertThat(gqs.getEffectiveToughness(gd, gravedigger)).isEqualTo(3);
    }

    // ===== Activated ability: activating =====

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingPutsOnStack() {
        addReadyLord(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setGraveyard(player1, List.of(new Gravedigger()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Lord of the Undead");
    }

    @Test
    @DisplayName("Activating ability taps Lord of the Undead")
    void activatingTapsLord() {
        Permanent lord = addReadyLord(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setGraveyard(player1, List.of(new Gravedigger()));

        assertThat(lord.isTapped()).isFalse();

        harness.activateAbility(player1, 0, null, null);

        assertThat(lord.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addReadyLord(player1);
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.setGraveyard(player1, List.of(new Gravedigger()));

        harness.activateAbility(player1, 0, null, null);

        // {1}{B} cost → 2 mana consumed, 2 remaining
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    // ===== Activated ability: resolution =====

    @Test
    @DisplayName("Returns Zombie card from graveyard to hand")
    void returnsZombieFromGraveyardToHand() {
        addReadyLord(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(new Gravedigger()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);

        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Gravedigger"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Gravedigger"));
    }

    @Test
    @DisplayName("Can choose specific Zombie when multiple are in graveyard")
    void choosesSpecificZombieFromGraveyard() {
        addReadyLord(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(new Gravedigger(), new GravebornMuse()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Choose Graveborn Muse (index 1)
        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Graveborn Muse"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Gravedigger"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Graveborn Muse"));
    }

    @Test
    @DisplayName("Player can decline graveyard choice")
    void playerCanDeclineGraveyardChoice() {
        addReadyLord(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setGraveyard(player1, List.of(new Gravedigger()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handleGraveyardCardChosen(player1, -1);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Gravedigger"));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Gravedigger"));
    }

    // ===== Activated ability: edge cases =====

    @Test
    @DisplayName("No effect when graveyard is empty")
    void noEffectWithEmptyGraveyard() {
        addReadyLord(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
        assertThat(gd.gameLog).anyMatch(s -> s.contains("no Zombie cards in graveyard"));
    }

    @Test
    @DisplayName("No effect when graveyard has no Zombie cards")
    void noEffectWithNoZombiesInGraveyard() {
        addReadyLord(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
        assertThat(gd.gameLog).anyMatch(s -> s.contains("no Zombie cards in graveyard"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Only Zombie cards are selectable when graveyard has mixed cards")
    void cannotChooseNonZombieFromGraveyard() {
        addReadyLord(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        // Index 0 = GrizzlyBears (not a Zombie), Index 1 = Gravedigger (Zombie)
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Gravedigger()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Trying to choose index 0 (Grizzly Bears) should fail
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
    }

    @Test
    @DisplayName("Non-creature Zombie cards are not in graveyard so only creatures qualify")
    void nonCreatureCardsNotSelectable() {
        addReadyLord(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setGraveyard(player1, List.of(new HolyDay()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
        assertThat(gd.gameLog).anyMatch(s -> s.contains("no Zombie cards in graveyard"));
    }

    // ===== Activated ability: validation =====

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyLord(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent lord = addReadyLord(player1);
        lord.tap();
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        // Add Lord with summoning sickness (creature with tap ability)
        LordOfTheUndead card = new LordOfTheUndead();
        Permanent lord = new Permanent(card);
        lord.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(lord);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    @Test
    @DisplayName("Opponent cannot make graveyard choice for controller")
    void opponentCannotChoose() {
        addReadyLord(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setGraveyard(player1, List.of(new Gravedigger()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn to choose");
    }

    // ===== Lord remains on battlefield =====

    @Test
    @DisplayName("Lord of the Undead remains on battlefield after activation and resolution")
    void remainsOnBattlefieldAfterResolution() {
        addReadyLord(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setGraveyard(player1, List.of(new Gravedigger()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Lord of the Undead"));
    }

    // ===== Helpers =====

    private Permanent addReadyLord(Player player) {
        LordOfTheUndead card = new LordOfTheUndead();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}


