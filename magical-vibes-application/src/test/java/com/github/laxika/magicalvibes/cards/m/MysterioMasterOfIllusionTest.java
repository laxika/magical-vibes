package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DocOckSinisterScientist;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MysterioMasterOfIllusion.class, DocOckSinisterScientist.class})
class MysterioMasterOfIllusionTest extends BaseCardTest {

    @Test
    @DisplayName("Creates an Illusion Villain for each nontoken Villain you control")
    void createsTokensForNontokenVillainsYouControl() {
        harness.addToBattlefield(player1, new DocOckSinisterScientist());
        harness.addToBattlefield(player2, new DocOckSinisterScientist());
        harness.setHand(player1, List.of(new MysterioMasterOfIllusion()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Illusion Villain").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getEffectivePower()).isEqualTo(3);
            assertThat(token.getEffectiveToughness()).isEqualTo(3);
        });
        assertThat(findPermanents(player2, "Illusion Villain")).isEmpty();
    }

    @Test
    @DisplayName("Exiles the tokens it created when it leaves the battlefield")
    void exilesCreatedTokensWhenItLeaves() {
        harness.setHand(player1, List.of(new MysterioMasterOfIllusion()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent mysterio = findPermanent(player1, "Mysterio, Master of Illusion");

        List<Permanent> tokens = findPermanents(player1, "Illusion Villain");
        assertThat(tokens).hasSize(1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, mysterio));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Illusion Villain")).isEmpty();
    }

    @Test
    @DisplayName("A created token leaving does not affect Mysterio")
    void createdTokenLeavingDoesNotAffectMysterio() {
        harness.setHand(player1, List.of(new MysterioMasterOfIllusion()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Illusion Villain").getFirst();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, token));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Mysterio, Master of Illusion")).hasSize(1);
    }
}
