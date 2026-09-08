package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.j.JirinaDauntlessGeneral;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GeneralsEnforcer.class, EliteVanguard.class, JirinaDauntlessGeneral.class,
        GrizzlyBears.class, Shock.class})
class GeneralsEnforcerTest extends BaseCardTest {

    @Test
    @DisplayName("Gives indestructible to legendary Humans you control")
    void givesIndestructibleToLegendaryHumansYouControl() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GeneralsEnforcer());
        Permanent legendaryHuman = harness.addToBattlefieldAndReturn(player1, new JirinaDauntlessGeneral());
        Permanent nonlegendaryHuman = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        Permanent nonHuman = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentLegendaryHuman = harness.addToBattlefieldAndReturn(player2, new JirinaDauntlessGeneral());

        assertThat(gqs.hasKeyword(gd, source, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, legendaryHuman, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonlegendaryHuman, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, nonHuman, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentLegendaryHuman, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Exiles a noncreature card without creating a token")
    void exilesNoncreatureCardWithoutCreatingToken() {
        harness.addToBattlefield(player1, new GeneralsEnforcer());
        Card target = new Shock();
        harness.setGraveyard(player2, List.of(target));
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Shock");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.HUMAN)
                        && permanent.getCard().getSubtypes().contains(CardSubtype.SOLDIER));
    }

    @Test
    @DisplayName("Exiling a creature card creates a 1/1 white Human Soldier token")
    void exilingCreatureCardCreatesHumanSoldierToken() {
        harness.addToBattlefield(player1, new GeneralsEnforcer());
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> {
                    var card = permanent.getCard();
                    return card.isToken()
                            && card.hasType(CardType.CREATURE)
                            && card.getPower() == 1
                            && card.getToughness() == 1
                            && card.getColor() == CardColor.WHITE
                            && card.getSubtypes().containsAll(List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER));
                });
    }

    @Test
    @DisplayName("Cannot target a permanent instead of a graveyard card")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player1, new GeneralsEnforcer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
