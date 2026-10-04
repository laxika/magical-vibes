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

    @Test
    @DisplayName("Can exile a creature from your own graveyard")
    void canExileCreatureFromOwnGraveyard() {
        harness.addToBattlefield(player1, new GeneralsEnforcer());
        Card target = new GeneralsEnforcer();
        harness.setGraveyard(player1, List.of(target));
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "General's Enforcer");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Two activations targeting the same creature create only one token")
    void missingGraveyardTargetDoesNotCreateAnotherToken() {
        harness.addToBattlefield(player1, new GeneralsEnforcer());
        Card target = new GeneralsEnforcer();
        harness.setGraveyard(player2, List.of(target));
        addActivationMana();
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    @DisplayName("Legendary Humans survive lethal damage while the Enforcer remains")
    void legendaryHumanSurvivesLethalDamage() {
        harness.addToBattlefield(player1, new GeneralsEnforcer());
        Permanent human = harness.addToBattlefieldAndReturn(player1, new JirinaDauntlessGeneral());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, human.getId());

        harness.assertOnBattlefield(player1, "Jirina, Dauntless General");
        harness.assertNotInGraveyard(player1, "Jirina, Dauntless General");
    }

    @Test
    @DisplayName("Activated ability survives its source and static protection ends when it leaves")
    void abilityResolvesAfterSourceDiesAndProtectionEnds() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GeneralsEnforcer());
        Permanent human = harness.addToBattlefieldAndReturn(player1, new JirinaDauntlessGeneral());
        Card target = new GeneralsEnforcer();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, source.getId());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.castInstant(player1, 0, source.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "General's Enforcer");
        harness.assertInGraveyard(player1, "General's Enforcer");
        assertThat(gqs.hasKeyword(gd, human, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
