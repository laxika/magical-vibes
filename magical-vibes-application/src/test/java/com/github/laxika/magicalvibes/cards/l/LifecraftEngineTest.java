package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.g.GreasewrenchGoblin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LifecraftEngine.class, DuskLegionDreadnought.class, GreasewrenchGoblin.class, Card.class})
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
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new LifecraftEngine());
        engine.setChosenSubtype(chosenSubtype);
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

    @Test
    @DisplayName("The chosen type is stored when the artifact spell resolves")
    void storesChosenTypeOnEntry() {
        harness.setHand(player1, List.of(new LifecraftEngine()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");

        Permanent engine = findPermanent(player1, "Lifecraft Engine");
        assertThat(engine.getChosenSubtype()).isEqualTo(CardSubtype.GOBLIN);
        assertThat(gqs.isCreature(gd, engine)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, engine, CardSubtype.GOBLIN)).isFalse();
    }

    @Test
    @DisplayName("A boosted Goblin can pay Crew 3 and the Engine gains its chosen type")
    void crewsWithBoostedCreature() {
        Permanent engine = addEngine(CardSubtype.GOBLIN);
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GreasewrenchGoblin());

        assertThat(gqs.getEffectivePower(gd, goblin)).isEqualTo(3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(goblin.isTapped()).isTrue();
        assertThat(engine.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, engine)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, engine, CardSubtype.GOBLIN)).isTrue();
        assertThat(gqs.getEffectivePower(gd, engine)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, engine)).isEqualTo(4);
    }

    @Test
    @DisplayName("Other animated Engines gain the chosen type and bonus, but opposing Vehicles do not")
    void boostsOtherVehiclesThroughGrantedType() {
        addEngine(CardSubtype.GOBLIN);
        Permanent other = harness.addToBattlefieldAndReturn(player1, new LifecraftEngine());
        other.setChosenSubtype(CardSubtype.ELF);
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new LifecraftEngine());
        opposing.setChosenSubtype(CardSubtype.ELF);

        assertThat(gqs.hasEffectiveSubtype(gd, other, CardSubtype.GOBLIN)).isFalse();
        other.setAnimatedUntilEndOfTurn(true);
        opposing.setAnimatedUntilEndOfTurn(true);

        assertThat(gqs.hasEffectiveSubtype(gd, other, CardSubtype.GOBLIN)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, other, CardSubtype.ELF)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, other, CardSubtype.VEHICLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(5);
        assertThat(gqs.hasEffectiveSubtype(gd, opposing, CardSubtype.GOBLIN)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opposing)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opposing)).isEqualTo(4);
    }

    @Test
    @DisplayName("Only your creatures of the chosen type receive the bonus")
    void excludesOpponentsAndDifferentTypes() {
        Permanent engine = addEngine(CardSubtype.ELF);
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GreasewrenchGoblin());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new GreasewrenchGoblin());

        assertThat(gqs.computeStaticBonus(gd, own).power()).isZero();
        assertThat(gqs.computeStaticBonus(gd, own).toughness()).isZero();
        engine.setChosenSubtype(CardSubtype.GOBLIN);
        assertThat(gqs.computeStaticBonus(gd, own).power()).isEqualTo(1);
        assertThat(gqs.computeStaticBonus(gd, opposing).power()).isZero();
        assertThat(gqs.computeStaticBonus(gd, opposing).toughness()).isZero();
    }

    @Test
    @DisplayName("The static bonuses stop when the Engine leaves the battlefield")
    void bonusesEndWhenEngineLeaves() {
        Permanent engine = addEngine(CardSubtype.GOBLIN);
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GreasewrenchGoblin());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new LifecraftEngine());
        other.setChosenSubtype(CardSubtype.ELF);
        other.setAnimatedUntilEndOfTurn(true);

        assertThat(gqs.hasEffectiveSubtype(gd, other, CardSubtype.GOBLIN)).isTrue();
        assertThat(gqs.computeStaticBonus(gd, goblin).power()).isEqualTo(1);
        gd.playerBattlefields.get(player1.getId()).remove(engine);

        assertThat(gqs.hasEffectiveSubtype(gd, other, CardSubtype.GOBLIN)).isFalse();
        assertThat(gqs.computeStaticBonus(gd, other).power()).isZero();
        assertThat(gqs.computeStaticBonus(gd, other).toughness()).isZero();
        assertThat(gqs.computeStaticBonus(gd, goblin).power()).isZero();
        assertThat(gqs.computeStaticBonus(gd, goblin).toughness()).isZero();
    }
}
