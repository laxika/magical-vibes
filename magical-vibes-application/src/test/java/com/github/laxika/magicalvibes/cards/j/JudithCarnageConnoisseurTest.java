package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JudithCarnageConnoisseur.class, HillGiant.class, Shock.class})
class JudithCarnageConnoisseurTest extends BaseCardTest {

    private static final String KEYWORD_MODE = "That spell gains deathtouch and lifelink";
    private static final String IMP_MODE =
            "Create a 2/2 red Imp creature token with \"When this token dies, it deals 2 damage to each opponent.\"";

    @Test
    @DisplayName("Judith can give the cast spell deathtouch and lifelink")
    void givesCastSpellDeathtouchAndLifelink() {
        harness.addToBattlefield(player1, new JudithCarnageConnoisseur());
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, hillGiant.getId());
        chooseJudithMode(KEYWORD_MODE);

        assertThat(findPermanents(player2, "Hill Giant")).isEmpty();
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Judith can create an Imp whose death damages each opponent")
    void createsImpWithDeathTrigger() {
        harness.addToBattlefield(player1, new JudithCarnageConnoisseur());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        chooseJudithMode(IMP_MODE);

        Permanent imp = findPermanents(player1, "Imp").getFirst();
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, imp.getId());
        chooseJudithMode(KEYWORD_MODE);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Imp")).isEmpty();
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 16);
    }

    private void chooseJudithMode(String mode) {
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
