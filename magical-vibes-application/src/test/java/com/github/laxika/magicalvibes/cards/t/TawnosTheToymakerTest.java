package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TawnosTheToymakerTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting a Beast spell creates an artifact token copy")
    void beastSpellCreatesArtifactTokenCopy() {
        harness.addToBattlefield(player1, new TawnosTheToymaker());
        Card beast = creature("Test Beast", CardSubtype.BEAST);
        harness.setHand(player1, List.of(beast));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> beasts = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Test Beast"))
                .toList();
        assertThat(beasts).hasSize(2);
        assertThat(beasts).anySatisfy(permanent -> {
            assertThat(permanent.getCard().isToken()).isTrue();
            assertThat(permanent.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
            assertThat(gqs.isArtifact(gd, permanent)).isTrue();
        });
        assertThat(beasts).anySatisfy(permanent -> assertThat(permanent.getCard().isToken()).isFalse());
    }

    @Test
    @DisplayName("A Bird spell also triggers Tawnos")
    void birdSpellAlsoTriggers() {
        harness.addToBattlefield(player1, new TawnosTheToymaker());
        harness.setHand(player1, List.of(creature("Test Bird", CardSubtype.BIRD)));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Test Bird"))
                .filter(permanent -> permanent.getCard().isToken()))
                .hasSize(1);
    }

    @Test
    @DisplayName("Declining the copy leaves only the original creature")
    void declineCopy() {
        harness.addToBattlefield(player1, new TawnosTheToymaker());
        harness.setHand(player1, List.of(creature("Test Beast", CardSubtype.BEAST)));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Test Beast")))
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.getCard().isToken()).isFalse());
    }

    @Test
    @DisplayName("A non-Beast, non-Bird creature does not trigger Tawnos")
    void otherCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new TawnosTheToymaker());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Grizzly Bears")))
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.getCard().isToken()).isFalse());
    }

    private Card creature(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.GREEN);
        card.setSubtypes(List.of(subtype));
        card.setPower(2);
        card.setToughness(2);
        return card;
    }
}
