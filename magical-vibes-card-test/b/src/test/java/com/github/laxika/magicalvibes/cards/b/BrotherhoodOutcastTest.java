package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.a.ArgentumArmor;
import com.github.laxika.magicalvibes.cards.a.Arrest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GalvanicBlast;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrotherhoodOutcast.class, AccordersShield.class, ArgentumArmor.class, Arrest.class,
        GrizzlyBears.class, GalvanicBlast.class, HolyStrength.class})
class BrotherhoodOutcastTest extends BaseCardTest {

    @Test
    void returnsTargetEquipment() {
        Card equipment = new AccordersShield();
        harness.setGraveyard(player1, List.of(equipment));

        castReturnMode();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(equipment.getId());
        harness.handleMultipleCardsChosen(player1, List.of(equipment.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(equipment.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(equipment.getId()));
    }

    @Test
    void returnsTargetAuraAndAttachesIt() {
        Card aura = new HolyStrength();
        harness.setGraveyard(player1, List.of(aura));
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        castReturnMode();
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(findPermanent(player1, "Holy Strength").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void shieldModePutsShieldCounterOnTargetCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new BrotherhoodOutcast()));
        addMana();
        harness.castCreature(player1, 0, 1, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
    }

    @Test
    void reanimationModeRejectsNonAuraOrEquipmentCard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        castReturnMode();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void returnsManaValueThreeAuraAttachedToOpponentsCreature() {
        Card aura = new Arrest();
        harness.setGraveyard(player1, List.of(aura));
        Permanent creature = addCreatureReady(player2, new BrotherhoodOutcast());

        castReturnMode();
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(findPermanent(player1, "Arrest").getAttachedTo()).isEqualTo(creature.getId());
        harness.assertNotInGraveyard(player1, "Arrest");
    }

    @Test
    void excludesExpensiveEquipmentAndOpponentsGraveyard() {
        Card eligible = new AccordersShield();
        Card expensive = new ArgentumArmor();
        Card opponentsEquipment = new AccordersShield();
        harness.setGraveyard(player1, List.of(eligible, expensive));
        harness.setGraveyard(player2, List.of(opponentsEquipment));

        castReturnMode();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(eligible.getId());
        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Accorder's Shield").getAttachedTo()).isNull();
        harness.assertInGraveyard(player1, "Argentum Armor");
        harness.assertInGraveyard(player2, "Accorder's Shield");
    }

    @Test
    void doesNotReturnTargetThatLeavesGraveyardBeforeResolution() {
        Card equipment = new AccordersShield();
        harness.setGraveyard(player1, List.of(equipment));

        castReturnMode();
        harness.handleMultipleCardsChosen(player1, List.of(equipment.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(equipment));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Accorder's Shield");
        harness.assertNotOnBattlefield(player1, "Accorder's Shield");
    }

    @Test
    void shieldPreventsOneLethalDamageEventThenIsSpent() {
        Permanent creature = addCreatureReady(player1, new BrotherhoodOutcast());
        harness.setHand(player1, List.of(new BrotherhoodOutcast()));
        addMana();
        harness.castCreature(player1, 0, 1, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new GalvanicBlast(), new GalvanicBlast()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(creature.getCounterCount(CounterType.SHIELD)).isZero();

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player1, "Brotherhood Outcast");
    }

    private void castReturnMode() {
        harness.setHand(player1, List.of(new BrotherhoodOutcast()));
        addMana();
        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

}
