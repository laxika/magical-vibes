package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DonalHeraldOfWings.class, AirElemental.class, GrizzlyBears.class})
class DonalHeraldOfWingsTest extends BaseCardTest {

    @Test
    @DisplayName("Copies an accepted nonlegendary flying creature spell as a 1/1 Spirit token")
    void copiesAcceptedFlyingCreatureSpellAsSpiritToken() {
        harness.addToBattlefield(player1, new DonalHeraldOfWings());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        resolveCastAndCopy();

        List<Permanent> elementals = findPermanents(player1, "Air Elemental");
        assertThat(elementals).hasSize(2);
        assertThat(elementals).anySatisfy(permanent -> {
            assertThat(permanent.getCard().isToken()).isTrue();
            assertThat(permanent.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
            assertThat(permanent.getCard().getPower()).isEqualTo(1);
            assertThat(permanent.getCard().getToughness()).isEqualTo(1);
        });
        assertThat(elementals).anySatisfy(permanent -> {
            assertThat(permanent.getCard().isToken()).isFalse();
            assertThat(permanent.getCard().getPower()).isEqualTo(4);
            assertThat(permanent.getCard().getToughness()).isEqualTo(4);
        });
    }

    @Test
    @DisplayName("Copies only one qualifying creature spell each turn")
    void copiesOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new DonalHeraldOfWings());
        harness.setHand(player1, List.of(new AirElemental(), new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 10);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveCastAndCopy();

        harness.castCreature(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isNull();
        resolveCastAndCopy();

        assertThat(findPermanents(player1, "Air Elemental")).hasSize(3);
        assertThat(findPermanents(player1, "Air Elemental").stream()
                .filter(permanent -> permanent.getCard().isToken())).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger for nonflying or legendary creature spells")
    void doesNotTriggerForNonflyingOrLegendaryCreatureSpells() {
        harness.addToBattlefield(player1, new DonalHeraldOfWings());
        harness.setHand(player1, List.of(new GrizzlyBears(), legendaryFlyingCreature()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.passBothPriorities();

        harness.castCreature(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())).isEmpty();
    }

    private void resolveCastAndCopy() {
        resolveAllTriggers();
    }

    private Card legendaryFlyingCreature() {
        Card card = new Card();
        card.setName("Legendary Flier");
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(CardColor.BLUE);
        card.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        card.setSubtypes(List.of(CardSubtype.BIRD));
        card.setKeywords(Set.of(Keyword.FLYING));
        card.setPower(2);
        card.setToughness(2);
        return card;
    }
}
