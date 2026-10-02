package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LoomingAltisaur;
import com.github.laxika.magicalvibes.cards.k.KinjallisCaller;
import com.github.laxika.magicalvibes.cards.t.TempleOfAclazotz;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArguelsBloodFast.class, TempleOfAclazotz.class, GrizzlyBears.class, LoomingAltisaur.class, KinjallisCaller.class})
class ArguelsBloodFastTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability draws a card and costs 2 life")
    void activatedAbilityDrawsCardAndCostsLife() {
        addEnchantmentReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        Card cardInLibrary = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, cardInLibrary);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addEnchantmentReady(player1);
        harness.setLife(player1, 20);
        // No mana added

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate ability without enough life")
    void cannotActivateWithoutEnoughLife() {
        addEnchantmentReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 1); // Only 1 life, need 2

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activation at exactly 2 life is accepted but player loses before ability resolves (CR 704.5a)")
    void canActivateWithExactly2Life() {
        addEnchantmentReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 2);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        Card cardInLibrary = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).add(0, cardInLibrary);

        // Activation is accepted (2 >= 2), life cost is paid, but SBAs fire immediately
        // and the player loses at 0 life before the ability resolves (CR 704.3 / 704.5a)
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.getLife(player1.getId())).isEqualTo(0);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Transforms when accepting may at 5 life during upkeep")
    void transformsAtFiveLife() {
        Permanent enchantment = addEnchantmentReady(player1);
        harness.setLife(player1, 5);

        advanceToUpkeep(player1); // advance to upkeep, trigger goes on stack
        harness.passBothPriorities(); // resolve triggered ability — queues may prompt
        harness.handleMayAbilityChosen(player1, true); // accept transform

        assertThat(enchantment.isTransformed()).isTrue();
        assertThat(enchantment.getCard().getName()).isEqualTo("Temple of Aclazotz");
    }

    @Test
    @DisplayName("Does not transform when declining may at 5 life")
    void doesNotTransformWhenDeclined() {
        Permanent enchantment = addEnchantmentReady(player1);
        harness.setLife(player1, 5);

        advanceToUpkeep(player1); // advance to upkeep
        harness.passBothPriorities(); // resolve triggered ability
        harness.handleMayAbilityChosen(player1, false); // decline transform

        assertThat(enchantment.isTransformed()).isFalse();
        assertThat(enchantment.getCard().getName()).isEqualTo("Arguel's Blood Fast");
    }

    @Test
    @DisplayName("Does not trigger at 6 or more life")
    void doesNotTriggerAboveFiveLife() {
        Permanent enchantment = addEnchantmentReady(player1);
        harness.setLife(player1, 6);

        advanceToUpkeep(player1); // advance to upkeep — no trigger

        assertThat(enchantment.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Triggers at 1 life")
    void triggersAtOneLife() {
        Permanent enchantment = addEnchantmentReady(player1);
        harness.setLife(player1, 1);

        advanceToUpkeep(player1); // advance to upkeep, trigger goes on stack
        harness.passBothPriorities(); // resolve triggered ability
        harness.handleMayAbilityChosen(player1, true);

        assertThat(enchantment.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Temple of Aclazotz sacrifice ability gains life equal to toughness")
    void templeGainsLifeEqualToToughness() {
        // Set up a transformed Arguel's Blood Fast (which is Temple of Aclazotz)
        Permanent temple = addTransformedTemple(player1);
        // Only one creature — auto-sacrificed (Temple is a land, not a creature)
        addCreatureReady(player1, createCreature("Beefy Beast", 2, 4));
        harness.setLife(player1, 10);

        int templeIdx = indexOf(player1, temple);
        harness.activateAbility(player1, templeIdx, 0, null, null);
        assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
        harness.passBothPriorities();

        // Gained 4 life (creature toughness)
        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        // Creature should be in graveyard
        harness.assertInGraveyard(player1, "Beefy Beast");
    }

    @Test
    @DisplayName("Temple sacrifice with 1/1 creature gains 1 life")
    void templeSacrifice1_1GainsOneLife() {
        Permanent temple = addTransformedTemple(player1);
        // Only one creature — auto-sacrificed
        addCreatureReady(player1, createCreature("Goblin Token", 1, 1));
        harness.setLife(player1, 10);

        int templeIdx = indexOf(player1, temple);
        harness.activateAbility(player1, templeIdx, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(11);
    }

    @Test
    @DisplayName("Upkeep trigger does nothing if life rises above five before resolution")
    void doesNotTransformWhenLifeRisesBeforeResolution() {
        Permanent enchantment = addEnchantmentReady(player1);
        harness.setLife(player1, 5);
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.setLife(player1, 6);
        harness.passBothPriorities();

        assertThat(enchantment.isTransformed()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent enchantment = addEnchantmentReady(player1);
        harness.setLife(player1, 5);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(enchantment.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Temple cannot sacrifice when its controller has no creatures")
    void templeCannotActivateWithoutCreature() {
        Permanent temple = addTransformedTemple(player1);
        addCreatureReady(player2, new LoomingAltisaur());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, temple), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Temple cannot activate its sacrifice ability while tapped")
    void templeCannotActivateWhileTapped() {
        Permanent temple = addTransformedTemple(player1);
        temple.setTapped(true);
        addCreatureReady(player1, new LoomingAltisaur());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(player1, temple), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Looming Altisaur");
    }

    @Test
    @DisplayName("Temple uses toughness including counters and sacrifices as an activation cost")
    void templeUsesModifiedToughnessAtActivation() {
        Permanent temple = addTransformedTemple(player1);
        Permanent creature = addCreatureReady(player1, new LoomingAltisaur());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLife(player1, 10);

        harness.activateAbility(player1, indexOf(player1, temple), 0, null, null);

        assertThat(temple.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Looming Altisaur");
        harness.assertLife(player1, 10);
        harness.passBothPriorities();
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Temple taps for black mana immediately without using the stack")
    void templeAddsBlackMana() {
        Permanent temple = addTransformedTemple(player1);
        int before = gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK);

        harness.tapPermanent(player1, indexOf(player1, temple));

        assertThat(temple.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(before + 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Life is paid before drawing and the front face ability does not tap")
    void lifeIsPaidBeforeDrawResolves() {
        Permanent enchantment = addEnchantmentReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 10);
        gd.playerDecks.get(player1.getId()).addFirst(new LoomingAltisaur());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, indexOf(player1, enchantment), null, null);

        harness.assertLife(player1, 8);
        assertThat(enchantment.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player1, "Looming Altisaur");
    }

    @Test
    @DisplayName("Temple gains toughness of the chosen creature when multiple creatures are available")
    void templeUsesChosenCreatureToughness() {
        Permanent temple = addTransformedTemple(player1);
        addCreatureReady(player1, new KinjallisCaller());
        Permanent chosen = addCreatureReady(player1, new LoomingAltisaur());
        harness.setLife(player1, 10);

        harness.activateAbility(player1, indexOf(player1, temple), 0, null, null);
        harness.handlePermanentChosen(player1, chosen.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertOnBattlefield(player1, "Kinjalli's Caller");
        harness.assertInGraveyard(player1, "Looming Altisaur");
    }

    private Permanent addEnchantmentReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ArguelsBloodFast());
    }

    private Permanent addTransformedTemple(Player player) {
        ArguelsBloodFast card = new ArguelsBloodFast();
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        // Transform to back face
        perm.setCard(card.getBackFaceCard());
        perm.setTransformed(true);
        return perm;
    }

    private Card createCreature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
