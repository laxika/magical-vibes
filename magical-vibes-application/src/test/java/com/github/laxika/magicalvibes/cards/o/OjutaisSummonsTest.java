package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OjutaisSummons.class})
class OjutaisSummonsTest extends BaseCardTest {

    @Test
    void createsA2x2BlueDjinnMonkWithFlying() {
        OjutaisSummons card = new OjutaisSummons();
        harness.setHand(player1, List.of(card));
        addOjutaisSummonsMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Permanent> tokens = djinnMonkTokens(player1);
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.DJINN, CardSubtype.MONK);
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    void reboundOffersAnotherFreeCastAtNextUpkeep() {
        OjutaisSummons card = new OjutaisSummons();
        harness.setHand(player1, List.of(card));
        addOjutaisSummonsMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(djinnMonkTokens(player1)).hasSize(1);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(djinnMonkTokens(player1)).hasSize(2);
        assertThat(gd.findExiledCard(card.getId())).isNull();
        harness.assertInGraveyard(player1, "Ojutai's Summons");
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    private void addOjutaisSummonsMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private List<Permanent> djinnMonkTokens(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Djinn Monk"))
                .toList();
    }
}
