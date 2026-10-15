package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.l.LiquimetalCoating;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThopterEngineer.class, Ornithopter.class, GrizzlyBears.class, LiquimetalCoating.class})
class ThopterEngineerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates one 1/1 flying Thopter artifact creature token")
    void etbCreatesThopterToken() {
        harness.setHand(player1, List.of(new ThopterEngineer()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(2);

        Permanent token = battlefield.stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.THOPTER))
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(token.getCard().getColors()).isEmpty();
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

    @Test
    @DisplayName("Artifact creatures lose granted haste when the Engineer leaves")
    void hasteEndsWhenEngineerLeaves() {
        harness.setHand(player1, List.of(new ThopterEngineer()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Thopter");
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();

        harness.getPermanentRemovalService().removePermanentToGraveyard(
                gd, findPermanent(player1, "Thopter Engineer"));

        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("ETB still creates a token after the Engineer leaves, without haste")
    void triggerResolvesAfterEngineerLeaves() {
        harness.setHand(player1, List.of(new ThopterEngineer()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.getPermanentRemovalService().removePermanentToGraveyard(
                gd, findPermanent(player1, "Thopter Engineer"));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent token = findPermanent(player1, "Thopter");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Engineer grants haste to itself when it becomes an artifact")
    void artifactEngineerHasHaste() {
        harness.addToBattlefield(player1, new LiquimetalCoating());
        harness.addToBattlefield(player1, new ThopterEngineer());
        Permanent engineer = findPermanent(player1, "Thopter Engineer");
        assertThat(gqs.hasKeyword(gd, engineer, Keyword.HASTE)).isFalse();

        harness.activateAbility(player1, 0, null, engineer.getId());
        resolveAllTriggers();

        assertThat(gqs.isArtifact(gd, engineer)).isTrue();
        assertThat(gqs.hasKeyword(gd, engineer, Keyword.HASTE)).isTrue();
    }
}
