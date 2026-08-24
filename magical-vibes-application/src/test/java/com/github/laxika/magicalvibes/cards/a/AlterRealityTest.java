package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.PaladinEnVec;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AlterReality.class, PaladinEnVec.class})
class AlterRealityTest extends BaseCardTest {

    @Test
    @DisplayName("Changes a color word on a target permanent indefinitely")
    void changesColorWordOnTargetPermanent() {
        harness.addToBattlefield(player2, new PaladinEnVec());
        harness.setHand(player1, List.of(new AlterReality()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Paladin en-Vec");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GREEN");

        Permanent target = findPermanent(player2, "Paladin en-Vec");
        assertThat(target.getTextReplacements()).containsExactly(new TextReplacement("red", "green"));
    }

    @Test
    @DisplayName("Carries a text change from a target spell onto the permanent it becomes")
    void changesColorWordOnTargetSpellCarriesToPermanent() {
        harness.setHand(player1, List.of(new AlterReality(), new PaladinEnVec()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 1);
        UUID paladinSpellId = gd.stack.getFirst().getCard().getId();
        harness.castInstant(player1, 0, paladinSpellId);
        harness.passBothPriorities();

        harness.handleListChoice(player1, "RED");
        harness.handleListChoice(player1, "GREEN");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Paladin en-Vec").getTextReplacements())
                .containsExactly(new TextReplacement("red", "green"));
    }

    @Test
    @DisplayName("Flashback changes a permanent's text and exiles Alter Reality")
    void flashbackChangesPermanentTextAndExilesSpell() {
        harness.addToBattlefield(player2, new PaladinEnVec());
        harness.setGraveyard(player1, List.of(new AlterReality()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player2, "Paladin en-Vec");
        harness.castFlashback(player1, 0, targetId);
        harness.passBothPriorities();

        harness.handleListChoice(player1, "BLACK");
        harness.handleListChoice(player1, "WHITE");

        assertThat(findPermanent(player2, "Paladin en-Vec").getTextReplacements())
                .containsExactly(new TextReplacement("black", "white"));
        harness.assertNotInGraveyard(player1, "Alter Reality");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Alter Reality"));
    }
}
