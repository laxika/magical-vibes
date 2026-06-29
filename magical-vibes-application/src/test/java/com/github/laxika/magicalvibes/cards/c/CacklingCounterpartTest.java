package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.b.BlisterstickShaman;
import com.github.laxika.magicalvibes.cards.b.BurningSunsAvatar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HomaridExplorer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CacklingCounterpartTest extends BaseCardTest {

    @Test
    @DisplayName("Cackling Counterpart has correct effect structure")
    void hasCorrectEffectStructure() {
        CacklingCounterpart card = new CacklingCounterpart();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst()).isInstanceOf(CreateTokenCopyOfTargetPermanentEffect.class);
        FlashbackCast flashback = card.getCastingOption(FlashbackCast.class).orElseThrow();
        assertThat(flashback.getCost(ManaCastingCost.class).orElseThrow().manaCost()).isEqualTo("{5}{U}{U}");
    }

    @Test
    @DisplayName("Creates a token copy of target creature you control")
    void createsTokenCopyOfCreatureYouControl() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CacklingCounterpart()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        long bearsCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .count();
        assertThat(bearsCount).isEqualTo(2);

        long tokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears") && p.getCard().isToken())
                .count();
        assertThat(tokenCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target opponent's creature")
    void cannotTargetOpponentsCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CacklingCounterpart()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Spell goes to graveyard after normal cast")
    void spellGoesToGraveyardAfterNormalCast() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CacklingCounterpart()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Cackling Counterpart"));
    }

    @Test
    @DisplayName("Flashback creates a token copy from graveyard")
    void flashbackCreatesTokenCopy() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new CacklingCounterpart()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castFlashback(player1, 0, targetId);
        harness.passBothPriorities();

        long bearsCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .count();
        assertThat(bearsCount).isEqualTo(2);

        long tokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears") && p.getCard().isToken())
                .count();
        assertThat(tokenCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Flashback exiles the card after resolving")
    void flashbackExilesAfterResolving() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new CacklingCounterpart()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castFlashback(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Cackling Counterpart"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Cackling Counterpart"));
    }

    @Test
    @DisplayName("Fizzles when target creature is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CacklingCounterpart()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        // Remove the target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Token copy ETB target selection (CR 603.3) =====

    @Test
    @DisplayName("Token copy of creature with targeted ETB prompts for target at trigger time")
    void tokenCopyOfTargetedETBPromptsForTarget() {
        harness.addToBattlefield(player1, new HomaridExplorer());
        harness.setHand(player1, List.of(new CacklingCounterpart()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID originalHomaridId = harness.getPermanentId(player1, "Homarid Explorer");
        harness.castInstant(player1, 0, originalHomaridId);
        harness.passBothPriorities(); // Cackling Counterpart resolves → token created → ETB target prompt

        // Engine must be awaiting a target choice for the token's ETB ability.
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("Token of Homarid Explorer mills the chosen player when its ETB resolves")
    void tokenHomaridExplorerMillsChosenPlayer() {
        // Trim Bob's deck so we can easily see mill counts.
        List<com.github.laxika.magicalvibes.model.Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }

        harness.addToBattlefield(player1, new HomaridExplorer());
        harness.setHand(player1, List.of(new CacklingCounterpart()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID originalHomaridId = harness.getPermanentId(player1, "Homarid Explorer");
        harness.castInstant(player1, 0, originalHomaridId);
        harness.passBothPriorities(); // resolves CC → token ETB awaits target

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, player2.getId());

        // The ETB is now on the stack targeting Bob.
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());

        harness.passBothPriorities(); // resolve the ETB trigger
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(6);
    }

    @Test
    @DisplayName("Token of Blisterstick Shaman deals 1 damage to chosen any-target")
    void tokenBlisterstickShamanDealsDamageToAnyTarget() {
        harness.addToBattlefield(player1, new BlisterstickShaman());
        harness.setHand(player1, List.of(new CacklingCounterpart()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int startingLife = gd.getLife(player2.getId());

        UUID originalId = harness.getPermanentId(player1, "Blisterstick Shaman");
        harness.castInstant(player1, 0, originalId);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());

        harness.passBothPriorities(); // resolve the ETB trigger
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 1);
    }

    @Test
    @DisplayName("Token copy of creature without targeted ETB does not prompt for a target")
    void tokenCopyOfNonTargetedETBDoesNotPrompt() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CacklingCounterpart()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Token of Homarid Explorer can target the controller (self-mill)")
    void tokenHomaridExplorerCanTargetSelf() {
        List<com.github.laxika.magicalvibes.model.Card> deck = gd.playerDecks.get(player1.getId());
        while (deck.size() > 10) {
            deck.removeFirst();
        }

        harness.addToBattlefield(player1, new HomaridExplorer());
        harness.setHand(player1, List.of(new CacklingCounterpart()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID originalHomaridId = harness.getPermanentId(player1, "Homarid Explorer");
        harness.castInstant(player1, 0, originalHomaridId);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, player1.getId());

        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> !c.getName().equals("Cackling Counterpart"))
                .hasSize(4);
    }

    // ===== Multi-target ETB on token copies =====

    @Test
    @DisplayName("Token of Burning Sun's Avatar prompts for two targets (mandatory + optional)")
    void tokenBurningSunsAvatarPromptsForTwoTargets() {
        harness.addToBattlefield(player1, new BurningSunsAvatar());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CacklingCounterpart()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gd.playerLifeTotals.put(player2.getId(), 20);

        UUID avatarId = harness.getPermanentId(player1, "Burning Sun's Avatar");
        harness.castInstant(player1, 0, avatarId);
        harness.passBothPriorities(); // CC resolves, token created, first group prompt

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        // Choose Bob for the opponent/planeswalker group (mandatory).
        harness.handlePermanentChosen(player1, player2.getId());

        // Second group (optional creature) now prompts.
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        // ETB is now on the stack with both targets.
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetIds()).containsExactly(player2.getId(), bearsId);

        harness.passBothPriorities(); // resolve ETB trigger
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Token of Burning Sun's Avatar can skip optional creature target")
    void tokenBurningSunsAvatarCanSkipOptionalTarget() {
        harness.addToBattlefield(player1, new BurningSunsAvatar());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CacklingCounterpart()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gd.playerLifeTotals.put(player2.getId(), 20);

        UUID avatarId = harness.getPermanentId(player1, "Burning Sun's Avatar");
        harness.castInstant(player1, 0, avatarId);
        harness.passBothPriorities();

        // First group: choose Bob.
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, player2.getId());

        // Second group: skip by choosing our own player ID.
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, player1.getId());

        // ETB on the stack with only the first target.
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetIds()).containsExactly(player2.getId());

        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        // Grizzly Bears survives (not targeted).
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Token copy of creature with an 'up to 2 creatures' ETB group prompts twice and stops on self-select")
    void tokenCopyUpToTwoCreaturesPromptsSlotBySlot() {
        // Synthetic test creature: single group with max=2 creature targets
        // (exercises the max>1 slot-by-slot selection path on token copies).
        Card upToTwoCreature = new Card();
        upToTwoCreature.setName("Test Multi Target");
        upToTwoCreature.setType(CardType.CREATURE);
        upToTwoCreature.setPower(2);
        upToTwoCreature.setToughness(2);
        upToTwoCreature.setManaCost("{2}{R}");
        upToTwoCreature.target(
                new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "creature"),
                0, 2
        ).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToTargetCreatureEffect(1));

        harness.addToBattlefield(player1, upToTwoCreature);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CacklingCounterpart()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID sourceId = harness.getPermanentId(player1, "Test Multi Target");
        harness.castInstant(player1, 0, sourceId);
        harness.passBothPriorities(); // CC resolves → token created → group slot 1 prompt

        // First slot in the max=2 group.
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        UUID bear1 = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow().getId();
        harness.handlePermanentChosen(player1, bear1);

        // Second slot. min=0 is satisfied, so self-select becomes a valid "stop" option.
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, player1.getId()); // stop the group

        // All groups done → ETB pushed onto stack with one chosen target.
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetIds()).containsExactly(bear1);
    }

    @Test
    @DisplayName("Token copy of creature with an 'up to 2 creatures' ETB group fills both slots when player picks two")
    void tokenCopyUpToTwoCreaturesFillsBothSlots() {
        Card upToTwoCreature = new Card();
        upToTwoCreature.setName("Test Multi Target");
        upToTwoCreature.setType(CardType.CREATURE);
        upToTwoCreature.setPower(2);
        upToTwoCreature.setToughness(2);
        upToTwoCreature.setManaCost("{2}{R}");
        upToTwoCreature.target(
                new PermanentPredicateTargetFilter(new PermanentIsCreaturePredicate(), "creature"),
                0, 2
        ).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToTargetCreatureEffect(1));

        harness.addToBattlefield(player1, upToTwoCreature);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CacklingCounterpart()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID sourceId = harness.getPermanentId(player1, "Test Multi Target");
        harness.castInstant(player1, 0, sourceId);
        harness.passBothPriorities();

        List<UUID> bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .map(Permanent::getId)
                .toList();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, bears.get(0));

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, bears.get(1));

        // max=2 reached → no more prompts, ETB pushed with both targets.
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetIds()).containsExactlyInAnyOrder(bears.get(0), bears.get(1));
    }

    @Test
    @DisplayName("Token copy has same abilities as the original")
    void tokenCopyHasSameAbilities() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CacklingCounterpart()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent tokenCopy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears") && p.getCard().isToken())
                .findFirst().orElseThrow();

        // Token should have copied the card's keywords
        assertThat(tokenCopy.getCard().getKeywords()).isEqualTo(
                gd.playerBattlefields.get(player1.getId()).stream()
                        .filter(p -> p.getCard().getName().equals("Grizzly Bears") && !p.getCard().isToken())
                        .findFirst().orElseThrow().getCard().getKeywords()
        );
    }
}
