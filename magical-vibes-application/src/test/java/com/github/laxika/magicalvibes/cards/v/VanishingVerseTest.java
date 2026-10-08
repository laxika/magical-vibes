package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BeamingDefiance;
import com.github.laxika.magicalvibes.cards.c.CombatProfessor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VanishingVerse.class, CombatProfessor.class, BeamingDefiance.class})
class VanishingVerseTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a monocolored permanent")
    void exilesMonocoloredPermanent() {
        Permanent target = addPermanent(player2, "Monocolored Permanent", CardColor.GREEN);

        prepare();
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
        harness.assertNotOnBattlefield(player2, "Monocolored Permanent");
        harness.assertNotInGraveyard(player2, "Monocolored Permanent");
    }

    @Test
    @DisplayName("Cannot target a multicolored permanent")
    void cannotTargetMulticoloredPermanent() {
        Permanent target = addPermanent(player2, "Multicolored Permanent", CardColor.GREEN, CardColor.WHITE);

        prepare();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a colorless permanent")
    void cannotTargetColorlessPermanent() {
        Permanent target = addPermanent(player2, "Colorless Permanent");

        prepare();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exiles an opponent's monocolored creature")
    void exilesOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CombatProfessor());
        prepare();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Combat Professor");
        harness.assertNotInGraveyard(player2, "Combat Professor");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        harness.assertInGraveyard(player1, "Vanishing Verse");
    }

    @Test
    @DisplayName("Can exile its controller's own monocolored creature")
    void exilesOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CombatProfessor());
        prepare();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Combat Professor");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("Does not exile a creature that gains hexproof in response")
    void hexproofInResponseMakesTargetIllegal() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CombatProfessor());
        prepare();
        harness.castInstant(player1, 0, target.getId());
        harness.setHand(player2, List.of(new BeamingDefiance()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Combat Professor");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Vanishing Verse");
        assertThat(gd.stack).isEmpty();
    }

    private void prepare() {
        harness.setHand(player1, List.of(new VanishingVerse()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    private Permanent addPermanent(Player player, String name, CardColor... colors) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setColors(List.of(colors));
        return harness.addToBattlefieldAndReturn(player, card);
    }
}
