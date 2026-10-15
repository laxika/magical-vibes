package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.ConclaveTribunal;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HuntedWitness.class, WrathOfGod.class, ConclaveTribunal.class})
class HuntedWitnessTest extends BaseCardTest {

    @Test
    @DisplayName("When Hunted Witness dies, a 1/1 white Soldier token with lifelink is created")
    void deathTriggerCreatesLifelinkSoldierToken() {
        harness.addToBattlefield(player1, new HuntedWitness());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Hunted Witness");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Soldier").getFirst();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SOLDIER);
        assertThat(token.getCard().getKeywords()).contains(Keyword.LIFELINK);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Hunted Witness death trigger creates the Soldier for its controller")
    void deathTriggerBelongsToController() {
        harness.addToBattlefield(player2, new HuntedWitness());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Soldier")).hasSize(1);
        assertThat(findPermanents(player1, "Soldier")).isEmpty();
    }

    @Test
    @DisplayName("Simultaneous deaths create one 1/1 Soldier for each Witness's controller")
    void simultaneousDeathsCreateOneTokenForEachController() {
        harness.addToBattlefield(player1, new HuntedWitness());
        harness.addToBattlefield(player2, new HuntedWitness());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        assertThat(findPermanents(player1, "Soldier")).isEmpty();
        assertThat(findPermanents(player2, "Soldier")).isEmpty();
        harness.assertInGraveyard(player1, "Hunted Witness");
        harness.assertInGraveyard(player2, "Hunted Witness");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier")).singleElement().satisfies(token -> {
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getKeywords()).contains(Keyword.LIFELINK);
            assertThat(token.getCard().isToken()).isTrue();
        });
        assertThat(findPermanents(player2, "Soldier")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The created Soldier deals one combat damage and gains its controller one life")
    void createdSoldierGainsLifeFromCombatDamage() {
        harness.addToBattlefield(player1, new HuntedWitness());
        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Soldier").getFirst();
        token.setSummoningSick(false);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Exiling Hunted Witness does not create a Soldier")
    void exileDoesNotTriggerDeathAbility() {
        harness.addToBattlefield(player2, new HuntedWitness());
        var witnessId = harness.getPermanentId(player2, "Hunted Witness");
        harness.setHand(player1, List.of(new ConclaveTribunal()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, witnessId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card instanceof HuntedWitness);
        assertThat(findPermanents(player2, "Hunted Witness")).isEmpty();
        assertThat(findPermanents(player1, "Soldier")).isEmpty();
        assertThat(findPermanents(player2, "Soldier")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
