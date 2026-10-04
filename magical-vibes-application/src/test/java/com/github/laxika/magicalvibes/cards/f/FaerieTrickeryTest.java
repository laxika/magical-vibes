package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.g.GoldmeadowDodger;
import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FaerieTrickery.class, FaerieHarbinger.class, GoldmeadowDodger.class,
        AvianChangeling.class, NamelessInversion.class})
class FaerieTrickeryTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack targeting a non-Faerie spell")
    void castingTargetsNonFaerieSpell() {
        GoldmeadowDodger dodger = new GoldmeadowDodger();
        harness.castFromHand(player1, dodger, "{W}");

        harness.setHand(player2, List.of(new FaerieTrickery()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, dodger.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(2);
        StackEntry entry = gd.stack.getLast();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(dodger.getId());
    }

    @Test
    @DisplayName("Cannot target a Faerie spell")
    void cannotTargetFaerieSpell() {
        FaerieHarbinger harbinger = new FaerieHarbinger();
        harness.castFromHand(player1, harbinger, "{3}{U}");

        harness.setHand(player2, List.of(new FaerieTrickery()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, harbinger.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a Changeling creature spell")
    void cannotTargetChangelingSpell() {
        AvianChangeling changeling = new AvianChangeling();
        harness.castFromHand(player1, changeling, "{2}{W}");

        harness.setHand(player2, List.of(new FaerieTrickery()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, changeling.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a kindred instant with changeling")
    void cannotTargetKindredChangelingSpell() {
        harness.addToBattlefield(player1, new GoldmeadowDodger());
        NamelessInversion inversion = new NamelessInversion();
        harness.setHand(player1, List.of(inversion));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Goldmeadow Dodger"));

        harness.setHand(player2, List.of(new FaerieTrickery()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, inversion.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target another Faerie Trickery, a noncreature Faerie spell")
    void cannotTargetKindredFaerieSpell() {
        GoldmeadowDodger dodger = new GoldmeadowDodger();
        harness.castFromHand(player1, dodger, "{W}");

        FaerieTrickery opposingTrickery = new FaerieTrickery();
        harness.setHand(player2, List.of(opposingTrickery));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, dodger.getId());

        harness.setHand(player1, List.of(new FaerieTrickery()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opposingTrickery.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters a non-Faerie spell and exiles it instead of the graveyard")
    void countersAndExilesNonFaerieSpell() {
        GoldmeadowDodger dodger = new GoldmeadowDodger();
        harness.castFromHand(player1, dodger, "{W}");

        harness.setHand(player2, List.of(new FaerieTrickery()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, dodger.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Goldmeadow Dodger"));
        harness.assertNotInGraveyard(player1, "Goldmeadow Dodger");
        harness.assertNotOnBattlefield(player1, "Goldmeadow Dodger");
    }

    @Test
    @DisplayName("Faerie Trickery goes to caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        GoldmeadowDodger dodger = new GoldmeadowDodger();
        harness.castFromHand(player1, dodger, "{W}");

        harness.setHand(player2, List.of(new FaerieTrickery()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, dodger.getId());

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player2, "Faerie Trickery");
        assertThat(gd.stack).isEmpty();
    }
}
