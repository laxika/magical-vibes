package com.github.laxika.magicalvibes.cards.p;

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

class PhyrexianRevokerTest extends BaseCardTest {

    // ===== Card effects =====

    @Test
    @DisplayName("Phyrexian Revoker has choose-nonland-card-name ETB and static lock effects")
    void hasCorrectEffects() {
        PhyrexianRevoker card = new PhyrexianRevoker();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        ChooseCardNameOnEnterEffect nameEffect = (ChooseCardNameOnEnterEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(nameEffect.excludedTypes()).containsExactly(CardType.LAND);

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        ActivatedAbilitiesOfChosenNameCantBeActivatedEffect lockEffect =
                (ActivatedAbilitiesOfChosenNameCantBeActivatedEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(lockEffect.blocksManaAbilities()).isTrue();
    }

    // ===== Casting and card name choice =====

    @Test
    @DisplayName("Casting Phyrexian Revoker puts it on the stack as creature spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new PhyrexianRevoker()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Phyrexian Revoker");
    }

    @Test
    @DisplayName("Resolving Phyrexian Revoker awaits card name choice before entering battlefield")
    void resolvingTriggersCardNameChoice() {
        harness.setHand(player1, List.of(new PhyrexianRevoker()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Permanent should NOT be on the battlefield yet — name must be chosen first (Rule 614.1c)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Phyrexian Revoker"));
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COLOR_CHOICE);
        assertThat(gd.interaction.awaitingColorChoicePlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing a card name sets chosenName on the permanent")
    void choosingNameSetsOnPermanent() {
        harness.setHand(player1, List.of(new PhyrexianRevoker()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleColorChosen(player1, "Prodigal Pyromancer");

        Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Revoker"))
                .findFirst().orElseThrow();
        assertThat(perm.getChosenName()).isEqualTo("Prodigal Pyromancer");
    }

    // ===== Blocking activated abilities =====

    @Test
    @DisplayName("Blocks non-mana activated abilities of the named card")
    void blocksNonManaActivatedAbilities() {
        Permanent revoker = addReadyRevoker(player1, "Prodigal Pyromancer");

        Card pyromancer = createCreatureWithTapAbility("Prodigal Pyromancer", 1, 1, CardColor.RED);
        Permanent pyromancerPerm = new Permanent(pyromancer);
        pyromancerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(pyromancerPerm);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("DOES block mana abilities of the named card (unlike Pithing Needle)")
    void blocksManaAbilities() {
        Permanent revoker = addReadyRevoker(player1, "Birds of Paradise");

        Card birds = createCreatureWithManaAbility("Birds of Paradise", 0, 1, CardColor.GREEN);
        Permanent birdsPerm = new Permanent(birds);
        birdsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(birdsPerm);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated")
                .hasMessageContaining("Phyrexian Revoker");
    }

    @Test
    @DisplayName("Does NOT block abilities of differently-named cards")
    void doesNotBlockDifferentlyNamedCards() {
        Permanent revoker = addReadyRevoker(player1, "Some Other Card");

        Card pyromancer = createCreatureWithTapAbility("Prodigal Pyromancer", 1, 1, CardColor.RED);
        Permanent pyromancerPerm = new Permanent(pyromancer);
        pyromancerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(pyromancerPerm);

        harness.activateAbility(player2, 0, null, player1.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Prodigal Pyromancer");
    }

    @Test
    @DisplayName("Blocks abilities of the controller's own named cards")
    void blocksOwnCardsAbilities() {
        Permanent revoker = addReadyRevoker(player1, "Prodigal Pyromancer");

        Card pyromancer = createCreatureWithTapAbility("Prodigal Pyromancer", 1, 1, CardColor.RED);
        Permanent pyromancerPerm = new Permanent(pyromancer);
        pyromancerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(pyromancerPerm);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    // ===== Revoker removal =====

    @Test
    @DisplayName("After Phyrexian Revoker leaves the battlefield, abilities are usable again")
    void abilitiesWorkAfterRevokerRemoved() {
        Permanent revoker = addReadyRevoker(player1, "Prodigal Pyromancer");

        Card pyromancer = createCreatureWithTapAbility("Prodigal Pyromancer", 1, 1, CardColor.RED);
        Permanent pyromancerPerm = new Permanent(pyromancer);
        pyromancerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(pyromancerPerm);

        // Verify blocked
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");

        // Remove Phyrexian Revoker from battlefield
        gd.playerBattlefields.get(player1.getId()).remove(revoker);

        // Now the ability should work
        harness.activateAbility(player2, 0, null, player1.getId());
        assertThat(gd.stack).hasSize(1);
    }

    // ===== Helpers =====

    private Permanent addReadyRevoker(Player player, String chosenName) {
        PhyrexianRevoker card = new PhyrexianRevoker();
        Permanent perm = new Permanent(card);
        perm.setChosenName(chosenName);
        perm.setSummoningSick(false);
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
