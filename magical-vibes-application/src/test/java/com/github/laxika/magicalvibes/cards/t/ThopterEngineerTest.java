package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThopterEngineerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates one 1/1 flying Thopter artifact creature token")
    void etbCreatesThopterToken() {
        harness.setHand(player1, List.of(new ThopterEngineer()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(2);

        Permanent token = battlefield.stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.THOPTER))
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Grants haste to your artifact creatures")
    void grantsHasteToOwnArtifactCreatures() {
        harness.addToBattlefield(player1, new ThopterEngineer());
        harness.addToBattlefield(player1, new Ornithopter());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Ornithopter"), Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant haste to a non-artifact creature, including itself")
    void noHasteForNonArtifactCreatures() {
        harness.addToBattlefield(player1, new ThopterEngineer());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Thopter Engineer"), Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant haste to an opponent's artifact creature")
    void noHasteForOpponent() {
        harness.addToBattlefield(player1, new ThopterEngineer());
        harness.addToBattlefield(player2, new Ornithopter());

        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Ornithopter"), Keyword.HASTE)).isFalse();
    }
}
