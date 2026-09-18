package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EzurisPredation.class, HillGiant.class, GrizzlyBears.class, LlanowarElves.class})
class EzurisPredationTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Phyrexian Beast for each opposing creature and has them fight different creatures")
    void createsBeastsAndHasThemFightDifferentCreatures() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castEzurisPredation();

        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        List<Permanent> beasts = findPermanents(player1, "Phyrexian Beast");
        assertThat(beasts).hasSize(2);
        assertThat(beasts).allSatisfy(beast -> {
            assertThat(beast.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(beast.getCard().getSubtypes())
                    .containsExactly(CardSubtype.PHYREXIAN, CardSubtype.BEAST);
            assertThat(beast.getEffectivePower()).isEqualTo(4);
            assertThat(beast.getEffectiveToughness()).isEqualTo(4);
        });
    }

    @Test
    @DisplayName("Does not count or affect creatures controlled by the spell's controller")
    void onlyCountsOpposingCreatures() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new LlanowarElves());

        castEzurisPredation();

        assertThat(findPermanents(player1, "Phyrexian Beast")).hasSize(1);
        harness.assertOnBattlefield(player1, "Hill Giant");
        assertThat(findPermanent(player1, "Hill Giant").getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Creates no tokens when opponents control no creatures")
    void createsNoTokensWithoutOpposingCreatures() {
        castEzurisPredation();

        assertThat(findPermanents(player1, "Phyrexian Beast")).isEmpty();
    }

    private void castEzurisPredation() {
        harness.setHand(player1, List.of(new EzurisPredation()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
