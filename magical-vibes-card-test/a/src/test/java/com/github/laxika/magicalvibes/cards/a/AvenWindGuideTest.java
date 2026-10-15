package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvenWindGuide.class, Colossapede.class})
class AvenWindGuideTest extends BaseCardTest {

    private static Card createCreature(String name, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(1);
        card.setToughness(1);
        return card;
    }

    private static Card tokenCreature(String name, CardColor color) {
        Card card = createCreature(name, color);
        card.setToken(true);
        return card;
    }

    @Test
    @DisplayName("Own creature token gains flying and vigilance")
    void ownTokenGainsFlyingAndVigilance() {
        harness.addToBattlefield(player1, new AvenWindGuide());
        harness.addToBattlefield(player1, tokenCreature("Soldier Token", CardColor.WHITE));

        Permanent token = findPermanent(player1, "Soldier Token");
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Own nontoken creature does not gain flying or vigilance")
    void nontokenDoesNotGainKeywords() {
        harness.addToBattlefield(player1, new AvenWindGuide());
        harness.addToBattlefield(player1, createCreature("Grizzly Bears", CardColor.GREEN));

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Opponent's creature token does not gain flying or vigilance")
    void opponentTokenDoesNotGainKeywords() {
        harness.addToBattlefield(player1, new AvenWindGuide());
        harness.addToBattlefield(player2, tokenCreature("Goblin Token", CardColor.RED));

        Permanent token = findPermanent(player2, "Goblin Token");
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Granted keywords are lost when Aven Wind Guide leaves the battlefield")
    void keywordsLostWhenGuideLeaves() {
        harness.addToBattlefield(player1, new AvenWindGuide());
        harness.addToBattlefield(player1, tokenCreature("Soldier Token", CardColor.WHITE));

        Permanent token = findPermanent(player1, "Soldier Token");
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Aven Wind Guide"));

        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isFalse();
    }

    private void setUpEmbalm() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new AvenWindGuide()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Embalm exiles the source card from the graveyard as a cost")
    void embalmExilesSourceAsCost() {
        setUpEmbalm();

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Aven Wind Guide");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Aven Wind Guide"));
    }

    @Test
    @DisplayName("Embalm creates a white Zombie Bird Warrior token copy with no mana cost")
    void embalmCreatesWhiteZombieTokenCopy() {
        setUpEmbalm();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities(); // resolve the Embalm ability

        Permanent token = findPermanent(player1, "Aven Wind Guide");

        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE, CardSubtype.BIRD, CardSubtype.WARRIOR);
        assertThat(token.getCard().getManaCost()).isEmpty();
    }

    @Test
    @DisplayName("Embalm can only be activated at sorcery speed")
    void embalmOnlyAtSorcerySpeed() {
        harness.setGraveyard(player1, List.of(new AvenWindGuide()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        // Opponent's turn — not sorcery speed for player1.
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Assertions.assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Aven Wind Guide");
    }

    @Test
    @DisplayName("Embalmed Wind Guide grants flying and vigilance to other creature tokens")
    void embalmedGuideRetainsTokenGrant() {
        setUpEmbalm();
        Card insectToken = new Colossapede();
        insectToken.setToken(true);
        harness.addToBattlefield(player1, insectToken);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent insect = findPermanent(player1, "Colossapede");
        Permanent guide = findPermanent(player1, "Aven Wind Guide");
        assertThat(gqs.hasKeyword(gd, insect, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, insect, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, guide, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, guide, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Noncreature tokens do not gain flying or vigilance")
    void noncreatureTokenDoesNotGainKeywords() {
        harness.addToBattlefield(player1, new AvenWindGuide());
        Card token = new Card();
        token.setName("Treasure");
        token.setType(CardType.ARTIFACT);
        token.setToken(true);
        harness.addToBattlefield(player1, token);

        Permanent treasure = findPermanent(player1, "Treasure");
        assertThat(gqs.hasKeyword(gd, treasure, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, treasure, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Embalm cannot be activated during combat on its controller's turn")
    void embalmCannotBeActivatedDuringCombat() {
        setUpEmbalm();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        Assertions.assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Aven Wind Guide");
        harness.assertNotOnBattlefield(player1, "Aven Wind Guide");
    }

    @Test
    @DisplayName("Embalm cannot be activated in response to another embalm ability")
    void embalmRequiresEmptyStack() {
        setUpEmbalm();
        harness.setGraveyard(player1, List.of(new AvenWindGuide(), new AvenWindGuide()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateGraveyardAbility(player1, 0);

        Assertions.assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Aven Wind Guide");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Aven Wind Guide")).isEqualTo(1);
    }

    @Test
    @DisplayName("Embalm requires both white and blue mana")
    void embalmCannotUseOnlyWhiteAndColorlessMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new AvenWindGuide()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        Assertions.assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Aven Wind Guide");
        harness.assertNotOnBattlefield(player1, "Aven Wind Guide");
    }
}
