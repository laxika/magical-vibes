package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnimistsAwakeningTest extends BaseCardTest {

    @Test
    @DisplayName("Revealed lands enter tapped and the rest go to the bottom of the library")
    void landsEnterTappedRestOnBottom() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        Card mountain = new Mountain();

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(forest, bears, mountain));

        harness.setHand(player1, List.of(new AnimistsAwakening()));
        harness.addMana(player1, ManaColor.GREEN, 4); // {3}{G} with X=3

        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Mountain").isTapped()).isTrue();

        // The non-land goes to the bottom of the library, not the graveyard.
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .doesNotContain("Grizzly Bears");
    }

    @Test
    @DisplayName("Spell mastery untaps the lands put onto the battlefield")
    void spellMasteryUntapsLands() {
        Card forest = new Forest();

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(forest);

        harness.setGraveyard(player1, List.of(new Blaze(), new Blaze()));
        harness.setHand(player1, List.of(new AnimistsAwakening()));
        harness.addMana(player1, ManaColor.GREEN, 3); // X=2

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(findPermanent(player1, "Forest").isTapped()).isFalse();
    }

    @Test
    @DisplayName("One instant or sorcery in the graveyard is not enough for spell mastery")
    void oneInstantOrSorceryDoesNotEnableSpellMastery() {
        Card forest = new Forest();

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(forest);

        harness.setGraveyard(player1, List.of(new Blaze()));
        harness.setHand(player1, List.of(new AnimistsAwakening()));
        harness.addMana(player1, ManaColor.GREEN, 3); // X=2

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting with X=0 reveals nothing and puts no lands onto the battlefield")
    void xZeroDoesNothing() {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());

        harness.setHand(player1, List.of(new AnimistsAwakening()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
