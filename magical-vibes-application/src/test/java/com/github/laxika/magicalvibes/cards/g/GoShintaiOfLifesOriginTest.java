package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(GoShintaiOfLifesOrigin.class)
class GoShintaiOfLifesOriginTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a colorless Shrine enchantment creature token when it enters")
    void createsShrineTokenOnEntry() {
        harness.enterBattlefieldAndReturn(player1, new GoShintaiOfLifesOrigin());
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Shrine").getFirst();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().hasType(CardType.ENCHANTMENT)).isTrue();
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SHRINE);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates one token for another nontoken Shrine but not for a Shrine token")
    void triggersForAnotherNontokenShrineOnly() {
        harness.addToBattlefield(player1, new GoShintaiOfLifesOrigin());

        Card shrine = shrine(false);
        harness.enterBattlefieldAndReturn(player1, shrine);
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player1, shrine(true));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Shrine")).hasSize(1);
    }

    @Test
    @DisplayName("Returns a target enchantment from the controller's graveyard")
    void returnsTargetEnchantment() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GoShintaiOfLifesOrigin());
        source.setSummoningSick(false);
        Card enchantment = enchantment("Returned Enchantment");
        harness.setGraveyard(player1, List.of(enchantment));
        addActivationMana();

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(enchantment.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getCard)
                .map(Card::getId))
                .contains(enchantment.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(enchantment);
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Rejects a non-enchantment graveyard target")
    void rejectsNonEnchantmentTarget() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GoShintaiOfLifesOrigin());
        source.setSummoningSick(false);
        Card creature = new Card();
        creature.setName("Not an Enchantment");
        creature.setType(CardType.CREATURE);
        harness.setGraveyard(player1, List.of(creature));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private Card shrine(boolean token) {
        Card card = enchantment("Test Shrine");
        card.setSubtypes(List.of(CardSubtype.SHRINE));
        card.setToken(token);
        return card;
    }

    private Card enchantment(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ENCHANTMENT);
        return card;
    }
}
