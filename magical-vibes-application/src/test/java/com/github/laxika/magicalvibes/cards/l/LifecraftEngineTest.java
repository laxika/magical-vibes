package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LifecraftEngineTest extends BaseCardTest {

    private static Card createCreature(String name, CardSubtype subtype) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(2);
        card.setToughness(2);
        card.setSubtypes(List.of(subtype));
        return card;
    }

    private Permanent addEngine(CardSubtype chosenSubtype) {
        Permanent engine = new Permanent(new LifecraftEngine());
        engine.setChosenSubtype(chosenSubtype);
        gd.playerBattlefields.get(player1.getId()).add(engine);
        return engine;
    }

    @Test
    @DisplayName("Entering Lifecraft Engine prompts for a creature type")
    void enteringPromptsForCreatureType() {
        harness.setHand(player1, List.of(new LifecraftEngine()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("Chosen type is granted only to creature Vehicles you control")
    void grantsChosenTypeToCreatureVehiclesOnly() {
        addEngine(CardSubtype.WIZARD);
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        vehicle.setSummoningSick(false);
        Permanent goblin = addCreature(player1, CardSubtype.GOBLIN);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.computeStaticBonus(gd, vehicle).grantedSubtypes()).contains(CardSubtype.WIZARD);
        assertThat(gqs.computeStaticBonus(gd, goblin).grantedSubtypes())
                .doesNotContain(CardSubtype.WIZARD);
    }

    @Test
    @DisplayName("Other creatures you control of the chosen type get +1/+1")
    void boostsOtherCreaturesOfChosenType() {
        addEngine(CardSubtype.GOBLIN);
        Permanent goblin = addCreature(player1, CardSubtype.GOBLIN);

        var bonus = gqs.computeStaticBonus(gd, goblin);

        assertThat(bonus.power()).isEqualTo(1);
        assertThat(bonus.toughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The Engine itself is not boosted when it becomes a creature")
    void doesNotBoostItself() {
        Permanent engine = addEngine(CardSubtype.GOBLIN);
        engine.setAnimatedUntilEndOfTurn(true);

        var bonus = gqs.computeStaticBonus(gd, engine);

        assertThat(bonus.grantedSubtypes()).contains(CardSubtype.GOBLIN);
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    private Permanent addCreature(com.github.laxika.magicalvibes.model.Player player, CardSubtype subtype) {
        return harness.addToBattlefieldAndReturn(player, createCreature(subtype.name(), subtype));
    }
}
