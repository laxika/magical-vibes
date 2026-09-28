package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ViconiaNightsingersDisciple.class, Forest.class, Plains.class, GrizzlyBears.class})
class ViconiaNightsingersDiscipleTest extends BaseCardTest {

    @Test
    void exileAbilityTracksTheCardWithViconia() {
        Permanent viconia = harness.addToBattlefieldAndReturn(player1, new ViconiaNightsingersDisciple());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
        assertThat(gd.findExiledCard(creature.getId()).sourcePermanentId()).isEqualTo(viconia.getId());
    }

    @Test
    void greenSpecializationConjuresABoostedDuplicateThatCanBeCastWithAnyColorMana() {
        Permanent viconia = harness.addToBattlefieldAndReturn(player1, new ViconiaNightsingersDisciple());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 5, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(viconia.getCard().getName()).isEqualTo("Viconia, Disciple of Strength");
        Card duplicate = gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent duplicatePermanent = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(duplicate.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, duplicatePermanent)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, duplicatePermanent)).isEqualTo(4);
    }

    @Test
    void whiteSpecializationCanPutTheConjuredCreatureOntoTheBattlefield() {
        Permanent viconia = harness.addToBattlefieldAndReturn(player1, new ViconiaNightsingersDisciple());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(viconia.getCard().getName()).isEqualTo("Viconia, Disciple of Rebirth");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .hasSize(1);
    }
}
