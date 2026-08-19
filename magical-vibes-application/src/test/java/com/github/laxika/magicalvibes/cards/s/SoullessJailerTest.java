package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.o.ObzedatsAid;
import com.github.laxika.magicalvibes.cards.r.RiseFromTheGrave;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoullessJailerTest extends BaseCardTest {

    @Test
    @DisplayName("Permanent cards cannot enter the battlefield from graveyards")
    void blocksPermanentCardsFromGraveyards() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new SoullessJailer());
        harness.setHand(player1, List.of(new RiseFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Noncreature permanent cards cannot enter the battlefield from graveyards")
    void blocksNoncreaturePermanentCardsFromGraveyards() {
        IcyManipulator artifact = new IcyManipulator();
        harness.setGraveyard(player1, List.of(artifact));
        harness.addToBattlefield(player1, new SoullessJailer());
        harness.setHand(player1, List.of(new ObzedatsAid()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Icy Manipulator");
        harness.assertInGraveyard(player1, "Icy Manipulator");
    }

    @Test
    @DisplayName("Noncreature spells cannot be cast from graveyards")
    void blocksNoncreatureSpellsFromGraveyards() {
        harness.addToBattlefield(player1, new SoullessJailer());
        harness.setGraveyard(player1, List.of(new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Noncreature spells cannot be cast from exile")
    void blocksNoncreatureSpellsFromExile() {
        ThinkTwice thinkTwice = new ThinkTwice();
        harness.addToBattlefield(player1, new SoullessJailer());
        harness.setExile(player1, List.of(thinkTwice));
        gd.exilePlayPermissions.put(thinkTwice.getId(), player1.getId());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, thinkTwice.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Noncreature spells can still be cast from hand")
    void allowsNoncreatureSpellsFromHand() {
        harness.addToBattlefield(player1, new SoullessJailer());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
