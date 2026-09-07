package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CircleOfProtectionRed;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhostlyFlame.class, CircleOfProtectionRed.class, Incinerate.class})
class GhostlyFlameTest extends BaseCardTest {

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

    private static Card createCreatureWithProtection(String name, int power, int toughness,
                                                      CardColor color, CardColor protectedColor) {
        Card card = createCreature(name, power, toughness, color);
        card.addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(protectedColor)));
        return card;
    }

    private static Card createMulticoloredCreature(String name, int power, int toughness,
                                                     CardColor primaryColor, CardColor secondaryColor) {
        Card card = createCreature(name, power, toughness, primaryColor);
        card.setColors(List.of(primaryColor, secondaryColor));
        return card;
    }

    private void addGhostlyFlame() {
        harness.addToBattlefield(player1, new GhostlyFlame());
    }

    @Test
    @DisplayName("Protection from red no longer prevents combat damage from a red creature")
    void redCombatDamageIsNotPreventedByProtectionFromRed() {
        Permanent attacker = addCreatureReady(player1, createCreature("Big Goblin", 3, 3, CardColor.RED));
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2,
                createCreatureWithProtection("Red Ward", 1, 2, CardColor.WHITE, CardColor.RED));
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        addGhostlyFlame();

        resolveCombat();

        assertThat(countPermanents(player2, "Red Ward")).isZero();
    }

    @Test
    @DisplayName("Protection from green still prevents combat damage from a green creature")
    void greenCombatDamageIsStillPrevented() {
        Permanent attacker = addCreatureReady(player1, createCreature("Big Bear", 3, 3, CardColor.GREEN));
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2,
                createCreatureWithProtection("Ward Wall", 0, 1, CardColor.WHITE, CardColor.GREEN));
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        addGhostlyFlame();

        resolveCombat();

        assertThat(countPermanents(player2, "Ward Wall")).isEqualTo(1);
    }

    @Test
    @DisplayName("Protection from black no longer prevents combat damage from a black creature")
    void blackCombatDamageIsNotPreventedByProtectionFromBlack() {
        Permanent attacker = addCreatureReady(player1, createCreature("Big Zombie", 3, 3, CardColor.BLACK));
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2,
                createCreatureWithProtection("Ward Wall", 0, 1, CardColor.WHITE, CardColor.BLACK));
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        addGhostlyFlame();

        resolveCombat();

        assertThat(countPermanents(player2, "Ward Wall")).isZero();
    }

    @Test
    @DisplayName("A multicolored red source deals combat damage as colorless")
    void multicoloredRedCombatDamageIsColorless() {
        Permanent attacker = addCreatureReady(player1,
                createMulticoloredCreature("Red Blue Attacker", 3, 3, CardColor.RED, CardColor.BLUE));
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2,
                createCreatureWithProtection("Blue Ward", 0, 1, CardColor.WHITE, CardColor.BLUE));
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        addGhostlyFlame();

        resolveCombat();

        assertThat(countPermanents(player2, "Blue Ward")).isZero();
    }

    @Test
    @DisplayName("Protection from red still stops a red spell from targeting")
    void redSpellStillCannotTarget() {
        Permanent redWard = addCreatureReady(player2,
                createCreatureWithProtection("Red Ward", 1, 2, CardColor.WHITE, CardColor.RED));

        // A second, legal target keeps the spell playable so the failure is target legality,
        // not an empty playable list.
        addCreatureReady(player2, createCreature("Bears", 2, 2, CardColor.GREEN));

        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);

        addGhostlyFlame();

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, redWard.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("A red spell deals damage through Circle of Protection: Red")
    void redSpellDealsDamageThroughCircleOfProtectionRed() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new CircleOfProtectionRed());
        addGhostlyFlame();

        Incinerate incinerate = new Incinerate();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(incinerate));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(incinerate.getId());

        harness.handlePermanentChosen(player1, incinerate.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("A red creature still can't block a creature with protection from red")
    void redCreatureStillCannotBlock() {
        Permanent attacker = addCreatureReady(player1,
                createCreatureWithProtection("Red Ward", 1, 2, CardColor.WHITE, CardColor.RED));
        attacker.setAttacking(true);

        addCreatureReady(player2, createCreature("Goblin", 2, 2, CardColor.RED));

        addGhostlyFlame();

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }
}
