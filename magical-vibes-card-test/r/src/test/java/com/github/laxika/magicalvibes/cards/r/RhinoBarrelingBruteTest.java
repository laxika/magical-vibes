package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.Aetherize;
import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RhinoBarrelingBrute.class, GrizzlyBears.class, Shock.class, Aetherize.class, Fireball.class})
class RhinoBarrelingBruteTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when it attacks after a spell with mana value 4 or greater was cast")
    void drawsAfterCastingHighManaValueSpell() {
        harness.setHand(player1, List.of(new RhinoBarrelingBrute()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst()).isInstanceOf(GrizzlyBears.class);
    }

    @Test
    @DisplayName("Does not draw when only a spell with mana value less than 4 was cast")
    void doesNotDrawAfterCastingLowManaValueSpell() {
        Permanent rhino = addCreatureReady(player1, new RhinoBarrelingBrute());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(rhino)));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when the qualifying spell is cast after the attack")
    void doesNotTriggerWhenQualifyingSpellIsCastAfterAttack() {
        Permanent rhino = addCreatureReady(player1, new RhinoBarrelingBrute());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Aetherize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(rhino)));
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card instanceof GrizzlyBears);
    }

    @Test
    @DisplayName("Draws after casting a spell with mana value exactly four")
    void drawsAtExactManaValueThreshold() {
        addCreatureReady(player1, new RhinoBarrelingBrute());
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new Aetherize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0);
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).singleElement().isInstanceOf(Shock.class);
    }

    @Test
    @DisplayName("An opponent's qualifying spell does not enable the draw")
    void opponentSpellDoesNotQualify() {
        addCreatureReady(player1, new RhinoBarrelingBrute());
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(new Aetherize()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player2, 0);
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A resolved X spell qualifies using its mana value when cast")
    void chosenXCountsTowardManaValue() {
        addCreatureReady(player1, new RhinoBarrelingBrute());
        harness.setLibrary(player1, List.of(new Shock()));
        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).singleElement().isInstanceOf(Shock.class);
    }

    @Test
    @DisplayName("Does not draw when no spell was cast this turn")
    void noSpellDoesNotQualify() {
        addCreatureReady(player1, new RhinoBarrelingBrute());
        harness.setLibrary(player1, List.of(new Shock()));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
