package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.ElvishBerserker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LysAlanaScarblade;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SkemfarAvengerTest extends BaseCardTest {

    private void killWithShock(Player targetController, String targetName) {
        UUID targetId = harness.getPermanentId(targetController, targetName);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Permanent addElfToken() {
        Card tokenCard = new Card();
        tokenCard.setName("Elf Token");
        tokenCard.setType(CardType.CREATURE);
        tokenCard.setManaCost("");
        tokenCard.setToken(true);
        tokenCard.setColor(CardColor.GREEN);
        tokenCard.setPower(1);
        tokenCard.setToughness(1);
        tokenCard.setSubtypes(List.of(CardSubtype.ELF));
        Permanent token = new Permanent(tokenCard);
        gd.playerBattlefields.get(player1.getId()).add(token);
        return token;
    }

    @Test
    @DisplayName("A nontoken Elf dying draws a card and causes 1 life loss")
    void nontokenElfDyingTriggers() {
        harness.addToBattlefield(player1, new SkemfarAvenger());
        harness.addToBattlefield(player1, new LysAlanaScarblade());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        killWithShock(player1, "Lys Alana Scarblade");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("A nontoken Berserker dying triggers")
    void nontokenBerserkerDyingTriggers() {
        harness.addToBattlefield(player1, new SkemfarAvenger());
        harness.addToBattlefield(player1, new ElvishBerserker());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        killWithShock(player1, "Elvish Berserker");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("A nontoken creature without Elf or Berserker does not trigger")
    void unrelatedCreatureDyingDoesNotTrigger() {
        harness.addToBattlefield(player1, new SkemfarAvenger());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        killWithShock(player1, "Grizzly Bears");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An Elf token dying does not trigger")
    void tokenElfDyingDoesNotTrigger() {
        harness.addToBattlefield(player1, new SkemfarAvenger());
        addElfToken();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        killWithShock(player1, "Elf Token");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
    }
}
