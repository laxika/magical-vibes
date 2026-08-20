package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HofriGhostforgeTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts own Spirits and grants them trample and haste")
    void boostsOwnSpirits() {
        harness.addToBattlefield(player1, new HofriGhostforge());
        harness.addToBattlefield(player1, spiritToken("Own Spirit", 1, 1));
        harness.addToBattlefield(player1, creatureToken("Own Bear", 1, 1, CardSubtype.BEAR));
        harness.addToBattlefield(player2, spiritToken("Opponent Spirit", 1, 1));

        Permanent ownSpirit = findPermanent(player1, "Own Spirit");
        Permanent ownBear = findPermanent(player1, "Own Bear");
        Permanent opponentSpirit = findPermanent(player2, "Opponent Spirit");

        assertThat(gqs.getEffectivePower(gd, ownSpirit)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownSpirit)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownSpirit, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownSpirit, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentSpirit)).isEqualTo(1);
    }

    @Test
    @DisplayName("Exiles an own nontoken creature and creates a linked Spirit copy")
    void createsLinkedSpiritCopy() {
        harness.addToBattlefield(player1, new HofriGhostforge());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        destroyWithShock(bears);
        harness.passBothPriorities();

        Permanent spiritCopy = findPermanent(player1, "Grizzly Bears");
        assertThat(spiritCopy.getCard().isToken()).isTrue();
        assertThat(spiritCopy.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(spiritCopy.getCard().getSubtypes()).contains(CardSubtype.BEAR);
        assertThat(spiritCopy.getCard().getPower()).isEqualTo(2);
        assertThat(spiritCopy.getCard().getToughness()).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, spiritCopy)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, spiritCopy)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, spiritCopy, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, spiritCopy, Keyword.HASTE)).isTrue();
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getName().equals("Grizzly Bears"));

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, spiritCopy));
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).noneMatch(exiled -> exiled.card().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Does not copy a token or an opponent's creature")
    void ignoresTokensAndOpponents() {
        harness.addToBattlefield(player1, new HofriGhostforge());
        Card ownToken = spiritToken("Own Spirit", 1, 1);
        harness.addToBattlefield(player1, ownToken);
        harness.addToBattlefield(player2, new GrizzlyBears());

        destroyWithShock(findPermanent(player1, "Own Spirit"));
        assertThat(findPermanents(player1, "Own Spirit")).isEmpty();

        destroyWithShock(findPermanent(player2, "Grizzly Bears"));
        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
    }

    private void destroyWithShock(Permanent target) {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
    }

    private Card spiritToken(String name, int power, int toughness) {
        return creatureToken(name, power, toughness, CardSubtype.SPIRIT);
    }

    private Card creatureToken(String name, int power, int toughness, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setToken(true);
        card.setColor(CardColor.WHITE);
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(List.of(subtype));
        return card;
    }
}
