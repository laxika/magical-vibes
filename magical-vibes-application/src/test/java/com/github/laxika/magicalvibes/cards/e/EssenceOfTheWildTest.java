package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.d.DoomedTraveler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CreaturesEnterAsCopyOfSourceEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EssenceOfTheWildTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has CreaturesEnterAsCopyOfSourceEffect as static effect")
    void hasCorrectStaticEffect() {
        EssenceOfTheWild card = new EssenceOfTheWild();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(CreaturesEnterAsCopyOfSourceEffect.class);
    }

    // ===== Replacement effect — cast creature =====

    @Test
    @DisplayName("Creature cast while Essence is on battlefield enters as a copy of Essence")
    void creatureEntersAsCopyOfEssence() {
        harness.addToBattlefield(player1, new EssenceOfTheWild());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // The Grizzly Bears should have entered as a copy of Essence of the Wild
        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        assertThat(bf).hasSize(2);

        Permanent copy = bf.stream()
                .filter(p -> p.getOriginalCard().getName().equals("Grizzly Bears"))
                .findFirst().orElse(null);
        assertThat(copy).isNotNull();
        assertThat(copy.getCard().getName()).isEqualTo("Essence of the Wild");
        assertThat(copy.getCard().getPower()).isEqualTo(6);
        assertThat(copy.getCard().getToughness()).isEqualTo(6);
        assertThat(copy.getCard().hasType(CardType.CREATURE)).isTrue();
    }

    @Test
    @DisplayName("Copied creature retains Essence's static ability")
    void copiedCreatureRetainsStaticAbility() {
        harness.addToBattlefield(player1, new EssenceOfTheWild());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        // The copy should also have CreaturesEnterAsCopyOfSourceEffect
        assertThat(copy.getCard().getEffects(EffectSlot.STATIC))
                .anyMatch(e -> e instanceof CreaturesEnterAsCopyOfSourceEffect);
    }

    // ===== Does not affect opponent =====

    @Test
    @DisplayName("Does not affect opponent's creatures entering the battlefield")
    void doesNotAffectOpponentCreatures() {
        harness.addToBattlefield(player1, new EssenceOfTheWild());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        // Opponent's Grizzly Bears should enter normally
        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElse(null);
        assertThat(bears).isNotNull();
        assertThat(bears.getCard().getPower()).isEqualTo(2);
        assertThat(bears.getCard().getToughness()).isEqualTo(2);
    }

    // ===== Token replacement =====

    @Test
    @DisplayName("Token creatures also enter as copies of Essence")
    void tokenEntersAsCopyOfEssence() {
        harness.addToBattlefield(player1, new EssenceOfTheWild());
        harness.addToBattlefield(player1, new DoomedTraveler());

        // Use Shock to kill Doomed Traveler (1/1) without killing Essence (6/6)
        UUID travelerId = harness.getPermanentId(player1, "Doomed Traveler");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, travelerId);
        harness.passBothPriorities(); // Resolve Shock — Doomed Traveler dies

        // Death trigger should be on the stack
        assertThat(gd.stack).hasSize(1);

        // Resolve the death trigger — Spirit token enters
        harness.passBothPriorities();

        // The Spirit token should have entered as a copy of Essence
        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        Permanent tokenCopy = bf.stream()
                .filter(p -> p.getOriginalCard().isToken())
                .findFirst().orElse(null);
        assertThat(tokenCopy).isNotNull();
        assertThat(tokenCopy.getCard().getName()).isEqualTo("Essence of the Wild");
        assertThat(tokenCopy.getCard().getPower()).isEqualTo(6);
        assertThat(tokenCopy.getCard().getToughness()).isEqualTo(6);
    }

    // ===== ETB suppression =====

    @Test
    @DisplayName("Original creature's ETB abilities do not trigger (ruling: creatures enter as Essence, not themselves)")
    void originalCreatureETBDoesNotTrigger() {
        harness.addToBattlefield(player1, new EssenceOfTheWild());
        harness.setLife(player1, 20);

        // Angel of Mercy has ETB: gain 3 life
        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Angel entered as Essence — its "gain 3 life" ETB should NOT trigger
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);

        // Verify it entered as Essence
        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Angel of Mercy"))
                .findFirst().orElse(null);
        assertThat(copy).isNotNull();
        assertThat(copy.getCard().getName()).isEqualTo("Essence of the Wild");
    }

    // ===== Effect stops after Essence leaves =====

    @Test
    @DisplayName("Creatures enter normally after Essence leaves the battlefield")
    void effectStopsAfterEssenceLeaves() {
        harness.addToBattlefield(player1, new EssenceOfTheWild());

        // Remove Essence from the battlefield
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Grizzly Bears should enter as itself since Essence is gone
        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElse(null);
        assertThat(bears).isNotNull();
        assertThat(bears.getCard().getPower()).isEqualTo(2);
        assertThat(bears.getCard().getToughness()).isEqualTo(2);
    }
}
