package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ActivatedAbilitiesOfChosenNameCantBeActivatedEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PithingNeedleTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Pithing Needle has correct card properties")
    void hasCorrectProperties() {
        PithingNeedle card = new PithingNeedle();

        assertThat(card.getName()).isEqualTo("Pithing Needle");
        assertThat(card.getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(card.getManaCost()).isEqualTo("{1}");
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(ChooseCardNameOnEnterEffect.class);
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(ActivatedAbilitiesOfChosenNameCantBeActivatedEffect.class);
    }

    // ===== Casting and card name choice =====

    @Test
    @DisplayName("Casting Pithing Needle puts it on the stack as artifact spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new PithingNeedle()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Pithing Needle");
    }

    @Test
    @DisplayName("Resolving Pithing Needle enters battlefield and awaits card name choice")
    void resolvingTriggersCardNameChoice() {
        harness.setHand(player1, List.of(new PithingNeedle()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Pithing Needle"));
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COLOR_CHOICE);
        assertThat(gd.interaction.awaitingColorChoicePlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing a card name sets chosenName on the permanent")
    void choosingNameSetsOnPermanent() {
        harness.setHand(player1, List.of(new PithingNeedle()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleColorChosen(player1, "Prodigal Pyromancer");

        Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Pithing Needle"))
                .findFirst().orElseThrow();
        assertThat(perm.getChosenName()).isEqualTo("Prodigal Pyromancer");
    }

    @Test
    @DisplayName("Card name choice clears awaiting state")
    void cardNameChoiceClearsAwaitingState() {
        harness.setHand(player1, List.of(new PithingNeedle()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleColorChosen(player1, "Prodigal Pyromancer");

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.interaction.awaitingColorChoicePlayerId()).isNull();
    }

    @Test
    @DisplayName("Card name choice is logged")
    void cardNameChoiceIsLogged() {
        harness.setHand(player1, List.of(new PithingNeedle()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleColorChosen(player1, "Prodigal Pyromancer");

        assertThat(gd.gameLog).anyMatch(log ->
                log.contains("Prodigal Pyromancer") && log.contains("Pithing Needle"));
    }

    // ===== Blocking activated abilities =====

    @Test
    @DisplayName("Blocks non-mana activated abilities of the named card")
    void blocksNonManaActivatedAbilities() {
        // Put Pithing Needle naming "Prodigal Pyromancer" on the battlefield
        Permanent needle = addReadyPithingNeedle(player1, "Prodigal Pyromancer");

        // Put Prodigal Pyromancer on opponent's battlefield (tap: deal 1 damage)
        Card pyromancer = createCreatureWithTapAbility("Prodigal Pyromancer", 1, 1, CardColor.RED);
        Permanent pyromancerPerm = new Permanent(pyromancer);
        pyromancerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(pyromancerPerm);

        // Try to activate Prodigal Pyromancer's ability — should be blocked
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated")
                .hasMessageContaining("Pithing Needle");
    }

    @Test
    @DisplayName("Does NOT block mana abilities of the named card")
    void doesNotBlockManaAbilities() {
        // Put Pithing Needle naming "Birds of Paradise" on the battlefield
        Permanent needle = addReadyPithingNeedle(player1, "Birds of Paradise");

        // Put Birds of Paradise on opponent's battlefield (tap: add any color mana)
        Card birds = createCreatureWithManaAbility("Birds of Paradise", 0, 1, CardColor.GREEN);
        Permanent birdsPerm = new Permanent(birds);
        birdsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(birdsPerm);

        // Activating Birds' mana ability should NOT be blocked by Pithing Needle
        // Mana abilities are resolved immediately, so we should get a color choice prompt
        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COLOR_CHOICE);
    }

    @Test
    @DisplayName("Does NOT block abilities of differently-named cards")
    void doesNotBlockDifferentlyNamedCards() {
        // Put Pithing Needle naming "Some Other Card" on the battlefield
        Permanent needle = addReadyPithingNeedle(player1, "Some Other Card");

        // Put Prodigal Pyromancer on opponent's battlefield
        Card pyromancer = createCreatureWithTapAbility("Prodigal Pyromancer", 1, 1, CardColor.RED);
        Permanent pyromancerPerm = new Permanent(pyromancer);
        pyromancerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(pyromancerPerm);

        // Should be able to activate Prodigal Pyromancer normally
        harness.activateAbility(player2, 0, null, player1.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Prodigal Pyromancer");
    }

    @Test
    @DisplayName("Blocks abilities of the controller's own named cards")
    void blocksOwnCardsAbilities() {
        // Player1 plays Pithing Needle naming "Prodigal Pyromancer"
        Permanent needle = addReadyPithingNeedle(player1, "Prodigal Pyromancer");

        // Player1 ALSO has a Prodigal Pyromancer
        Card pyromancer = createCreatureWithTapAbility("Prodigal Pyromancer", 1, 1, CardColor.RED);
        Permanent pyromancerPerm = new Permanent(pyromancer);
        pyromancerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(pyromancerPerm);

        // Even the controller's own Pyromancer is blocked
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    // ===== Pithing Needle removal =====

    @Test
    @DisplayName("After Pithing Needle leaves the battlefield, abilities are usable again")
    void abilitiesWorkAfterNeedleRemoved() {
        Permanent needle = addReadyPithingNeedle(player1, "Prodigal Pyromancer");

        Card pyromancer = createCreatureWithTapAbility("Prodigal Pyromancer", 1, 1, CardColor.RED);
        Permanent pyromancerPerm = new Permanent(pyromancer);
        pyromancerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(pyromancerPerm);

        // Verify blocked
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");

        // Remove Pithing Needle from battlefield
        gd.playerBattlefields.get(player1.getId()).remove(needle);

        // Now the ability should work
        harness.activateAbility(player2, 0, null, player1.getId());
        assertThat(gd.stack).hasSize(1);
    }

    // ===== Multiple Pithing Needles =====

    @Test
    @DisplayName("Multiple Pithing Needles can name different cards")
    void multiplePithingNeedlesBlockDifferentCards() {
        addReadyPithingNeedle(player1, "Prodigal Pyromancer");
        addReadyPithingNeedle(player1, "Siege-Gang Commander");

        // Prodigal Pyromancer on opponent's side
        Card pyromancer = createCreatureWithTapAbility("Prodigal Pyromancer", 1, 1, CardColor.RED);
        Permanent pyromancerPerm = new Permanent(pyromancer);
        pyromancerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(pyromancerPerm);

        // Prodigal Pyromancer should be blocked
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    // ===== No chosen name (edge case) =====

    @Test
    @DisplayName("Pithing Needle with no chosen name does not block anything")
    void noChosenNameDoesNotBlock() {
        // Add Pithing Needle without setting a chosen name (e.g. it was never resolved)
        PithingNeedle needleCard = new PithingNeedle();
        Permanent needlePerm = new Permanent(needleCard);
        gd.playerBattlefields.get(player1.getId()).add(needlePerm);

        Card pyromancer = createCreatureWithTapAbility("Prodigal Pyromancer", 1, 1, CardColor.RED);
        Permanent pyromancerPerm = new Permanent(pyromancer);
        pyromancerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(pyromancerPerm);

        // Should activate normally — no name was chosen
        harness.activateAbility(player2, 0, null, player1.getId());
        assertThat(gd.stack).hasSize(1);
    }

    // ===== Helpers =====

    private Permanent addReadyPithingNeedle(Player player, String chosenName) {
        PithingNeedle card = new PithingNeedle();
        Permanent perm = new Permanent(card);
        perm.setChosenName(chosenName);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private static Card createCreatureWithTapAbility(String name, int power, int toughness, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        card.addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new DealDamageToAnyTargetEffect(1)),
                true, "{T}: " + name + " deals 1 damage to any target."
        ));
        return card;
    }

    private static Card createCreatureWithManaAbility(String name, int power, int toughness, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        card.addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new AwardAnyColorManaEffect()),
                false, "{T}: Add one mana of any color."
        ));
        return card;
    }
}
