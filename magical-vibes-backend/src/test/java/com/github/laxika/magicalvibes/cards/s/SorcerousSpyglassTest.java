package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ActivatedAbilitiesOfChosenNameCantBeActivatedEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SorcerousSpyglassTest extends BaseCardTest {

    // ===== Card effects =====

    @Test
    @DisplayName("Sorcerous Spyglass has choose-card-name-with-look-at-hand ETB and static lock effects")
    void hasCorrectEffects() {
        SorcerousSpyglass card = new SorcerousSpyglass();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        ChooseCardNameOnEnterEffect nameEffect = (ChooseCardNameOnEnterEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(nameEffect.excludedTypes()).isEmpty();
        assertThat(nameEffect.lookAtOpponentHand()).isTrue();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        ActivatedAbilitiesOfChosenNameCantBeActivatedEffect lockEffect =
                (ActivatedAbilitiesOfChosenNameCantBeActivatedEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(lockEffect.blocksManaAbilities()).isFalse();
    }

    // ===== Casting and card name choice =====

    @Test
    @DisplayName("Casting Sorcerous Spyglass puts it on the stack as artifact spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new SorcerousSpyglass()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Sorcerous Spyglass");
    }

    @Test
    @DisplayName("Resolving Sorcerous Spyglass reveals opponent's hand and awaits card name choice")
    void resolvingRevealsHandAndTriggersCardNameChoice() {
        Card cardInOpponentHand = new SorcerousSpyglass();
        harness.setHand(player2, List.of(cardInOpponentHand));

        harness.setHand(player1, List.of(new SorcerousSpyglass()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        // Opponent's hand should be revealed in game log
        assertThat(gd.gameLog).anyMatch(log -> log.contains("looks at") && log.contains("hand"));

        // Permanent should NOT be on the battlefield yet — name must be chosen first (Rule 614.1c)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Sorcerous Spyglass"));
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COLOR_CHOICE);
        assertThat(gd.interaction.colorChoice().playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Resolving with empty opponent hand logs that hand is empty")
    void resolvingWithEmptyHandLogsEmpty() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new SorcerousSpyglass()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("looks at") && log.contains("empty"));
    }

    @Test
    @DisplayName("RevealHandMessage is sent to the controller")
    void revealHandMessageSentToController() {
        Card cardInOpponentHand = new SorcerousSpyglass();
        harness.setHand(player2, List.of(cardInOpponentHand));

        harness.setHand(player1, List.of(new SorcerousSpyglass()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.clearMessages();
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        List<String> p1Messages = harness.getConn1().getSentMessages();
        assertThat(p1Messages).anyMatch(msg -> msg.contains("REVEAL_HAND"));
    }

    @Test
    @DisplayName("Choosing a card name sets chosenName on the permanent")
    void choosingNameSetsOnPermanent() {
        harness.setHand(player1, List.of(new SorcerousSpyglass()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Prodigal Pyromancer");

        Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Sorcerous Spyglass"))
                .findFirst().orElseThrow();
        assertThat(perm.getChosenName()).isEqualTo("Prodigal Pyromancer");
    }

    // ===== Blocking activated abilities =====

    @Test
    @DisplayName("Blocks non-mana activated abilities of the named card")
    void blocksNonManaActivatedAbilities() {
        Permanent spyglass = addReadySpyglass(player1, "Prodigal Pyromancer");

        Card pyromancer = createCreatureWithTapAbility("Prodigal Pyromancer", 1, 1, CardColor.RED);
        Permanent pyromancerPerm = new Permanent(pyromancer);
        pyromancerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(pyromancerPerm);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Does NOT block mana abilities of the named card")
    void doesNotBlockManaAbilities() {
        Permanent spyglass = addReadySpyglass(player1, "Birds of Paradise");

        Card birds = createCreatureWithManaAbility("Birds of Paradise", 0, 1, CardColor.GREEN);
        Permanent birdsPerm = new Permanent(birds);
        birdsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(birdsPerm);

        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COLOR_CHOICE);
    }

    @Test
    @DisplayName("Does NOT block abilities of differently-named cards")
    void doesNotBlockDifferentlyNamedCards() {
        Permanent spyglass = addReadySpyglass(player1, "Some Other Card");

        Card pyromancer = createCreatureWithTapAbility("Prodigal Pyromancer", 1, 1, CardColor.RED);
        Permanent pyromancerPerm = new Permanent(pyromancer);
        pyromancerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(pyromancerPerm);

        harness.activateAbility(player2, 0, null, player1.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Prodigal Pyromancer");
    }

    // ===== Spyglass removal =====

    @Test
    @DisplayName("After Sorcerous Spyglass leaves the battlefield, abilities are usable again")
    void abilitiesWorkAfterSpyglassRemoved() {
        Permanent spyglass = addReadySpyglass(player1, "Prodigal Pyromancer");

        Card pyromancer = createCreatureWithTapAbility("Prodigal Pyromancer", 1, 1, CardColor.RED);
        Permanent pyromancerPerm = new Permanent(pyromancer);
        pyromancerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(pyromancerPerm);

        // Verify blocked
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");

        // Remove Sorcerous Spyglass from battlefield
        gd.playerBattlefields.get(player1.getId()).remove(spyglass);

        // Now the ability should work
        harness.activateAbility(player2, 0, null, player1.getId());
        assertThat(gd.stack).hasSize(1);
    }

    // ===== Helpers =====

    private Permanent addReadySpyglass(Player player, String chosenName) {
        SorcerousSpyglass card = new SorcerousSpyglass();
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
                "{T}: " + name + " deals 1 damage to any target."
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
                "{T}: Add one mana of any color."
        ));
        return card;
    }
}
