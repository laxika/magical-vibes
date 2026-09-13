package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.w.Whetstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Annul.class, Whetstone.class, AngelicChorus.class, CoralMerfolk.class})
class AnnulTest extends BaseCardTest {

    @Test
    @DisplayName("Counters an artifact spell")
    void countersArtifactSpell() {
        Whetstone whetstone = new Whetstone();
        harness.castFromHand(player1, whetstone, "{3}");

        harness.setHand(player2, List.of(new Annul()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, whetstone.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Whetstone");
        harness.assertNotOnBattlefield(player1, "Whetstone");
        harness.assertInGraveyard(player2, "Annul");
    }

    @Test
    @DisplayName("Counters an enchantment spell")
    void countersEnchantmentSpell() {
        AngelicChorus chorus = new AngelicChorus();
        harness.castFromHand(player1, chorus, "{3}{W}{W}");

        harness.setHand(player2, List.of(new Annul()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, chorus.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Angelic Chorus");
        harness.assertNotOnBattlefield(player1, "Angelic Chorus");
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        CoralMerfolk merfolk = new CoralMerfolk();
        harness.castFromHand(player1, merfolk, "{1}{U}");

        harness.setHand(player2, List.of(new Annul()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, merfolk.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an activated ability from an artifact")
    void cannotTargetActivatedAbility() {
        Whetstone whetstone = new Whetstone();
        harness.addToBattlefield(player1, whetstone);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.setHand(player2, List.of(new Annul()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, whetstone.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
    }
}
