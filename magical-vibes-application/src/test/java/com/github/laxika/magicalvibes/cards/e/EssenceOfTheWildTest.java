package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.v.VillageBellRinger;
import com.github.laxika.magicalvibes.cards.d.DoomedTraveler;
import com.github.laxika.magicalvibes.cards.d.DarkthicketWolf;
import com.github.laxika.magicalvibes.cards.b.BrimstoneVolley;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.r.RustedSentinel;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EssenceOfTheWild.class, DarkthicketWolf.class, DoomedTraveler.class, BrimstoneVolley.class, VillageBellRinger.class,
        RustedSentinel.class, TurnToFrog.class})
class EssenceOfTheWildTest extends BaseCardTest {

    @Test
    @DisplayName("Creature cast while Essence is on battlefield enters as a copy of Essence")
    void creatureEntersAsCopyOfEssence() {
        harness.addToBattlefield(player1, new EssenceOfTheWild());
        harness.setHand(player1, List.of(new DarkthicketWolf()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        assertThat(bf).hasSize(2);

        Permanent copy = bf.stream()
                .filter(p -> p.getOriginalCard().getName().equals("Darkthicket Wolf"))
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
        harness.setHand(player1, List.of(new DarkthicketWolf()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Darkthicket Wolf"))
                .findFirst().orElseThrow();

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p != copy);
        DarkthicketWolf nextCreature = new DarkthicketWolf();
        Permanent nextCopy = harness.enterBattlefieldAndReturn(player1, nextCreature);
        assertThat(nextCopy.getOriginalCard()).isSameAs(nextCreature);
        assertThat(nextCopy.getCard().getName()).isEqualTo("Essence of the Wild");
    }

    @Test
    @DisplayName("Does not affect opponent's creatures entering the battlefield")
    void doesNotAffectOpponentCreatures() {
        harness.addToBattlefield(player1, new EssenceOfTheWild());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new DarkthicketWolf()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Darkthicket Wolf"))
                .findFirst().orElse(null);
        assertThat(bears).isNotNull();
        assertThat(bears.getCard()).isSameAs(bears.getOriginalCard());
    }

    @Test
    @DisplayName("Token creatures also enter as copies of Essence")
    void tokenEntersAsCopyOfEssence() {
        harness.addToBattlefield(player1, new EssenceOfTheWild());
        harness.addToBattlefield(player1, new DoomedTraveler());

        UUID travelerId = harness.getPermanentId(player1, "Doomed Traveler");
        harness.setHand(player2, List.of(new BrimstoneVolley()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castInstant(player2, 0, travelerId);
        harness.passBothPriorities(); // Resolve BrimstoneVolley — Doomed Traveler dies

        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        Permanent tokenCopy = bf.stream()
                .filter(p -> p.getOriginalCard().isToken())
                .findFirst().orElse(null);
        assertThat(tokenCopy).isNotNull();
        assertThat(tokenCopy.getCard().getName()).isEqualTo("Essence of the Wild");
        assertThat(tokenCopy.getCard().getPower()).isEqualTo(6);
        assertThat(tokenCopy.getCard().getToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Original creature's ETB abilities do not trigger (ruling: creatures enter as Essence, not themselves)")
    void originalCreatureETBDoesNotTrigger() {
        Permanent essence = harness.addToBattlefieldAndReturn(player1, new EssenceOfTheWild());
        essence.tap();

        harness.setHand(player1, List.of(new VillageBellRinger()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(essence.isTapped()).isTrue();

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Village Bell-Ringer"))
                .findFirst().orElse(null);
        assertThat(copy).isNotNull();
        assertThat(copy.getCard().getName()).isEqualTo("Essence of the Wild");
    }

    @Test
    @DisplayName("Creatures enter normally after Essence leaves the battlefield")
    void effectStopsAfterEssenceLeaves() {
        harness.addToBattlefield(player1, new EssenceOfTheWild());

        gd.playerBattlefields.get(player1.getId()).clear();

        harness.setHand(player1, List.of(new DarkthicketWolf()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Darkthicket Wolf"))
                .findFirst().orElse(null);
        assertThat(bears).isNotNull();
        assertThat(bears.getCard()).isSameAs(bears.getOriginalCard());
    }

    @Test
    @DisplayName("Essence with no abilities does not replace creature entry")
    void essenceWithNoAbilitiesDoesNotReplaceEntry() {
        Permanent essence = harness.addToBattlefieldAndReturn(player1, new EssenceOfTheWild());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, essence.getId());
        harness.passBothPriorities();

        DarkthicketWolf creature = new DarkthicketWolf();
        Permanent entering = harness.enterBattlefieldAndReturn(player1, creature);

        assertThat(entering.getCard()).isSameAs(creature);
    }

    @Test
    @DisplayName("The entering creature loses its own enters-tapped ability")
    void originalEntersTappedAbilityDoesNotApply() {
        harness.addToBattlefield(player1, new EssenceOfTheWild());
        harness.setHand(player1, List.of(new RustedSentinel()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard() instanceof RustedSentinel)
                .findFirst().orElseThrow();
        assertThat(copy.getCard().getName()).isEqualTo("Essence of the Wild");
        assertThat(copy.isTapped()).isFalse();
    }
}
